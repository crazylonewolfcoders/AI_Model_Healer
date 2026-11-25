package io.github.crazy.lonewolf.coders.omnij.engine.configs;


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

    //Adding custom Redis Configurations easier to use existing redis if present....
    @Bean
    public LettuceConnectionFactory connectionFactory(){

        LettuceConnectionFactory  factory = new LettuceConnectionFactory();

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
