package io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;

import java.time.Instant;

@Builder
public record Template (
    String clazzName,
    String template,
    Instant created,
    boolean isValid,
    String clientid,
    String downStream,
    Resolution resolution,
    String inboundTemplate,
    String operation
){
    public Template {
        if (resolution == null) {
            resolution = Resolution.DIRECT;
        }

    }
    public String toString(){
        try {
            return toJson();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Template withInboundTemplate(String inboundTemplate){
        return new Template(clazzName(),template(),created(),isValid(), clientid(),downStream(),resolution(),inboundTemplate, operation());
    }



    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public Template withResolution(Resolution resolution) {
        return new Template(clazzName(),template(),created(),isValid(), clientid(),downStream(),resolution,inboundTemplate(), operation());
    }
}

