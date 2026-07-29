package io.github.crazy.lonewolf.coders.omnij.plugin.config;

import io.github.crazy.lonewolf.coders.omnij.plugin.annotation.OmniJModel;
import io.github.crazy.lonewolf.coders.omnij.plugin.base.JsonArgumentResolver;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.RedisTemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.TemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.ITemplateEngine;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.ITemplateGenerator;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.json.JsonTemplateEngine;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.json.JsonTemplateGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@Slf4j
public class PluginConfig {

    private final OmniJConfig omniJConfig;

    public PluginConfig(OmniJConfig omniJConfig) {
        this.omniJConfig = omniJConfig;
    }


    @Bean
    public ITemplateContainer getMemoryTemplateContainer(LettuceConnectionFactory connectionFactory, OmniJConfig omniJConfig ) {

        OmniJConfig.MemoryContainer container = omniJConfig.getMemoryContainer();

        // 1. Safe Fallback Condition Check
        if (container == null || !container.isRedisEnabled() || connectionFactory == null) {
          log.info(">>> OmniJ: Falling back to local in-memory template container.");
            return new TemplateContainer();
        }

        try {
            log.info(">>> OmniJ: Initializing enterprise Redis template container.");


            // 4. Safely return the container passing the correct wrapper type
            return new RedisTemplateContainer(connectionFactory, "App");

        } catch (Exception e) {
            // Safety boundary: Log the failure and drop back to local memory instead of crashing the core server
            log.info(">>> OmniJ Critical: Failed to establish Redis connection. Emergency fallback to local memory active.");
            log.error(e.getMessage(), e);
            return new TemplateContainer();
        }
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
    @ConditionalOnBean({ITemplateEngine.class,ITemplateContainer.class})
    public JsonArgumentResolver generateJsonArgumentResolver(ITemplateEngine iTemplateEngine,OmniJConfig omniJConfig,ITemplateContainer container){
        return new JsonArgumentResolver(iTemplateEngine,container,omniJConfig);
    }


}

