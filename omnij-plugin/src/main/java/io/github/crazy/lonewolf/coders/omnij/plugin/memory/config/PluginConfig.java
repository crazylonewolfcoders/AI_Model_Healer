package io.github.crazy.lonewolf.coders.omnij.plugin.memory.config;

import io.github.crazy.lonewolf.coders.omnij.datasource.service.ClientService;
import io.github.crazy.lonewolf.coders.omnij.datasource.service.ClientTemplateService;
import io.github.crazy.lonewolf.coders.omnij.datasource.service.SingleFlightTemplateLoader;
import io.github.crazy.lonewolf.coders.omnij.datasource.service.TemplateService;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateProvisionStrategy;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.TemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.base.JsonArgumentResolver;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.client.OmniJResolutionDispatcher;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.parser.ITemplateEngine;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.parser.ITemplateGenerator;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.parser.json.JsonTemplateEngine;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.parser.json.JsonTemplateGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class PluginConfig {

    private final OmniJConfig omniJConfig;

    public PluginConfig(OmniJConfig omniJConfig) {
        this.omniJConfig = omniJConfig;
    }


    @Bean
    public ITemplateContainer getMemoryTemplateContainer(ITemplateProvisionStrategy strategy, OmniJConfig omniJConfig ) {

        OmniJConfig.MemoryContainer container = omniJConfig.getMemoryContainer();

        // 1. Safe Fallback Condition Check
        if (container == null || !container.isRedisEnabled() || strategy == null) {
          log.info(">>> OmniJ: Falling back to local in-memory template container.");
            if(!container.isUseCustomContainer())
                return new TemplateContainer();

        }

        try {
            log.info(">>> OmniJ: Initializing enterprise Redis template container.");

            assert  strategy!= null;
            return strategy.provisionContainer();

        } catch (Exception e) {
            // Safety boundary: Log the failure and drop back to local memory instead of crashing the core server
            log.info(">>> OmniJ Critical: Failed to establish Redis connection. Emergency fallback to local memory active.");
            log.error(e.getMessage(), e);

        }
        return new TemplateContainer();
    }



    @Bean("JsonEngine")
    @ConditionalOnBean(ITemplateGenerator.class)
    public ITemplateEngine jsonTemplateEngine(){
        return  new JsonTemplateEngine();
    }



    @Bean("JsonGenerator")
    @ConditionalOnBean(ITemplateContainer.class)
    public ITemplateGenerator jsonTemplateGenerator(ITemplateContainer container, ApplicationContext context, OmniJConfig config) throws Exception {
        JsonTemplateGenerator jsonTemplateGenerator =  new JsonTemplateGenerator(context,container,config);
        jsonTemplateGenerator.loadTemplates(container);
        return jsonTemplateGenerator;
    }

    @Bean
    @ConditionalOnBean({ITemplateEngine.class,ITemplateContainer.class, TemplateService.class,ClientService.class,OmniJResolutionDispatcher.class})
    public JsonArgumentResolver generateJsonArgumentResolver(ITemplateEngine iTemplateEngine
            , OmniJConfig omniJConfig, ITemplateContainer container, ClientService clientService
            , TemplateService templateService, OmniJResolutionDispatcher  resolutionDispatcher , SingleFlightTemplateLoader singleFlightTemplateLoader, ClientTemplateService clientTemplateService) {
        return new JsonArgumentResolver(iTemplateEngine,container,omniJConfig,clientService,templateService, resolutionDispatcher,singleFlightTemplateLoader,clientTemplateService);
    }




}

