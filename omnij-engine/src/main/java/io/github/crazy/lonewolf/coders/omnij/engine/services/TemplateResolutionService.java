package io.github.crazy.lonewolf.coders.omnij.engine.services;

import io.github.crazy.lonewolf.coders.omnij.engine.db.TemplateEntity;
import io.github.crazy.lonewolf.coders.omnij.engine.db.TemplateStructure;
import io.github.crazy.lonewolf.coders.omnij.engine.repository.ClientRepository;
import io.github.crazy.lonewolf.coders.omnij.engine.repository.TemplateRepository;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.ITemplateContainer;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Resolution;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.ResolutionState;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.TemplateKey;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
public class TemplateResolutionService {

    private final ClientRepository clientRepository;
    private final TemplateRepository templateRepository;
    private final ITemplateContainer templateContainer;

    public TemplateResolutionService(
        ClientRepository clientRepository,
        TemplateRepository templateRepository,
        ITemplateContainer templateContainer
    ) {
        this.clientRepository = clientRepository;
        this.templateRepository = templateRepository;
        this.templateContainer = templateContainer;
    }

    @Transactional
    public void registerPendingDiscovery(
        String clientId,
        String appName,
        String className,
        String operation,
        String destinationTemplate
    ) {

        if (templateRepository.existsByClient_IdAndAppNameAndClassNameAndOperation(
                UUID.fromString(clientId), appName, className, operation)) {
            return; // Already queued
        }

       ClientEntity client = clientRepository.findById(UUID.fromString(clientId))
            .orElseGet(() -> {
                        ClientEntity e = new ClientEntity();
                        e.setId(UUID.fromString(clientId));
                        e.setClientName(clientId);
                        e.setActive(true);

                        clientRepository.save(e);
                        return e;
                    }

            );

        TemplateEntity template = TemplateEntity.builder()
            .client(client)
            .appName(appName)
            .className(className)
            .operation(operation)
            .status(ResolutionState.REVIEW_PENDING)
            .approved(false)
            .build();

        TemplateStructure structure = TemplateStructure.builder()
            .template(destinationTemplate)
            .approved(false)
            .build();

        template.setTemplateStructure(structure);

        try {
            templateRepository.saveAndFlush(template);
        } catch (DataIntegrityViolationException ignored) {
            // Safe idempotent guard if two Kafka consumers race on the same key
        }
    }

    @Transactional
    public void approveAndPublish(Long templateId, String resolvedJson) {
        TemplateEntity template = templateRepository.findWithStructureById(templateId)
            .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        template.setStatus(ResolutionState.SUCCESS);
        template.setApproved(true);

        TemplateStructure structure = template.getTemplateStructure();
        structure.setTemplate(resolvedJson);
        structure.setApproved(true);

        templateRepository.save(template);


        // Push to Redis only after the DB transaction commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                TemplateKey templateKey = new TemplateKey();
                templateKey.setClazz(template.getClassName());
                templateKey.setAppName(template.getAppName());
                templateKey.setClientId(template.getClient().getId().toString());

                Template info = new Template(template.getClassName(),template.getTemplateStructure().getTemplate(),template.getUpdatedAt(),true,null,null, Resolution.ENGINE,null,"");
                templateContainer.recordSuccess(templateKey,info);
            }
        });
    }
}