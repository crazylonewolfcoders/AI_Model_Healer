package io.github.crazy.lonewolf.coders.omnij.engine.db;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Resolution;

public record TemplateQueueItem(
    Long id,
    String clientId,
    String appName,
    String className,
    String operation,
    Resolution status,
    boolean approved
) {}