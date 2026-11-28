package io.github.crazy.lonewolf.sampleapp.db;

import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "clients")
@Getter
@Setter
@Builder
/**
 * Example Class to extend the ClientEntity. You can add more parameters to the entityClass to have more columns and relationships
 */
public class CustomClientForPlugin extends ClientEntity {




}
