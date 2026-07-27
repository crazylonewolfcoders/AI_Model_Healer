package io.github.crazy.lonewolf.coders.omnij.plugin.exceptions;

import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;

public class JsonTemplateProcessingException extends RuntimeException{
    Template template;
    public JsonTemplateProcessingException(Template t, String s) {
        super(s);
        template = t;
    }
    public JsonTemplateProcessingException(Template t, Exception e){
        super(e);
        template= t;
    }

    public Template getTemplateData(){
        return template;
    }
}
