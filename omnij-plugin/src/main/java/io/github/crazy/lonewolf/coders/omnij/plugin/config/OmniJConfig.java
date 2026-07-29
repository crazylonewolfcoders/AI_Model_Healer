package io.github.crazy.lonewolf.coders.omnij.plugin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@ConfigurationProperties(prefix = "omnij")
@Getter
@Setter
@Configuration
public class OmniJConfig {

    @Value("${spring.application.name:default-app-name}")
    public static  String APP_NAME;

    private Engine engine = new Engine();
    private JsonValidations jsonvalidations = new JsonValidations();
    private MemoryContainer memoryContainer = new MemoryContainer();

    @Getter @Setter
    public static class Engine {
        private boolean enableAutoHealing = true;
        private String metaDataLocation = "META-INF/omnij/";

    }

    @Getter @Setter
    public static class JsonValidations{
        private boolean enableMandatoryChecks = false;
    }

    @Getter @Setter
    public static class MemoryContainer{
        private boolean isRedisEnabled = false;
        private String hostname ="localhost";
        private int port = 6379;
        private boolean isSSL = false;
        private int timeout = 3000;


    }
}

