package io.github.crazy.lonewolf.coders.omnij.engine.repository;

import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {



}