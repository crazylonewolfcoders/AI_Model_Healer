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

    @Parameter(property = "action", required = true)
    private String action;

    @Parameter(property="jdbcUrl", required = true)
    private String jdbcUrl;
    @Parameter(property="username", required = true)
    private String username;
    @Parameter(property="password", required = true)
    private String password;
    @Parameter(property="appName", required = true)
    private String appName;


    @Override
    public void execute() throws MojoExecutionException {
        // Use the built-in Maven logger
        getLog().info("====================================");
        getLog().info("[OMNIJ PLUGIN]: " );

        IMetaDataTemplateGenerator gen = null;
        try {

            switch (action) {
                case "file":
                    gen = new MavenJsonMetaDataGenerator(outputDirectory);
                    gen.generateMetaData( metaDataLocation,basePackagesLocation);
                    break;
                case "db":
                    getLog().info("Utilizing Application Db configurations based on configurations" );
                    if(!username.isEmpty() && !password.isEmpty() && !jdbcUrl.isEmpty()){
                        getLog().info("Omnij plugin: Omnij plugin Client Class");
                        gen = new MavenDBDataGenerator(jdbcUrl,username,password,appName, outputDirectory);
                        gen.generateMetaData(basePackagesLocation);

                    }
                    break;
                default:
                    gen = new MavenJsonMetaDataGenerator(outputDirectory);
                    gen.generateMetaData( metaDataLocation,basePackagesLocation);
                    break;

            }

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }




        getLog().info("====================================");
    }

}
