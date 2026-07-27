package io.github.crazy.lonewolf.coders.omnij.plugin.parser;

import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.exceptions.JsonTemplateProcessingException;

public interface ITemplateEngine {
    public  Object processTemplateAndGenerateOutPut(Template template, String inboundJson) throws JsonTemplateProcessingException;
}
