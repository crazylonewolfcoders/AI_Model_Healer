package io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans;

import java.time.Instant;
import java.util.UUID;

public record EngineResponse(
            UUID ticketId,
            String status, // e.g., "PENDING_RESOLUTION"
            String message
    ) {}


