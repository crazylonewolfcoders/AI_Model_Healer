package io.github.crazy.lonewolf.coders.omnij.datasource.repository;

import io.github.crazy.lonewolf.coders.omnij.datasource.db.OmniJTemplateEnitty;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.OmnijClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<OmniJTemplateEnitty, String> {


}
