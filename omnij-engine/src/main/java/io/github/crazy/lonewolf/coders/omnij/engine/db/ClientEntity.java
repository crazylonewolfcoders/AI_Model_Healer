package io.github.crazy.lonewolf.coders.omnij.engine.db;

import io.github.crazy.lonewolf.coders.authentication.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientEntity extends BaseEntity {

    @EmbeddedId
    private ClientId id;

    @Column(name = "client_name", length = 200)
    private String clientName;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateEntity> templates = new ArrayList<>();
}