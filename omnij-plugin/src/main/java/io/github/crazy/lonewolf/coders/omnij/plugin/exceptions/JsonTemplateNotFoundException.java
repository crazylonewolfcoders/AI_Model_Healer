package io.github.crazy.lonewolf.coders.omnij.plugin.exceptions;

import lombok.Getter;

@Getter
public class JsonTemplateNotFoundException extends RuntimeException{
    Class<?> instance;
    public JsonTemplateNotFoundException(Class<?> instance, String s) {
        super(s);
        this.instance = instance;
    }
    public JsonTemplateNotFoundException(Class<?> instance, Exception e){
        super(e);
        this.instance= instance;
    }

}
