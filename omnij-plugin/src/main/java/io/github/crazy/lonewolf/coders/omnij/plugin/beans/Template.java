package io.github.crazy.lonewolf.coders.omnij.plugin.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


public record Template (
    @JsonIgnore
     Class<?> clazz,
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
        if(clazz == null){
            try {
                clazz = Class.forName(clazzName);
            } catch (ClassNotFoundException e) {

            }
        }
    }



    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}

