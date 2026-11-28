package io.github.crazy.lonewolf.coders.omnij.datasource;

import com.zaxxer.hikari.HikariDataSource;
import io.github.crazy.lonewolf.coders.omnij.datasource.db.OmniJTemplateEnitty;
import io.github.crazy.lonewolf.coders.omnij.datasource.repository.ClientRepository;
import io.github.crazy.lonewolf.coders.omnij.datasource.repository.TemplateRepository;
import io.github.crazy.lonewolf.coders.omnij.datasource.service.*;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientTemplateEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientTemplateId;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.OmnijClientEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.client.ClientMetadataSyncWorker;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;
@Configuration
@ConditionalOnProperty(prefix = "omnij.datasource", name = "url")
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "omnijEntityManagerFactory",
        transactionManagerRef = "omnijTransactionManager"
)
public class OmniJDataSourceConfig {

    @Bean(name = "omnijDataSourceProperties")
    @ConfigurationProperties("omnij.datasource")
    public DataSourceProperties omnijDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "omnijDataSource")
    public DataSource omnijDataSource(
            @Qualifier("omnijDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "omnijEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean omnijEntityManagerFactory(
            @Qualifier("omnijDataSource") DataSource dataSource) {

        Map<String, Object> properties = Map.of(
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.jdbc.time_zone", "UTC",
                "hibernate.default_schema", "omnij",
                "hibernate.hbm2ddl.create_namespaces", "true"
        );

        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(
                new HibernateJpaVendorAdapter(),
                ds -> properties,
                null
        );

        return builder
                .dataSource(dataSource)
                .packages(OmnijClientEntity.class
                        , OmniJTemplateEnitty.class
                        , ClientMetadataSyncWorker.class
                        , ClientRepository.class
                        , TemplateRepository.class
                        , ClientService.class
                        , TemplateService.class
                        , SingleFlightTemplateLoader.class
                        , ClientTemplateEntity.class
                        , ClientTemplateRepository.class
                        , ClientTemplateService.class
                        , ClientTemplateId.class
                )
                .persistenceUnit("omnijPU")
                .build();
    }

    @Bean(name = "omnijTransactionManager")
    public PlatformTransactionManager omnijTransactionManager(
            @Qualifier("omnijEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}

