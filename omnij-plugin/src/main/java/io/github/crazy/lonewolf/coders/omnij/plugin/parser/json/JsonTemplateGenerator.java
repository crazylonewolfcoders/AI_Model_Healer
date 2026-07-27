package io.github.crazy.lonewolf.coders.omnij.plugin.parser.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.crazy.lonewolf.coders.omnij.plugin.annotation.OmniJModel;
import io.github.crazy.lonewolf.coders.omnij.plugin.base.TemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Resolution;
import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.ITemplateGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

@Slf4j
@Component
public class JsonTemplateGenerator implements ITemplateGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<String> modelClassList = new ArrayList<String>();
    private final ApplicationContext context;
    private TemplateContainer container;


    public JsonTemplateGenerator(ApplicationContext context, TemplateContainer container) throws Exception {
        this.context=context;
        this.container = container;
        generateTemplates();
    }


    public List<String> generateTemplates() throws Exception {

        if(!modelClassList.isEmpty()){
            return modelClassList;
        }

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(OmniJModel.class));
        for (String basePackage : AutoConfigurationPackages.get(context)) {
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                log.trace("Found: " + bd.getBeanClassName());
                modelClassList.add(bd.getBeanClassName());
                String templateInfo = generateTemplateJson(bd.getBeanClassName());
                try {
                    Template template = generateTemplateObject(Class.forName(bd.getBeanClassName()), templateInfo, "");
                    if (template != null) {
                        Objects.requireNonNull(container).recordSuccess(bd.getBeanClassName(), template);
                    }
                }catch (Exception e){
                    log.error("Not able to generate templates for the bean class", e);
                }
            }
        }



        return modelClassList;
    }

    public Template generateTemplateObject(Class<?> clazz,String templateInfo,String inboundData){
        return new Template(clazz,templateInfo,new Date(),true,inboundData,"", Resolution.TEMPLATE);
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


    public String generateTemplateJson(String className) throws Exception {

        Class<?> clazz = Class.forName(className);

        String value = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(generateTemplate(clazz));
        log.trace(value);
        System.out.println(value);
        return value;
    }




}
