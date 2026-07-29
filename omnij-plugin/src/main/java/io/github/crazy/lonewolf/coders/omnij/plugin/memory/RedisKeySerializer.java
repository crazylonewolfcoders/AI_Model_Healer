package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

import io.github.crazy.lonewolf.coders.omnij.plugin.beans.TemplateInfo;

public class RedisKeySerializer {

    private final String appPrefix;

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
}
