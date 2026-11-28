package io.github.crazy.lonewolf.coders.omnij.plugin.memory.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.crazy.lonewolf.coders.omnij.datasource.db.OmniJTemplateEnitty;
import io.github.crazy.lonewolf.coders.omnij.datasource.repository.ClientRepository;
import io.github.crazy.lonewolf.coders.omnij.datasource.service.ClientService;
import io.github.crazy.lonewolf.coders.omnij.datasource.service.ClientTemplateService;
import io.github.crazy.lonewolf.coders.omnij.datasource.service.SingleFlightTemplateLoader;
import io.github.crazy.lonewolf.coders.omnij.datasource.service.TemplateService;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientTemplateEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientTemplateId;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.annotation.OmniJsonResolver;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Resolution;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.TemplateKey;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.client.OmniJResolutionDispatcher;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.config.OmniJConfig;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.exceptions.JsonMappingException;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.exceptions.JsonTemplateNotFoundException;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.exceptions.JsonTemplateProcessingException;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateContainer;
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
import java.util.UUID;

@Slf4j
public class JsonArgumentResolver implements HandlerMethodArgumentResolver {



    private final ITemplateEngine templateEngine;

    private final ITemplateContainer container;

    private final OmniJConfig config;

    private final ClientService service;

    private final TemplateService  templateService;

    private final OmniJResolutionDispatcher resolutionDispatcher;

    private final SingleFlightTemplateLoader loader;

    private final ClientTemplateService clientTemplateService;

    public JsonArgumentResolver(@Qualifier("JsonEngine") ITemplateEngine templateEngine
            , ITemplateContainer container
            , OmniJConfig config
            , ClientService service
            , TemplateService templateService
            , OmniJResolutionDispatcher resolutionDispatcher
            , SingleFlightTemplateLoader templateLoader
            , ClientTemplateService clientTemplateService
    ) {

        this.templateEngine = templateEngine;
        this.container = container;
        this.config = config;
        this.service = service;
        this.templateService = templateService;
        this.resolutionDispatcher = resolutionDispatcher;
        this.loader = templateLoader;
        this.clientTemplateService = clientTemplateService;
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(OmniJsonResolver.class);
    }

    @Override
    /*public @Nullable Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {


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

        try {
            if( !config.getEngine().isEnableAutoHealing()) {
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

        String apiKey = servletRequest.getHeader("omnij-api-key");
        String clientid = servletRequest.getHeader("omnij-client-id");

        ClientEntity entity = service.getClientByClientKey(clientid, apiKey);

        if(entity.getClientid() == null){
            throw new JsonMappingException("Client not found");
        }


        TemplateKey templateInfo = new TemplateKey();
        templateInfo.setClazz(parameter.getParameterType().getTypeName());
        templateInfo.setAppName(OmniJConfig.APP_NAME);


        Template tmp;
        try {
            tmp = container.getDefault(templateInfo); // we can find some alernatives later.

        }catch (JsonTemplateProcessingException e){

            OmniJTemplateEnitty tne = this.templateService.getTemplate(templateInfo.getClazz());

            tmp = Template.builder().clientid(entity.getId().toString()).template(tne.getDestinationTemplate()). build();
        }




        Object obj = null;
        TemplateKey key = new TemplateKey();
        key.setAppName(OmniJConfig.APP_NAME);
        key.setClazz(tmp.clazzName());
        key.setClientId(tmp.clientid());

        Template finalTmp = tmp;
        obj = this.loader.loadCoalesced(key,()->{

            Object rtVal = null;
            switch (finalTmp.resolution()) {
            case TEMPLATE:
                rtVal= resolveUsingTemplate(finalTmp,data,parameter.getParameterType());
                break;
            case ENGINE:
                log.info("Resolution pending from Engine");

                try {
                    rtVal = resolveUsingEngine(finalTmp, data, parameter.getParameterType());
                }catch (Exception e){
                    String inboundTemplate = JsonSkeletonExtractor.extractSafeSkeleton(data);
                    finalTmp.withInboundTemplate(inboundTemplate);
                    log.info("Inbound template resolving using Resolution Engine");
                    container.recordFailure(templateInfo, finalTmp);
                }

                break;
            default:

                    container.recordFailure(templateInfo, finalTmp);
                break;



        }

        return rtVal;
        });

        return obj;

    }*/

    public @Nullable Object resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (servletRequest == null) {
            throw new IllegalStateException("Current request is not an HttpServletRequest");
        }

        String data;
        try {
            data = new String(servletRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error reading inbound payload stream", e);
            throw new JsonMappingException("Inbound input stream error");
        }

        Class<?> targetClass = parameter.getParameterType();

        // 2. Authenticate client
        String apiKey = servletRequest.getHeader("omnij-api-key");
        String clientId = servletRequest.getHeader("omnij-client-id");

        if((apiKey!=null && clientId == null)  || (clientId !=null && apiKey == null )){
            throw new JsonMappingException("Headers are missing. Both clientid and apikey is required");
        }

        // 1. If auto-healing is disabled, use standard direct Jackson mapping
        if (!config.getEngine().isEnableAutoHealing()|| (apiKey == null || apiKey.isBlank()) || (clientId == null || clientId.isBlank())) {
            try {
                return resolveUsingDirectMapping(data, targetClass);
            } catch (Exception e) {
                log.error("Direct mapping failed and OmniJ Auto-Healing is disabled", e);
                throw new JsonMappingException("Direct mapping failed");
            }
        }


        ClientEntity entity = service.getClientByClientKey(clientId, apiKey);
        if (entity == null || entity.getClientid() == null) {
            throw new JsonMappingException("Unauthorized or unknown OmniJ client");
        }

        String className = targetClass.getTypeName();
        TemplateKey clientKey = new TemplateKey();
        clientKey.setAppName(OmniJConfig.APP_NAME);
        clientKey.setClazz(className);
        clientKey.setClientId(entity.getClientid());

        Template activeTemplate = container.getSuccess(clientKey);
        if (activeTemplate == null) {
            activeTemplate = (Template) this.loader.loadCoalesced(clientKey, () -> {
                Template rechecked =  container.getSuccess(clientKey);

                if (rechecked == null) {
                    rechecked = container.getFailure(clientKey);
                    if(rechecked != null) {
                        log.error("Ending Resolution Pending");
                        return Template.builder()
                                .resolution(Resolution.ENGINE)
                                .build();
                    }


                }else{
                    return rechecked;
                }

                TemplateKey defaultKey = new TemplateKey();
                defaultKey.setAppName(OmniJConfig.APP_NAME);
                defaultKey.setClazz(className);
                Template def = null ;
                try {
                    Optional<ClientTemplateEntity> clienttmp = this.clientTemplateService.findClientTemplate(
                            entity.getClientid(), OmniJConfig.APP_NAME, className);

                    if (clienttmp.isPresent()) {
                        ClientTemplateEntity ct = clienttmp.get();

                        // 1. Deactivated or awaiting resolution -> SKIP default template & fail fast!
                        if (!ct.isActive()) {
                            log.warn("Client template inactive/pending for client={} class={}", entity.getClientid(), className);
                            return Template.builder()
                                    .clientid(entity.getClientid())
                                    .clazzName(className)
                                    .resolution(Resolution.ENGINE)
                                    .build();
                        }

                        // 2. Active approved custom client template -> Warm Redis & return!
                        Template approved = Template.builder()
                                .template(ct.getMappingJson())
                                .clazzName(className)
                                .resolution(Resolution.TEMPLATE)
                                .clientid(entity.getClientid())
                                .build();
                        container.recordSuccess(clientKey, approved);
                        return approved;
                    }
                } catch (JsonTemplateProcessingException e) {
                  log.error("Default template not found", e);
                }

                OmniJTemplateEnitty tne = this.templateService.getTemplate(className);
                Template built = Template.builder()
                        .clientid(entity.getClientid())
                        .clazzName(className)
                        .template(tne.getDestinationTemplate())
                        .resolution(Resolution.TEMPLATE)
                        .build();

                return built;
            });
        }

        switch (activeTemplate.resolution()) {
            case TEMPLATE:
                try {
                    return resolveUsingTemplate(activeTemplate, data, targetClass);
                } catch (Exception e) {
                    String inboundSkeleton = JsonSkeletonExtractor.extractSafeSkeleton(data);
                    Template failedTmp = activeTemplate
                            .withInboundTemplate(inboundSkeleton)
                            .withResolution(Resolution.ENGINE);

                    log.info("Template mapping failed; recording failure & dispatching to Resolution Engine");
                    container.recordFailure(clientKey, failedTmp);
                    resolutionDispatcher.notifyFailureAsync(
                            entity.getId(),
                            className,
                            "Resolve",
                            failedTmp.template(),
                            inboundSkeleton,
                            e.getMessage()
                    );
                    throw new JsonMappingException("Payload requires mapping resolution for " + className);
                }

            case ENGINE:
                log.info("Resolution still pending from Engine for client={} class={}", entity.getClientid(), className);
                throw new JsonMappingException("Payload awaiting mapping resolution in Engine for " + className);

            default:
                container.recordFailure(clientKey, activeTemplate);
                throw new JsonMappingException("Unsupported template resolution state: " + activeTemplate.resolution());
        }
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


    private Object resolveUsingEngine(Template tmp,String inboundData, Class<?> clazz) throws JsonMappingException {

        if (tmp == null) {
            throw new JsonMappingException("No template found for target class: " + clazz.getName());
        }

        return Optional.of(tmp)
                .filter(Template::isValid)
                .map(t -> applyTemplate(t, inboundData, clazz))
                .orElseGet(() -> {
                    log.info("Inbound template invalid or unapproved; recording failure for Engine resolution");

                    String inboundSkeleton = JsonSkeletonExtractor.extractSafeSkeleton(inboundData);
                    Template failedTemplate = tmp.withInboundTemplate(inboundSkeleton);

                    TemplateKey templateInfo = new TemplateKey();
                    templateInfo.setAppName(OmniJConfig.APP_NAME);
                    templateInfo.setClazz(failedTemplate.clazzName());
                    templateInfo.setClientId(failedTemplate.clientid());

                    container.recordFailure(templateInfo, failedTemplate);
                    try {
                    //  notify  Resolver Microservice asynchronously
                    resolutionDispatcher.notifyFailureAsync(
                            UUID.fromString(failedTemplate.clientid()),
                            failedTemplate.clazzName(),
                            "Resolve",
                            failedTemplate.template(),
                            inboundSkeleton,
                            "Template invalid or unmapped"
                    );


                        throw new JsonMappingException("Error processing template for class: " + clazz.getName());
                    } catch (JsonMappingException e) {
                        throw new RuntimeException(e);
                    }
                });
                //orElseThrow( ()-> new JsonTemplateProcessingException(tmp,"Error processing "));
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
