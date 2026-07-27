package io.github.crazy.lonewolf.coders.omnij.plugin.base;

import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TemplateContainer {

    private final Map<String, Template> successTemplates = new ConcurrentHashMap<>();
    private final Map<String, Template> failedTemplates = new ConcurrentHashMap<>();

    public  Template getSuccess(String key) {
        return successTemplates.get(key);
    }

    public  Template getFailure(String key) {
        return failedTemplates.get(key);
    }

    public  void recordSuccess(String key, Template template) {
        if (key != null) {
            successTemplates.put(key, template);
            failedTemplates.remove(key);
        }
    }

    public  void recordFailure(String key, Template template) {
        if (key != null) {

            failedTemplates.put(key, template);
            successTemplates.remove(key);
        }
    }





}
