package io.github.crazy.lonewolf.coders.omnij.plugin.entities;

import io.github.crazy.lonewolf.coders.authentication.entity.DateEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
@Getter
@Setter
public class ClientEntity extends DateEntity {

    @Id
    @Column(name = "client_id")
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name="clientid" , nullable = false)
    private String clientid;

    @Column(name = "client_name", length = 200)
    private String clientName;

    @Column(name="upstreamkey")
    private String clientKey;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "synced_to_resolver", nullable = false)
    private boolean syncedToResolver = false;



}