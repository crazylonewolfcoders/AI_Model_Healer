package io.github.crazy.lonewolf.coders.omnij.engine.db;

import io.github.crazy.lonewolf.coders.authentication.entity.BaseEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.ResolutionState;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "template",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_client_template_scope",
            columnNames = {"client_id",  "app_name", "class_name", "operation"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "client_id", referencedColumnName = "client_id", nullable = false)
    })
    private ClientEntity client;

    @Column(name = "app_name", nullable = false, length = 120)
    private String appName;

    @Column(name = "class_name", nullable = false, length = 255)
    private String className;

    @Column(name = "operation", nullable = false, length = 100)
    private String operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ResolutionState status = ResolutionState.REVIEW_PENDING;

    @Column(name = "is_approved", nullable = false)
    private boolean approved;

    @OneToOne(mappedBy = "templateEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TemplateStructure templateStructure;


}