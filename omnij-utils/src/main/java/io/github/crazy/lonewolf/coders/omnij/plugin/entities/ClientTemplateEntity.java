package io.github.crazy.lonewolf.coders.omnij.plugin.entities;

import io.github.crazy.lonewolf.coders.authentication.entity.DateEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "client_templates", schema = "omnij")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientTemplateEntity extends DateEntity {

    @EmbeddedId
    private ClientTemplateId id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mapping_json", columnDefinition = "jsonb")
    private String mappingJson;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = false;
}
