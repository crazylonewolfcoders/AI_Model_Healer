package io.github.crazy.lonewolf.coders.configs;

import io.github.crazy.lonewolf.coders.omnij.plugin.config.OmniJConfig;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateProvisionStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RedisCacheConfig {
    private final OmniJConfig omniJConfig;

    public RedisCacheConfig(OmniJConfig omniJConfig) {
        this.omniJConfig = omniJConfig;
    }





    //Adding custom Redis Configurations easier to use existing redis if present....
    @Bean
    public LettuceConnectionFactory connectionFactory(){
        OmniJConfig.MemoryContainer memoryConfig = this.omniJConfig.getMemoryContainer();

        LettuceConnectionFactory  factory = new LettuceConnectionFactory();
        if(!memoryConfig.isRedisEnabled()){
            return  factory;
        }
        factory.setHostName(memoryConfig.getHostname());
        factory.setPort(memoryConfig.getPort());
        factory.setUseSsl(memoryConfig.isSSL());
        factory.setTimeout(memoryConfig.getTimeout());
        return factory;
    }


    @Bean
    @ConditionalOnBean(LettuceConnectionFactory.class)
    public ITemplateProvisionStrategy getMemoryTemplateConnectionFactory(LettuceConnectionFactory  connectionFactory){
        return new RedisStorageStrategy(connectionFactory);
    }



}
