package io.github.crazy.lonewolf.sampleapp.configs;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.config.OmniJConfig;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateProvisionStrategy;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.RedisKeySerializer;
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
        return factory;
    }


    @Bean
    @ConditionalOnBean(LettuceConnectionFactory.class)
    public ITemplateProvisionStrategy getMemoryTemplateConnectionFactory(LettuceConnectionFactory  connectionFactory){
        return new RedisStorageStrategy(connectionFactory);
    }

    @Bean
    public RedisKeySerializer  redisKeySerializer(){
        return new RedisKeySerializer("app");
    }



}
