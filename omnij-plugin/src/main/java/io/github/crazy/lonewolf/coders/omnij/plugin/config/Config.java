package io.github.crazy.lonewolf.coders.omnij.plugin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.crazy.lonewolf.coders.omnij.plugin.base.TemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.ITemplateGenerator;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.ITemplateEngine;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.json.JsonTemplateGenerator;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.json.JsonTemplateEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@ConditionalOnBean(RequiredConfigs.class)
public class Config {


    @Autowired
    private RequiredConfigs configs;



    @Bean("JsonEngine")
    public ITemplateEngine jsonTemplateEngine(){
        return  new JsonTemplateEngine();
   }





}

