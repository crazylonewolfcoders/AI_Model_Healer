package io.github.crazy.lonewolf.coders.omnij.plugin.beans;

import io.github.crazy.lonewolf.coders.omnij.plugin.config.OmniJConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter @Setter
public class TemplateInfo {
    private String clazz="";
    private String upstreamKey ="";
    private ResolutionState  resolutionState = ResolutionState.SUCCESS;
    private TemplateInfoDetails infoDetails = new TemplateInfoDetails();


    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        if (!(obj instanceof TemplateInfo other)) return false;

        return Objects.equals(this.clazz, other.clazz) &&
                Objects.equals(this.upstreamKey, other.upstreamKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.clazz, this.upstreamKey);
    }

    public String toRedisKey() {

        String cleanClazz = (clazz == null || clazz.isEmpty()) ? "default_class" : clazz;
        String cleanUpstream = (upstreamKey == null || upstreamKey.isEmpty()) ? "default_upstream" : upstreamKey;

        return String.format(Objects.requireNonNullElse(OmniJConfig.APP_NAME,"app")+":templates:%s:%s", cleanClazz, cleanUpstream);
    }

    @Override
    public String toString() {
        return toRedisKey();
    }
}
