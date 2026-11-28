package io.github.crazy.lonewolf.coders.omnij.datasource.db;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class OmniJHibernateBuildManager implements AutoCloseable {

    private final StandardServiceRegistry registry;
    private final SessionFactory sessionFactory;

    public OmniJHibernateBuildManager(String jdbcUrl, String username, String password) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.connection.url", jdbcUrl);
        settings.put("hibernate.connection.username", username);
        settings.put("hibernate.connection.password", password);
        settings.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        settings.put("hibernate.default_schema", "omnij");

        // Automatically creates/updates the table if missing!
        settings.put("hibernate.hbm2ddl.auto", "update");
        settings.put("hibernate.show_sql", "false");

        this.registry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        try {
            this.sessionFactory = new MetadataSources(this.registry)
                    .addAnnotatedClass(OmniJTemplateEnitty.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(this.registry);
            throw e;
        }
    }

    public void saveOrUpdateTemplate(String className, String appName, String jsonContent) {
        sessionFactory.inTransaction(session -> {
            OmniJTemplateEnitty entity = session.createQuery(
                            "FROM OmniJTemplateEnitty t WHERE t.className = :className AND t.appName = :appName",
                            OmniJTemplateEnitty.class)
                    .setParameter("className", className)
                    .setParameter("appName", appName)
                    .uniqueResultOptional()
                    .orElseGet(() -> {
                        OmniJTemplateEnitty fresh = new OmniJTemplateEnitty();
                        fresh.setClassName(className);
                        fresh.setAppName(appName);
                        return fresh;
                    });



            entity.setDestinationTemplate(jsonContent);
            entity.setUpdatedAt(Instant.now());
            session.persist(entity);
        });
    }

    @Override
    public void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
