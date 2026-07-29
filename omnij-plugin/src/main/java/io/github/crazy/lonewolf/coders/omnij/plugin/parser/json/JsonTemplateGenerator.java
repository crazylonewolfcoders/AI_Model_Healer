package io.github.crazy.lonewolf.coders.omnij.plugin.parser.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.crazy.lonewolf.coders.omnij.plugin.annotation.OmniJModel;
import io.github.crazy.lonewolf.coders.omnij.plugin.beans.TemplateInfo;
import io.github.crazy.lonewolf.coders.omnij.plugin.config.OmniJConfig;
import io.github.crazy.lonewolf.coders.omnij.plugin.generator.NativeScanner;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Resolution;
import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.ITemplateGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JsonTemplateGenerator implements ITemplateGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<Template> templateArrayList = new ArrayList<Template>();
    private  ApplicationContext context;
    private ITemplateContainer container;
    private OmniJConfig config;

    public JsonTemplateGenerator(ApplicationContext context, ITemplateContainer container, OmniJConfig config) throws Exception {
        this.context=context;
        this.container = container;
        this.config = config;
     //   generateTemplates(); // To be replaced with compile time templates generations
    }

    public JsonTemplateGenerator(){}


    public List<Template> generateTemplates() throws Exception {

        if(!templateArrayList.isEmpty()){
            return templateArrayList;
        }

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(OmniJModel.class));
        for (String basePackage : AutoConfigurationPackages.get(context)) {
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                log.trace("Found: " + bd.getBeanClassName());

                String templateInfo = generateTemplateJson(bd.getBeanClassName());
                try {
                    Template template = generateTemplateObject(Class.forName(bd.getBeanClassName()), templateInfo, "");
                    if (template != null) {
                        TemplateInfo templateKey = new TemplateInfo();
                        templateKey.setClazz(bd.getBeanClassName());
                       // Objects.requireNonNull(container).recordSuccess(templateKey, template); // update the method to write it into a static file.
                        templateArrayList.add(template);
                    }
                }catch (Exception e){
                    log.error("Not able to generate templates for the bean class", e);
                }
            }
        }



        return templateArrayList;
    }

    public Template generateTemplateObject(Class<?> clazz,String templateInfo,String inboundData){
        return new Template(clazz.getName(),templateInfo,new Date(),true,inboundData,"", Resolution.TEMPLATE);
    }


    public ObjectNode generateTemplate(Class<?> clazz) {
        return generateTemplate(clazz, "");
    }

    public ObjectNode generateTemplate(Class<?> clazz, String path) {
        ObjectNode root = objectMapper.createObjectNode();

        for (Field field : clazz.getDeclaredFields()) {
           // field.setAccessible(true);

            String name = field.getName();
            Class<?> type = field.getType();
            String fullPath = path.isEmpty() ? name : path + "/" + name;

            // Primitive-like types
            if (isPrimitiveLike(type)) {
                root.put(name, "/" + fullPath );
            }else if(field.getType().equals(Date.class)){
                root.put(name, "/" + fullPath );
            }
            // Collections
            else if (Collection.class.isAssignableFrom(type)) {
                ArrayNode arrayNode = objectMapper.createArrayNode();

                // Try to get generic type
                Class<?> elementType = getCollectionElementType(field);

                if (elementType != null && !isPrimitiveLike(elementType)) {
                    arrayNode.add(generateTemplate(elementType, fullPath + "[0]"));
                } else {
                    arrayNode.add("/" + fullPath + "[0]");
                }

                root.set(name, arrayNode);
            }
            // Maps
            else if (Map.class.isAssignableFrom(type)) {
                ObjectNode mapNode = objectMapper.createObjectNode();
                mapNode.put("key", "/" + fullPath + ".key");
                mapNode.put("value", "/" + fullPath + ".value");
                root.set(name, mapNode);
            }
            // Nested object
            else {
                root.set(name, generateTemplate(type, fullPath));
            }
        }

        return root;
    }

    @Override
    public List<Template> generateTemplates(ClassLoader loader, String path) {
            templateArrayList.clear();
        try {
            List<Class<?>> classList = NativeScanner.getClassesWithAnnotation(loader,path, OmniJModel.class);

            for(Class<?> bd: classList){

            log.trace("Found: " + bd.getName());

            String templateInfo = generateTemplateJson(bd.getName(),loader);
            try {
                Template template = generateTemplateObject(bd, templateInfo, "");
                if (template != null) {
                    TemplateInfo templateKey = new TemplateInfo();
                    templateKey.setClazz(bd.getName());
                    // Objects.requireNonNull(container).recordSuccess(templateKey, template); // update the method to write it into a static file.
                    templateArrayList.add(template);
                }
            }catch (Exception e){
                log.error("Not able to generate templates for the bean class", e);
            }


            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    return templateArrayList;

    }

    @Override
    public void loadTemplates(ITemplateContainer container) {
        String BASE_PATH = config.getEngine().getMetaDataLocation();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // 1. Read the index file to discover filenames dynamically
        try (InputStream indexStream = classLoader.getResourceAsStream(BASE_PATH + "files.idx")) {
            if (indexStream == null) {
                // If no index exists, return an empty map safely
               return;
            }

            try (BufferedReader indexReader = new BufferedReader(new InputStreamReader(indexStream, StandardCharsets.UTF_8))) {
                // Collect file names into a Java 8 List
                List<String> fileNames = indexReader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toList());

                // 2. Map filenames to their contents via the classloader stream pipeline
                 fileNames.stream().forEach(fileName -> {
                    Template t = readContentFromJar(classLoader, BASE_PATH + fileName);
                     TemplateInfo  templateKey = new TemplateInfo();
                     String clazzName = fileName.substring(0, fileName.lastIndexOf("."));
                     templateKey.setClazz(clazzName);
                    container.recordSuccess(templateKey,t);

                 });
            }
        } catch (Exception e) {
            System.err.println("Failed to dynamically resolve templates: " + e.getMessage());

        }
    }

    private static Template readContentFromJar(ClassLoader classLoader, String fullPath) {
        try (InputStream is = classLoader.getResourceAsStream(fullPath)) {
            if (is == null) return null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                ObjectMapper mapper = new ObjectMapper();
                return  mapper.readValue(content, Template.class);

            }
        } catch (Exception e) {
            log.error("Could not load contents for " + fullPath + ": " + e.getMessage());
            return null;
        }
    }


    private boolean isPrimitiveLike(Class<?> type) {
        return type == String.class ||
                type == Boolean.class || type == boolean.class ||
                Number.class.isAssignableFrom(type) ||
                type.isPrimitive();
    }

    private Class<?> getCollectionElementType(Field field) {
        try {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            return (Class<?>) pt.getActualTypeArguments()[0];
        } catch (Exception e) {
            return null;
        }
    }


    public String generateTemplateJson(String className, ClassLoader classLoader) throws Exception {

        Class<?> clazz = Class.forName(className,false,classLoader);

        String value = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(generateTemplate(clazz));
        log.trace(value);
        System.out.println(value);
        return value;
    }

    public String generateTemplateJson(String className) throws Exception {
        ClassLoader pluginClassLoader = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = Class.forName(className,false,pluginClassLoader);

        String value = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(generateTemplate(clazz));
        log.trace(value);
        System.out.println(value);
        return value;
    }





}
