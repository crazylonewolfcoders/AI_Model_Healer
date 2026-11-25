package io.github.crazy.lonewolf.coders.omnij.plugin.memory.generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.net.MalformedURLException;

@Mojo(name="omnij",defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class OmniJPlugin extends AbstractMojo {

    // Configurable parameter passed from the target pom.xml
    @Parameter(property = "metaDataLocation", required = true)
    private String metaDataLocation;

    @Parameter(property = "basePackagesLocation", required = true)
    private String basePackagesLocation;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        // Use the built-in Maven logger
        getLog().info("====================================");
        getLog().info("[OMNIJ PLUGIN]: " );

        IMetaDataTemplateGenerator gen = null;
        try {
            gen = new MavenJsonMetaDataGenerator(outputDirectory);
            gen.generateMetaData( metaDataLocation,basePackagesLocation);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }




        getLog().info("====================================");
    }

}
