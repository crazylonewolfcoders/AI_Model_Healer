package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

import io.github.crazy.lonewolf.coders.omnij.plugin.beans.TemplateInfo;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

public class RedisKeySerializer implements RedisSerializer<String> {

    private final String appPrefix;
    private final StringRedisSerializer delegate = StringRedisSerializer.UTF_8;

    public RedisKeySerializer(String appName) {
        this.appPrefix = appName;
    }

    /**
     * Centralized, production-safe key formatter
     */
    public String toRedisKey(TemplateInfo info) {
        return String.format("%s:templates:%s:%s", 
            this.appPrefix, 
            info.getClazz(),

            info.getUpstreamKey()
        );
    }

    @Override
    public byte[] serialize(@Nullable String value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        // Safely convert the string key into a valid binary byte array for Redis
        return delegate.serialize(value);
    }

    @Override
    public @Nullable String deserialize(byte @Nullable [] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        // Convert the binary bytes back into a readable Java String
        return delegate.deserialize(bytes);
    }
}
