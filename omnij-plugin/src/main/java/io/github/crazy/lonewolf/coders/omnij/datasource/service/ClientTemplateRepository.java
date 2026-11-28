package io.github.crazy.lonewolf.coders.omnij.datasource.service;

import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientTemplateEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientTemplateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientTemplateRepository extends JpaRepository<ClientTemplateEntity, ClientTemplateId> {

    Optional<ClientTemplateEntity> findByIdAndActiveTrue(ClientTemplateId id);

    List<ClientTemplateEntity> findByIdClientId(String clientId);

    @Modifying
    @Query("UPDATE ClientTemplateEntity c SET c.active = false WHERE c.id.appName = :appName AND c.id.className = :className")
    int deactivateAllForClass(@Param("appName") String appName, @Param("className") String className);
}
