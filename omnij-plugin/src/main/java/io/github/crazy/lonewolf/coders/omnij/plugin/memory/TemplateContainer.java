package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.beans.TemplateInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemplateContainer implements ITemplateContainer {

    private final Map<TemplateInfo, Template> successTemplates = new ConcurrentHashMap<>();
    private final Map<TemplateInfo, Template> failedTemplates = new ConcurrentHashMap<>();
    private final Map<TemplateInfo,Template> pendingReview = new ConcurrentHashMap<>();

    @Override
    public Template getSuccess(TemplateInfo key) {
        return successTemplates.get(key);
    }
    @Override
    public Template getFailure(TemplateInfo key) {
        return failedTemplates.get(key);
    }

    @Override
    public Template getPendingReview(TemplateInfo key){
        return pendingReview.get(key);
    }

    @Override
    public void recordSuccess(TemplateInfo key, Template template) {
        if (key != null) {
            successTemplates.put(key, template);
            failedTemplates.remove(key);
        }
    }
    @Override
    public void recordFailure(TemplateInfo key, Template template) {
        if (key != null) {

            failedTemplates.put(key, template);
            successTemplates.remove(key);
        }
    }

    @Override
    public void recordPendingReview(TemplateInfo key, Template template) {
        if (key != null) {
            pendingReview.put(key, template);
            failedTemplates.remove(key);
            successTemplates.remove(key);
        }
    }


}
