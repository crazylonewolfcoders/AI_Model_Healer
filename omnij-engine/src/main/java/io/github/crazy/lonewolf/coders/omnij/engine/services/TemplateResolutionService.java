package io.github.crazy.lonewolf.coders.omnij.engine.services;

import io.github.crazy.lonewolf.coders.omnij.engine.db.*;
import io.github.crazy.lonewolf.coders.omnij.engine.repository.ClientRepository;
import io.github.crazy.lonewolf.coders.omnij.engine.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TemplateResolutionService {

    private final ClientRepository clientRepository;
    private final TemplateRepository templateRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${adaptive.redis.key-prefix:adp:v1:}")
    private String keyPrefix;

    @Value("${adaptive.redis.eviction-channel:adp:v1:evict}")
    private String evictionChannel;

    public TemplateResolutionService(
        ClientRepository clientRepository,
        TemplateRepository templateRepository,
        StringRedisTemplate redisTemplate
    ) {
        this.clientRepository = clientRepository;
        this.templateRepository = templateRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void registerPendingDiscovery(
        String clientId,
        String upstreamKey,
        String appName,
        String className,
        String operation,
        String starterTemplateJson
    ) {
        ClientId compositeClientId = new ClientId(clientId);

        if (templateRepository.existsByClient_IdAndAppNameAndClassNameAndOperation(
            compositeClientId, appName, className, operation)) {
            return; // Already queued
        }

        ClientEntity client = clientRepository.findById(compositeClientId)
            .orElseGet(() -> clientRepository.save(
                ClientEntity.builder()
                    .id(compositeClientId)
                    .clientName(clientId)
                    .active(true)
                    .build()
            ));

        TemplateEntity template = TemplateEntity.builder()
            .client(client)
            .appName(appName)
            .className(className)
            .operation(operation)
            .status(ResolutionStatus.RESOLUTION_PENDING)
            .approved(false)
            .build();

        TemplateStructure structure = TemplateStructure.builder()
            .template(starterTemplateJson)
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

        template.setStatus(ResolutionStatus.APPROVED);
        template.setApproved(true);

        TemplateStructure structure = template.getTemplateStructure();
        structure.setTemplate(resolvedJson);
        structure.setApproved(true);

        templateRepository.save(template);

        String compositeKey = String.join(":",
            template.getAppName(),
            template.getClient().getId().getClientId(),
            template.getClassName(),
            template.getOperation()
        );

        // Push to Redis only after the DB transaction commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisTemplate.opsForValue().set(keyPrefix + "template:" + compositeKey, resolvedJson);
                redisTemplate.delete(keyPrefix + "pending:" + compositeKey);
                redisTemplate.convertAndSend(evictionChannel, compositeKey);
            }
        });
    }
}