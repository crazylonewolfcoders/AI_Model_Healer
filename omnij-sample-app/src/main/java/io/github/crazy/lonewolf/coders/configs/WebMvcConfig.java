package io.github.crazy.lonewolf.coders.configs;

import io.github.crazy.lonewolf.coders.omnij.plugin.base.JsonArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig  implements WebMvcConfigurer {

    private final JsonArgumentResolver omniJResolver;

    public WebMvcConfig( JsonArgumentResolver resolver) {
        this.omniJResolver = resolver;
    }

    // Add our custom resolver to the list of argument resolvers
    // Custom resolvers run after Spring's built-in annotation-based resolvers
    // (@RequestParam, @PathVariable, @RequestBody, etc.) but before the
    // catch-all type-based fallback resolvers
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(omniJResolver);
    }
}
