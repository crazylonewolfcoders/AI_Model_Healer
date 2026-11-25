package io.github.crazy.lonewolf.coders.omnij.plugin.memory;

public class DefaultInstanceFactory implements ITemplateProvisionStrategy {

    Object memoryTemplateConnectionFactory;

    public DefaultInstanceFactory(Object memoryTemplateConnectionFactory){
        this.memoryTemplateConnectionFactory = memoryTemplateConnectionFactory;
    }



    @Override
    public ITemplateContainer provisionContainer() {
        return (ITemplateContainer) memoryTemplateConnectionFactory;
    }

    @Override
    public boolean isHealthy() {
        return false;
    }
}
