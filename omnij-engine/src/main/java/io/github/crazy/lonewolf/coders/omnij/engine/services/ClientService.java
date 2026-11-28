package io.github.crazy.lonewolf.coders.omnij.engine.services;

import io.github.crazy.lonewolf.coders.omnij.engine.repository.ClientRepository;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.ClientBean;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public List<ClientEntity> saveOrUpdateClientList(List<ClientBean> clients) {
        if (clients == null || clients.isEmpty()) {
            return List.of();
        }

        // 1. Collect IDs of clients that already have an ID
        List<UUID> existingIds = clients.stream()
                .map(ClientBean::getId)
                .filter(id -> id != null && !id.isBlank())
                .map(UUID::fromString)
                .toList();

        // 2. Fetch existing entities in a single SELECT query (avoids N+1 queries and preserves keyHash)
        Map<UUID, ClientEntity> existingMap = clientRepository.findAllById(existingIds).stream()
                .collect(Collectors.toMap(ClientEntity::getId, Function.identity()));

        // 3. Map beans to existing entities (update) or new entities (insert)
        List<ClientEntity> clientEntities = clients.stream()
                .map(bean -> {
                    UUID parsedId = (bean.getId() != null && !bean.getId().isBlank())
                            ? UUID.fromString(bean.getId())
                            : null;

                    ClientEntity entity = (parsedId != null && existingMap.containsKey(parsedId))
                            ? existingMap.get(parsedId)
                            : new ClientEntity();

                    entity.setClientName(bean.getClientName());
                    // Only set ID manually if your entity doesn't auto-generate it on new inserts
                    if (parsedId != null && entity.getId() == null) {
                        entity.setId(parsedId);
                    }
                    return entity;
                })
                .toList();

        return clientRepository.saveAll(clientEntities);
    }


}
