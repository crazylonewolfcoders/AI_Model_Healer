package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.TemplateKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemplateContainer implements ITemplateContainer {

    private final Map<TemplateKey, Template> successTemplates = new ConcurrentHashMap<>();
    private final Map<TemplateKey, Template> failedTemplates = new ConcurrentHashMap<>();
    private final Map<TemplateKey,Template> pendingReview = new ConcurrentHashMap<>();

    @Override
    public Template getSuccess(TemplateKey key) {
        return successTemplates.get(key);
    }
    @Override
    public Template getFailure(TemplateKey key) {
        return failedTemplates.get(key);
    }

    @Override
    public Template getPendingReview(TemplateKey key){
        return pendingReview.get(key);
    }

    @Override
    public Template getDefault(TemplateKey key) {
        return null;
    }

    @Override
    public void recordSuccess(TemplateKey key, Template template) {
        if (key != null) {
            successTemplates.put(key, template);
            failedTemplates.remove(key);
        }
    }
    @Override
    public void recordFailure(TemplateKey key, Template template) {
        if (key != null) {

            failedTemplates.put(key, template);
            successTemplates.remove(key);
        }
    }

    @Override
    public void recordPendingReview(TemplateKey key, Template template) {
        if (key != null) {
            pendingReview.put(key, template);
            failedTemplates.remove(key);
            successTemplates.remove(key);
        }
    }


}
