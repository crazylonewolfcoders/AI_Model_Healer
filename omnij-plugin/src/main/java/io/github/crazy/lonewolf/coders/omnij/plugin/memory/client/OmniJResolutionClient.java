package io.github.crazy.lonewolf.coders.omnij.plugin.memory.client;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@HttpExchange("/api/v1/omnij/resolutions")
public interface OmniJResolutionClient {



    //Report a failed mapping so the Resolver Microservice marks Template Container PENDING & queues it for Angular UI
    @PostExchange("/failures")
    EngineResponse reportMappingFailure(@RequestBody FailureReportRequest request);


    @PostExchange("/clients/add")
    EngineResponse addClients(@RequestBody ClientsInfo clients);

    //  Optional fallback endpoint to pull an approved template directly if Redis was evicted
    @GetExchange("/{clientId}/{className}/{operation}")
    Optional<ResolvedTemplateResponse> fetchResolvedTemplate(
            @PathVariable("clientId") UUID clientId,
            @PathVariable("className") String className,
            @PathVariable("operation") String operation
    );

    @PostExchange("/clients/sync")
    void syncClientMetadata(@RequestBody ClientsInfo clients);
}
