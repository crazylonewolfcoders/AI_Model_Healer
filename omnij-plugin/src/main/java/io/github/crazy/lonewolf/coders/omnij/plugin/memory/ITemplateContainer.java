package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.beans.TemplateInfo;

public interface ITemplateContainer {

    Template getSuccess(TemplateInfo key);

    Template getFailure(TemplateInfo key);

    Template getPendingReview(TemplateInfo key);

    void recordSuccess(TemplateInfo key, Template template);

    void recordFailure(TemplateInfo key, Template template);

    void recordPendingReview(TemplateInfo key, Template template);

}
