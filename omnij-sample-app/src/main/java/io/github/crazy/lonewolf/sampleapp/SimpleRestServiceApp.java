package io.github.crazy.lonewolf.sampleapp;

import io.github.crazy.lonewolf.coders.omnij.datasource.OmniJDataSourceConfig;
import io.github.crazy.lonewolf.coders.override.datasource.WebAppPrimaryJpaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "io.github.crazy.lonewolf.coders.omnij.plugin.memory"
        ,"io.github.crazy.lonewolf.sampleapp"
}, scanBasePackageClasses =  {OmniJDataSourceConfig.class, WebAppPrimaryJpaConfig.class})
@Slf4j
public class SimpleRestServiceApp {
    public static void main(String[] args) {
        log.info("Starting Application");
        SpringApplication.run(SimpleRestServiceApp.class, args);

    }

}