package io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter @Setter
public class TemplateKey {
    private String clazz="";
    private String upstreamKey ="";
    private String fingerprint;
    private String appName;
    private String clientId;
    public TemplateKey(){}

    public TemplateKey(String appName){
        this.appName = appName;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        if (!(obj instanceof TemplateKey other)) return false;

        return Objects.equals(this.clazz, other.clazz) &&
                Objects.equals(this.upstreamKey, other.upstreamKey);
    }

    public String getUpstreamKey() {
        return upstreamKey.isEmpty() ? "default_upstream" : upstreamKey;
    }
    public void setUpstreamKey(String upstreamKey) {}

    @Override
    public int hashCode() {
        return Objects.hash(this.clazz, this.upstreamKey);
    }

    public String toRedisKey() {

        String cleanClazz = (clazz == null || clazz.isEmpty()) ? "default_class" : clazz;
        String cleanUpstream = (upstreamKey == null || upstreamKey.isEmpty()) ? "default_upstream" : upstreamKey;

        return String.format(Objects.requireNonNullElse(this.appName,"app")+":templates:%s:%s", cleanClazz, cleanUpstream);
    }

    @Override
    public String toString() {
        return toRedisKey();
    }
}
