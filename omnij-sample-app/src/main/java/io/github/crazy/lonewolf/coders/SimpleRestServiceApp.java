package io.github.crazy.lonewolf.coders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class SimpleRestServiceApp {
    public static void main(String[] args) {
        log.info("Starting Application");
        SpringApplication.run(SimpleRestServiceApp.class, args);

    }

}