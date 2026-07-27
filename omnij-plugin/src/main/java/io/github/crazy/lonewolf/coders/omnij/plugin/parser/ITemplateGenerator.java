package io.github.crazy.lonewolf.coders.omnij.plugin.parser;

public interface ITemplateGenerator {
    Object generateTemplate(Class<?> clazz);

    Object generateTemplate(Class<?> clazz, String path) ;
}
