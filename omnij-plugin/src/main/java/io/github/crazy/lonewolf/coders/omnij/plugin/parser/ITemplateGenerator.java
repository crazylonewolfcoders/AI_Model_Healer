package io.github.crazy.lonewolf.coders.omnij.plugin.parser;

import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateContainer;

import java.util.List;

public interface ITemplateGenerator {
    Object generateTemplate(Class<?> clazz);

    Object generateTemplate(Class<?> clazz, String path) ;

    List<Template> generateTemplates(ClassLoader loader,String path) ;

    void loadTemplates(ITemplateContainer container);
}
