package io.github.crazy.lonewolf.coders.omnij.plugin.beans;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


public record Template (
    Class<?> clazz,
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
}

