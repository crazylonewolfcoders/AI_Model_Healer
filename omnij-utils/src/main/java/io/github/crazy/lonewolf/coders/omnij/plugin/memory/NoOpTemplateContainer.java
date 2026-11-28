package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.TemplateKey;

public class NoOpTemplateContainer implements ITemplateContainer {

    @Override
    public Template getSuccess(TemplateKey key) {
        return null;
    }

    @Override
    public Template getFailure(TemplateKey key) {
        return null;
    }

    @Override
    public Template getPendingReview(TemplateKey key) {
        return null;
    }

    @Override
    public Template getDefault(TemplateKey key) {
        return null;
    }

    @Override
    public void recordSuccess(TemplateKey key, Template template) {

    }

    @Override
    public void recordFailure(TemplateKey key, Template template) {

    }

    @Override
    public void recordPendingReview(TemplateKey key, Template template) {

    }
}
