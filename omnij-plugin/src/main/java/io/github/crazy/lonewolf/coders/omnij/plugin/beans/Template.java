package io.github.crazy.lonewolf.coders.omnij.plugin.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


public record Template (
    String clazzName,
    String template,
    Date created,
    boolean isValid,
    String upStream,
    String downStream,
    Resolution resolution
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

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}

