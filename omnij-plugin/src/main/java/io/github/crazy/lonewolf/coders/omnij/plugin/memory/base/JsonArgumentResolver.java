package io.github.crazy.lonewolf.coders.omnij.plugin.memory.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.annotation.OmniJsonResolver;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.TemplateInfo;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.config.OmniJConfig;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.exceptions.JsonMappingException;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.exceptions.JsonTemplateNotFoundException;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.exceptions.JsonTemplateProcessingException;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.TemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.parser.ITemplateEngine;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
public class JsonArgumentResolver implements HandlerMethodArgumentResolver {



    private final ITemplateEngine templateEngine;

    private final ITemplateContainer container;

    private final OmniJConfig config;





    public JsonArgumentResolver( @Qualifier("JsonEngine") ITemplateEngine templateEngine ,  ITemplateContainer container,OmniJConfig config) {

        this.templateEngine = templateEngine;
        this.container = container;
        this.config = config;
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(OmniJsonResolver.class);
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {


        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        if (servletRequest == null) {
            throw new IllegalStateException("Current request is not an HttpServletRequest");
        }
        String data ;
        try {

            data = new String(servletRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error getting paylod input stream", e);
            throw new JsonMappingException("Inbound input stream error");
        }

        TemplateInfo templateInfo = new TemplateInfo();
        templateInfo.setClazz(parameter.getParameterType().getTypeName());
        templateInfo.setUpstreamKey(webRequest.getHeader("upstream-key"));

        Template tmp = container.getSuccess(templateInfo);

        try {
            if(tmp == null || !config.getEngine().isEnableAutoHealing()) {
                log.info("No template found hence resolving using Direct Mapping using default Jackson");
              return  resolveUsingDirectMapping(data, parameter.getParameterType());
            }
        }catch (Exception e){
            if(config.getEngine().isEnableAutoHealing()) {
                log.error("Processing Failed for direct Mapping... Resort to template mapping by updating the templates... ");
                throw new JsonTemplateNotFoundException(parameter.getParameterType(), e);
            }else{
                log.error("Processing Failed for direct Mapping... And OmniJ Resolution is not enabled.. ");
                throw new JsonMappingException(e);
            }
        }

        Object rtVal = null;
        switch (tmp.resolution()) {
            case TEMPLATE:
                rtVal= resolveUsingTemplate(tmp,data,parameter.getParameterType());
                break;
            case ENGINE:
                log.info("Resolution pending from Engine");

                try {
                    rtVal = resolveUsingEngine(tmp, data, parameter.getParameterType());
                }catch (Exception e){
                    String inboundTemplate = JsonSkeletonExtractor.extractSafeSkeleton(data);
                    tmp.withInboundTemplate(inboundTemplate);
                    log.info("Inbound template resolving using Resolution Engine");
                    container.recordFailure(templateInfo,tmp);
                }

                break;
            default:

                    container.recordFailure(templateInfo, tmp);
                break;



        }

        return rtVal;

    }


    /*
        Resolve first using Direct Mapping if failed then try with template....
     */
    private Object resolveUsingDirectMapping(String data,Class clazz) throws JsonProcessingException {

            ObjectMapper objectMapper = new ObjectMapper();
            if(this.config.getJsonvalidations().isEnableMandatoryChecks()){
              //  objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
              //  objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

            }
            return    objectMapper.readValue(data, clazz); // Check if the mapping is valid for direct object conversion. ...

    }

    private Object resolveUsingTemplate(Template tmp,String inboundData, Class<?> clazz){
       return Optional.ofNullable(tmp).filter(Template::isValid).
                map(t-> applyTemplate(t,inboundData,clazz)
                ).
                orElseThrow( ()-> new JsonTemplateProcessingException(tmp,"Error processing "));
    }


    private Object resolveUsingEngine(Template tmp,String inboundData, Class<?> clazz){
        return Optional.ofNullable(tmp).filter(Template::isValid).
                map(t-> applyTemplate(t,inboundData,clazz)
                ).
                orElseThrow( ()-> new JsonTemplateProcessingException(tmp,"Error processing "));
    }

    private Object applyTemplate(Template t, String inboundData, Class<?> targetType) throws JsonTemplateProcessingException {
            try {

                if(t ==null){
                    throw new JsonTemplateNotFoundException(targetType,"No Template found for this class and is failing with direct conversion");
                }


                return templateEngine.processTemplateAndGenerateOutPut(t, inboundData);

            }catch (Exception e){
                throw new JsonTemplateProcessingException(t,e);
            }

    }

}
