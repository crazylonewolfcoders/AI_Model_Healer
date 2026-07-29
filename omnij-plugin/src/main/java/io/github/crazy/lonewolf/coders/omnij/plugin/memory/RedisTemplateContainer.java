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
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Implementaion for Redis Cache Container for heavy loads
 */
@Slf4j
public class RedisTemplateContainer implements ITemplateContainer{
    @Autowired
    private OmniJConfig.MemoryContainer container;


    private  RedisTemplate<String,Template> redisTemplate;

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

        }
    }




    @Override
    public Template getSuccess(TemplateInfo key) {
        String redisKey = keySerializer.toRedisKey(key);
        String rawData = (String) redisTemplate.opsForHash().get(redisKey,FIELD_SUCCESS);
        return deserialize(rawData);
    }

    @Override
    public Template getFailure(TemplateInfo key) {
        String redisKey = keySerializer.toRedisKey(key);
        String rawData = (String) redisTemplate.opsForHash().get(redisKey, FIELD_FAILURE);
        return deserialize(rawData);
    }

    @Override
    public Template getPendingReview(TemplateInfo key) {
        String redisKey = keySerializer.toRedisKey(key);
        String rawData = (String) redisTemplate.opsForHash().get(redisKey, FIELD_PENDING);
        return deserialize(rawData);
    }

    @Override
    public void recordSuccess(TemplateInfo key, Template template) {
        if (key == null) return;
        String redisKey = keySerializer.toRedisKey(key);

        // HSET writes the success template, HDEL removes failure and pending states instantly
        redisTemplate.opsForHash().put(redisKey, FIELD_SUCCESS, serialize(template));
        redisTemplate.opsForHash().delete(redisKey, FIELD_FAILURE, FIELD_PENDING);
    }

    @Override
    public void recordFailure(TemplateInfo key, Template template) {
        if (key == null) return;
        String redisKey = keySerializer.toRedisKey(key);

        redisTemplate.opsForHash().put(redisKey, FIELD_FAILURE, serialize(template));
        redisTemplate.opsForHash().delete(redisKey, FIELD_SUCCESS, FIELD_PENDING);
    }

    @Override
    public void recordPendingReview(TemplateInfo key, Template template) {
        if (key == null) return;
        String redisKey = keySerializer.toRedisKey(key);

        redisTemplate.opsForHash().put(redisKey, FIELD_PENDING, serialize(template));
        redisTemplate.opsForHash().delete(redisKey, FIELD_SUCCESS, FIELD_FAILURE);
    }


    private String serialize(Template template)  {
        if (template == null) return "";
        try {
            return template.toJson(); // Or use ObjectMapper to write as JSON string
        } catch (JsonProcessingException e) {
          log.error(e.getMessage(),e);
          throw  new RuntimeException(e);
        }
    }

    private Template deserialize(String rawData) {
        if (rawData == null || rawData.isEmpty()) return null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(rawData,Template.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(),e);
            throw  new RuntimeException(e);
        }
    }
}
