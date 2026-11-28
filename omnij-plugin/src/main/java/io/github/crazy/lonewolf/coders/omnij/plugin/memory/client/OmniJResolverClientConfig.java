package io.github.crazy.lonewolf.coders.omnij.plugin.memory.client;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.config.OmniJConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class OmniJResolverClientConfig {

    @Bean
    public OmniJResolutionClient omnijResolutionClient(OmniJConfig config) {
        OmniJConfig.ResolverConfig resolver = config.getResolverConfig();

        RestClient restClient = RestClient.builder()
                .baseUrl(resolver.getBaseUrl())
                .defaultHeader("X-API-Key", resolver.getApiKey())
                .defaultHeader("X-API-Secret", resolver.getApiSecret())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(OmniJResolutionClient.class);
    }
}
