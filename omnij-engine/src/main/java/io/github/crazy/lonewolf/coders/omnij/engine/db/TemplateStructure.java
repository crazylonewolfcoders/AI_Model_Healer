package io.github.crazy.lonewolf.coders.omnij.engine.db;

import io.github.crazy.lonewolf.coders.authentication.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="templateStucture")
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
public class TemplateStructure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;


    private String template;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private TemplateEntity TemplateEntity;

    private boolean approved;






}
