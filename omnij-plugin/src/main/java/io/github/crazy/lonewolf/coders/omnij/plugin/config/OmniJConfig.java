package io.github.crazy.lonewolf.coders.omnij.plugin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "omnij")
@Getter
@Setter
@Configuration
public class OmniJConfig {

    private Engine engine = new Engine();
    private JsonValidations jsonvalidations = new JsonValidations();

    @Getter @Setter
    public static class Engine {
        private boolean enableAutoHealing = true;
    }

    @Getter @Setter
    public static class JsonValidations{
        private boolean enableMandatoryChecks = false;
    }
}

