package io.github.crazy.lonewolf.coders.omnij.engine.repository;

import io.github.crazy.lonewolf.coders.omnij.engine.db.ClientEntity;
import io.github.crazy.lonewolf.coders.omnij.engine.db.ClientId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<ClientEntity, ClientId> {
}