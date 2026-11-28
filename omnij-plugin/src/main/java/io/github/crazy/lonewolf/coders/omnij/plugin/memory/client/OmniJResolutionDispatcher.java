package io.github.crazy.lonewolf.coders.omnij.plugin.memory.client;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.FailureReportRequest;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Resolution;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.TemplateKey;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.config.OmniJConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class OmniJResolutionDispatcher {

    private final OmniJResolutionClient resolutionClient;
    private final ITemplateContainer  templateContainer;

    public OmniJResolutionDispatcher(OmniJResolutionClient resolutionClient,  ITemplateContainer templateContainer) {
        this.resolutionClient = resolutionClient;
        this.templateContainer = templateContainer;
    }




    @Async
    public void notifyFailureAsync(
            UUID clientId,
            String className,
            String operation,
            String destinationTemplateJson,
            String sourceJson,
            String failureReason) {

        try {
            var request = new FailureReportRequest(
                    clientId,
                    OmniJConfig.APP_NAME,
                    className,
                    operation,
                    destinationTemplateJson,
                    sourceJson,
                    failureReason,
                    Instant.now()
            );

            TemplateKey key = new TemplateKey();
            key.setClientId(clientId.toString());
            key.setAppName(OmniJConfig.APP_NAME);
            key.setClazz(className);


           Template template =  templateContainer.getFailure(key);

           if(template == null){

               template = new Template(className,destinationTemplateJson,null,false,clientId.toString(), "", Resolution.ENGINE,sourceJson,"resolve");

               templateContainer.recordFailure(key,template);

               var response = resolutionClient.reportMappingFailure(request);

               log.info("Reported unmapped payload for client={} class={} -> ticketId={} ({})",
                       clientId, className, response.ticketId(), response.status());

           }else{
               log.info("Already Failure present and not resolved");
           }


        } catch (Exception ex) {
            // Log warning without crashing the host application if Resolver Microservice is temporarily down
            log.warn("Failed to notify Resolver Microservice for client={} class={}: {}",
                    clientId, className, ex.getMessage());
        }
    }
}
