package io.github.crazy.lonewolf.coders.omnij.plugin.memory.client;

import io.github.crazy.lonewolf.coders.omnij.datasource.annotation.OmniJTransactional;
import io.github.crazy.lonewolf.coders.omnij.datasource.repository.ClientRepository;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.OmnijClientEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.ClientBean;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.ClientsInfo;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.config.OmniJConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableScheduling
@Slf4j
public class ClientMetadataSyncWorker {

    private final ClientRepository clientRepository;
    private final OmniJResolutionClient resolutionClient;

    public ClientMetadataSyncWorker(ClientRepository clientRepository,
                                    OmniJResolutionClient resolutionClient) {
        this.clientRepository = clientRepository;
        this.resolutionClient = resolutionClient;
    }

    @Scheduled(fixedDelayString = "${omnij.sync.interval-ms:15000}")
    @OmniJTransactional
    public void syncPendingClients() {
        List<OmnijClientEntity> unsynced = clientRepository.findBySyncedToResolverFalse();
        if (unsynced.isEmpty()) {
            return;
        }

        List<ClientBean> payload = unsynced.stream()
                .map(c -> new ClientBean(
                        c.getId().toString(),
                        c.getClientName(),
                        "",
                        c.isActive(),
                        null
                ))
                .toList();

        try {
            ClientsInfo i = new ClientsInfo();
            i.setClients(payload);
            resolutionClient.syncClientMetadata(i);
            unsynced.forEach(c -> c.setSyncedToResolver(true));
            clientRepository.saveAll(unsynced);
            log.info("Synced {} client metadata records to Resolver Microservice", unsynced.size());
        } catch (Exception ex) {
            // Leaves syncedToResolver = false so it automatically retries on the next tick
            log.warn("Client metadata sync delayed (Resolver unreachable): {}", ex.getMessage());
        }
    }
}
