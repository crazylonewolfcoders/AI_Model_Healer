package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.beans.TemplateInfo;
import io.github.crazy.lonewolf.coders.omnij.plugin.config.OmniJConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Implementaion for Redis Cache Container for heavy loads
 */
@Slf4j
public class RedisTemplateContainer implements ITemplateContainer{



    private  RedisTemplate<String,Object> redisTemplate;

    private  RedisKeySerializer keySerializer;

    // Fields inside our Redis Hash
    private static final String FIELD_SUCCESS = "success";
    private static final String FIELD_FAILURE = "failure";
    private static final String FIELD_PENDING = "pending";

    public RedisTemplateContainer(RedisConnectionFactory connectionFactory, String appName) {
        if(connectionFactory != null){
            this.redisTemplate = new RedisTemplate<>();

            redisTemplate.setConnectionFactory(connectionFactory);
            this.keySerializer = new RedisKeySerializer(appName);
            redisTemplate.setKeySerializer(this.keySerializer);
            redisTemplate.setHashKeySerializer(new StringRedisSerializer());

            // 4. Fix the Hash Values (The template data payloads)
            // Enforce standard JSON serialization so your templates store as clean, legible JSON strings in Redis
            JacksonJsonRedisSerializer<Template> jsonSerializer = new JacksonJsonRedisSerializer<>(Template.class);
            redisTemplate.setValueSerializer(jsonSerializer);
            redisTemplate.setHashValueSerializer(jsonSerializer);

            // 5. CRITICAL: Force Spring to validate and bind all serializers before releasing the bean
            redisTemplate.afterPropertiesSet();
        }
    }




    @Override
    public Template getSuccess(TemplateInfo key) {
        String redisKey = keySerializer.toRedisKey(key);
        return (Template) redisTemplate.opsForHash().get(redisKey,FIELD_SUCCESS);

    }

    @Override
    public Template getFailure(TemplateInfo key) {
        String redisKey = keySerializer.toRedisKey(key);
        return (Template)redisTemplate.opsForHash().get(redisKey, FIELD_FAILURE);

    }

    @Override
    public Template getPendingReview(TemplateInfo key) {
        String redisKey = keySerializer.toRedisKey(key);
        return (Template) redisTemplate.opsForHash().get(redisKey, FIELD_PENDING);

    }

    @Override
    public void recordSuccess(TemplateInfo key, Template template) {
        if (key == null) return;
        String redisKey = keySerializer.toRedisKey(key);

        // HSET writes the success template, HDEL removes failure and pending states instantly
        redisTemplate.opsForHash().put(redisKey, FIELD_SUCCESS, template);
        redisTemplate.opsForHash().delete(redisKey, FIELD_FAILURE, FIELD_PENDING);
    }

    @Override
    public void recordFailure(TemplateInfo key, Template template) {
        if (key == null) return;
        String redisKey = keySerializer.toRedisKey(key);

        redisTemplate.opsForHash().put(redisKey, FIELD_FAILURE, template);
        redisTemplate.opsForHash().delete(redisKey, FIELD_SUCCESS, FIELD_PENDING);
    }

    @Override
    public void recordPendingReview(TemplateInfo key, Template template) {
        if (key == null) return;
        String redisKey = keySerializer.toRedisKey(key);

        redisTemplate.opsForHash().put(redisKey, FIELD_PENDING, template);
        redisTemplate.opsForHash().delete(redisKey, FIELD_SUCCESS, FIELD_FAILURE);
    }


}
