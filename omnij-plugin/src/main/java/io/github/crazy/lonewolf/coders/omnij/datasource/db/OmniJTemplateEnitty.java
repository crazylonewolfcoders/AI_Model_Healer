package io.github.crazy.lonewolf.coders.omnij.datasource.db;

import io.github.crazy.lonewolf.coders.authentication.entity.DateEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "template_definitions", schema = "omnij" )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OmniJTemplateEnitty extends DateEntity {

    @Id
    @Column(name = "class_name", nullable = false, length = 255)
    private String className;


    @Column(name = "app_name", nullable = false, length = 120)
    private String appName;

    @Column(name = "operation", length = 100)
    private String operation;

    @Column(name = "is_approved", nullable = false)
    private boolean approved = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "destination_template", columnDefinition = "jsonb", nullable = false)
    private String destinationTemplate;


}
