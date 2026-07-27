package io.github.crazy.lonewolf.coders.omnij.plugin.config;

import io.github.crazy.lonewolf.coders.omnij.plugin.base.TemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.ITemplateGenerator;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.json.JsonTemplateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequiredConfigs {

    private final TemplateContainer container  = new TemplateContainer();

    @Autowired
    private ApplicationContext context;

    @Bean
    public TemplateContainer initateTemplateContainer(){
        return container;
    }

    @Bean("JsonGenerator")
    public ITemplateGenerator jsonTemplateGenerator() throws Exception {
        return  new JsonTemplateGenerator(context,container);
    }


}
