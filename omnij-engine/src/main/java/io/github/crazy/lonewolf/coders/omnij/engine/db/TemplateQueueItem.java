package io.github.crazy.lonewolf.coders.omnij.engine.db;

public record TemplateQueueItem(
    Long id,
    String clientId,
    String appName,
    String className,
    String operation,
    ResolutionStatus status,
    boolean approved
) {}