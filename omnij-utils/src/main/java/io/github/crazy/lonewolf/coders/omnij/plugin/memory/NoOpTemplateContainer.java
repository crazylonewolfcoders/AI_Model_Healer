package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.TemplateInfo;

public class NoOpTemplateContainer implements ITemplateContainer {

    @Override
    public Template getSuccess(TemplateInfo key) {
        return null;
    }

    @Override
    public Template getFailure(TemplateInfo key) {
        return null;
    }

    @Override
    public Template getPendingReview(TemplateInfo key) {
        return null;
    }

    @Override
    public void recordSuccess(TemplateInfo key, Template template) {

    }

    @Override
    public void recordFailure(TemplateInfo key, Template template) {

    }

    @Override
    public void recordPendingReview(TemplateInfo key, Template template) {

    }
}
