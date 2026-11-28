package io.github.crazy.lonewolf.coders.omnij.datasource.service;

import io.github.crazy.lonewolf.coders.omnij.datasource.annotation.OmniJTransactional;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientTemplateEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientTemplateId;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientTemplateService {

    private final ClientTemplateRepository repository;

    public ClientTemplateService(ClientTemplateRepository repository) {
        this.repository = repository;
    }

    public Optional<ClientTemplateEntity> findClientTemplate(String clientId, String appName, String className) {
        return repository.findById(new ClientTemplateId(clientId, appName, className));
    }

    @OmniJTransactional
    public ClientTemplateEntity markPendingResolution(String clientId, String appName, String className) {
        ClientTemplateId id = new ClientTemplateId(clientId, appName, className);
        ClientTemplateEntity entity = repository.findById(id)
                .orElseGet(() -> ClientTemplateEntity.builder().id(id).build());
        entity.setActive(false);
        return repository.save(entity);
    }

    @OmniJTransactional
    public ClientTemplateEntity activateClientTemplate(String clientId, String appName, String className, String mappingJson) {
        ClientTemplateId id = new ClientTemplateId(clientId, appName, className);
        ClientTemplateEntity entity = repository.findById(id)
                .orElseGet(() -> ClientTemplateEntity.builder().id(id).build());
        entity.setMappingJson(mappingJson);
        entity.setActive(true);
        return repository.save(entity);
    }

    @OmniJTransactional
    public int deactivateForSchemaChange(String appName, String className) {
        return repository.deactivateAllForClass(appName, className);
    }
}
