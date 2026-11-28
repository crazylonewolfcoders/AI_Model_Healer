package io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans;

import java.time.Instant;
import java.util.UUID;

public record FailureReportRequest(
        UUID clientId,
        String appName,
        String className,
        String operation,
        String destinationTemplateJson,
        String sampleSourceJson,
        String failureReason,
        Instant occurredAt
) {}
