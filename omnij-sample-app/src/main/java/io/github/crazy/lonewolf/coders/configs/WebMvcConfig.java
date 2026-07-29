package io.github.crazy.lonewolf.coders.configs;

import io.github.crazy.lonewolf.coders.omnij.plugin.base.JsonArgumentResolver;
import io.github.crazy.lonewolf.coders.omnij.plugin.config.PluginConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@ConditionalOnBean(PluginConfig.class)
public class WebMvcConfig  implements WebMvcConfigurer {
    @Autowired
    private JsonArgumentResolver omniJResolver;



    // Add our custom resolver to the list of argument resolvers
    // Custom resolvers run after Spring's built-in annotation-based resolvers
    // (@RequestParam, @PathVariable, @RequestBody, etc.) but before the
    // catch-all type-based fallback resolvers
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(omniJResolver);
    }


}
