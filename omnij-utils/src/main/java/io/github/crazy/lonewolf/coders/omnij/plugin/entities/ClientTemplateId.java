package io.github.crazy.lonewolf.coders.omnij.plugin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record ClientTemplateId(
        @Column(name = "client_id", length = 100, nullable = false)
        String clientId,

        @Column(name = "app_name", length = 120, nullable = false)
        String appName,

        @Column(name = "class_name", length = 255, nullable = false)
        String className
) implements Serializable {}
