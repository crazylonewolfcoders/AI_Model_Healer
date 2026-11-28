package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.TemplateKey;

public interface ITemplateContainer {

    Template getSuccess(TemplateKey key);

    Template getFailure(TemplateKey key);

    Template getPendingReview(TemplateKey key);

    Template getDefault(TemplateKey key);

    void recordSuccess(TemplateKey key, Template template);

    void recordFailure(TemplateKey key, Template template);

    void recordPendingReview(TemplateKey key, Template template);


}
