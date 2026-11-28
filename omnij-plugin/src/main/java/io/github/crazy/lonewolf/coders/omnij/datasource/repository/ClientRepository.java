package io.github.crazy.lonewolf.coders.omnij.datasource.repository;

import io.github.crazy.lonewolf.coders.omnij.plugin.entities.OmnijClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ClientRepository extends JpaRepository<OmnijClientEntity, UUID> {

    List<OmnijClientEntity> findBySyncedToResolverFalse();


    @Query(
            value = "SELECT * FROM omnij.client_def WHERE synced_to_resolver = false LIMIT 50 FOR UPDATE SKIP LOCKED",
            nativeQuery = true
    )
    List<OmnijClientEntity> findUnsyncedClientsForUpdate();

    Optional<OmnijClientEntity> findByClientid(String clientid);

    Optional<OmnijClientEntity> findByClientidAndClientKeyAndActiveTrue(String clientid, String clientKey);

    Optional<OmnijClientEntity> findByIdAndClientKeyAndActiveTrue(UUID id, String clientKey);
}
