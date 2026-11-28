package io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans;

import java.util.UUID;

public record ResolvedTemplateResponse(
        UUID clientId,
        String className,
        String operation,
        String resolvedMappingJson,
        boolean approved
) {}
