package io.github.crazy.lonewolf.coders.omnij.engine.repository;

import io.github.crazy.lonewolf.coders.omnij.engine.db.ClientId;
import io.github.crazy.lonewolf.coders.omnij.engine.db.TemplateEntity;
import io.github.crazy.lonewolf.coders.omnij.engine.db.TemplateQueueItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<TemplateEntity, Long> {

    // Lightweight queue query for Angular UI (skips loading large template_json)
    @Query("""
        SELECT new io.github.crazy.lonewolf.coders.omnij.engine.db.TemplateQueueItem(
            t.id,
            t.client.id.clientId,
            t.appName,
            t.className,
            t.operation,
            t.status,
            t.approved
        )
        FROM TemplateEntity t
        WHERE t.status = :status
        ORDER BY t.id DESC
    """)
    List<TemplateQueueItem> findQueueByStatus(@Param("status") ResolutionStatus status);

    // Loads parent + 1:1 TemplateStructure in a single JOIN when opening the editor or publishing
    @EntityGraph(attributePaths = {"client", "templateStructure"})
    Optional<TemplateEntity> findWithStructureById(Long id);

    boolean existsByClient_IdAndAppNameAndClassNameAndOperation(
            ClientId clientId, String appName, String className, String operation
    );
}