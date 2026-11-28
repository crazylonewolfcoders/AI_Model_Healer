package io.github.crazy.lonewolf.sampleapp.configs;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateProvisionStrategy;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.RedisTemplateContainer;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class RedisStorageStrategy implements ITemplateProvisionStrategy {
    
    private final LettuceConnectionFactory connectionFactory;

    public RedisStorageStrategy(LettuceConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }


    @Override
    public ITemplateContainer provisionContainer() {
        return new RedisTemplateContainer(this.connectionFactory, "appName");
    }

    @Override
    public boolean isHealthy() {
        try {
            // Eagerly check connection health to prevent runtime failures
            if (!connectionFactory.isRunning()) {
                connectionFactory.afterPropertiesSet();
                connectionFactory.start();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
