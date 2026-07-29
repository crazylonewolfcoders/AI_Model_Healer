package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

public interface ITemplateProvisionStrategy {
    public ITemplateContainer provisionContainer();
    boolean isHealthy();
}
