package io.github.crazy.lonewolf.coders.omnij.plugin.memory.parser;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.exceptions.JsonTemplateProcessingException;

public interface ITemplateEngine {
    public  Object processTemplateAndGenerateOutPut(Template template, String inboundJson) throws JsonTemplateProcessingException;
}
