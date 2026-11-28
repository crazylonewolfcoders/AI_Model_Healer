# OmniJ — Self-Healing, Deterministic Schema Mapping Engine for Spring Boot

**OmniJ** is a lightweight, format-extensible Spring Boot plugin and Maven build tool that eliminates brittle client-specific DTOs and manual payload onboarding.

Instead of maintaining dozens of custom adapters per upstream client—or incurring the latency, cost, and unpredictability of running LLMs on live production traffic—OmniJ combines **build-time schema reflection**, **sub-millisecond deterministic JSON Pointer mapping**, and a **zero-PHI self-healing resolution loop**.

---

## Key Features

* **Zero-Boilerplate Build Reflection (`omnij-maven-plugin`):** Scans annotated Java domain models during Maven build (`process-classes`) and automatically publishes canonical destination blueprints to PostgreSQL or `META-INF/omnij/`.
* **Isolated Dual-DataSource Persistence:** Runs inside its own dedicated PostgreSQL schema (`omnij`) and transaction manager (`omnijTransactionManager`) without colliding with your host application's primary `DataSource` or JPA entities.
* **Sub-Millisecond Runtime Transformation:** Evaluates RFC 6901 JSON Pointers backed by Redis (`ITemplateContainer`) and in-JVM single-flight request coalescing (`SingleFlightTemplateLoader`) to prevent cache stampedes under burst traffic.
* **Zero-PHI Structural Skeleton Extraction:** When an unknown or changed payload structure fails mapping, OmniJ strips all data values (`JsonSkeletonExtractor`) and dispatches *only* the structural skeleton to the Resolution Microservice—keeping sensitive PII/PHI out of logs, queues, and UIs.
* **Fail-Safe Onboarding State Machine:** Tracks per-client mapping states (`ACTIVE` vs. `PENDING_RESOLUTION`) so deactivated or unmapped clients fail fast rather than silently ingesting partially mapped data.
* **Zero-Endpoint Client Provisioning:** Authenticates upstream callers via `omnij-client-id` + SHA-256 hashed `omnij-api-key` headers and syncs non-sensitive client metadata asynchronously (`FOR UPDATE SKIP LOCKED`) with zero admin endpoints exposed on the host application.

---

##  Architecture Overview

```text
                       ┌──────────────────────────────────────────┐
                       │         Maven Build (Compile Time)       │
                       │  omnij-maven-plugin reflects @Model DTOs │
                       │  ──► Publishes canonical 1:1 blueprint   │
                       └────────────────────┬─────────────────────┘
                                            │
                                            ▼
┌──────────────┐   POST /orders    ┌────────────────────────────────────────────┐
│  Upstream    │ ─────────────────►│         Host Spring Boot Application       │
│  Client API  │  omnij-client-id  │                                            │
│              │  omnij-api-key    │  1. Authenticate client (SHA-256 hash)     │
└──────────────┘                   │  2. Check Redis cache (Single-Flight)      │
                                   │  3. Check client_templates state (DB)      │
                                   │  4. Apply deterministic JSON Pointer rules │
                                   └─────────┬────────────────────────┬─────────┘
                                             │                        │
                                    [Success]│               [Mapping Failed]
                                             ▼                        │
                                   ┌──────────────────┐               ▼
                                   │ @RestController  │   ┌──────────────────────┐
                                   │  Receives Typed  │   │ JsonSkeletonExtractor│
                                   │   Java Object    │   │ (Strips all values)  │
                                   └──────────────────┘   └──────────┬───────────┘
                                                                     │ Async (@HttpExchange)
                                                                     ▼
                                                          ┌──────────────────────┐
                                                          │ Resolution Service & │
                                                          │     Angular UI       │
                                                          │ (Maps & pushes rule  │
                                                          │    back to Redis)    │
                                                          └──────────────────────┘
```

---

## Quick Start

### 1. Configure the Maven Plugin (`pom.xml`)
Add `omnij-maven-plugin` to your host microservice so canonical destination blueprints are generated automatically during build:

```xml
<plugin>
    <groupId>io.github.crazy.lonewolf.coders</groupId>
    <artifactId>omnij-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <basePackagesLocation>com.example.app.model</basePackagesLocation>
        <action>db</action>
        <jdbcUrl>\${env.OMNIJ_DB_URL}</jdbcUrl>
        <username>\${env.DB_USER}</username>
        <password>\${env.DB_PASS}</password>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>omnij</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 2. Configure `application.yml`
Configure your primary application database alongside OmniJ's isolated datasource, Redis cache, and Resolver Microservice connection:

```yaml
spring:
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:postgresql://localhost:5432/samplewebapp
    username: \${DB_USER}
    password: \${DB_PASS}
  jpa:
    hibernate:
      ddl-auto: update
  data:
    redis:
      host: localhost
      port: 6379

omnij:
  datasource:
    url: jdbc:postgresql://localhost:5432/omnij_plugin
    username: \${DB_USER}
    password: \${DB_PASS}
  engine:
    enable-auto-healing: true
    enable-mandatory-checks: true
  resolver:
    base-url: http://localhost:8081
    api-key: \${OMNIJ_RESOLVER_API_KEY}
    api-secret: \${OMNIJ_RESOLVER_SECRET}
```

### 3. Enable OmniJ in Your Spring Boot Application
Import `OmniJDataSourceConfig` (and `WebAppPrimaryJpaConfig` if your host app relies on Spring Boot's default primary `DataSource`):

```java
@SpringBootApplication(
    scanBasePackages = {
        "com.example.app",
        "io.github.crazy.lonewolf.coders.omnij.plugin.memory"
    },
    scanBasePackageClasses = {
        OmniJDataSourceConfig.class,
        WebAppPrimaryJpaConfig.class
    }
)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 4. Annotate Your Controller Parameter
Use your custom `@GenerateJson` (or OmniJ-resolved) parameter directly in any Spring MVC `@RestController`:

```java
@PostMapping("/api/v1/orders")
public ResponseEntity<OrderResponse> createOrder(@GenerateJson OrderRequest order) {
    return ResponseEntity.ok(orderService.process(order));
}
```

---

## Client Provisioning & Security

* **No Exposed Admin Endpoints:** Host applications expose zero client-management REST endpoints.
* **One-Way SHA-256 Credential Hashing:** Upstream clients pass their raw API key in the `omnij-api-key` header alongside `omnij-client-id` over HTTPS. OmniJ hashes the incoming key in memory (`SHA-256`) and verifies it against `omnij.client_def`.
* **Safe Metadata Sync:** A background worker (`ClientMetadataSyncWorker`) uses PostgreSQL `FOR UPDATE SKIP LOCKED` to sync non-sensitive client metadata (`id`, `clientName`, `active`) to the Resolution Microservice—never transmitting hashed or raw API keys over the network.

---

##  Roadmap

* [x] Build-time reflection & canonical schema generation (`omnij-maven-plugin`)
* [x] Isolated dual-datasource JPA & schema management (`omnij` schema)
* [x] Single-flight Redis template cache & state-machine argument resolver
* [x] Zero-PHI JSON skeleton extraction & async failure reporting
* [ ] AI-Assisted Mapping (Zero-PHI, Resolution-Time Only):
    * Automatically pre-map unmapped source skeletons to canonical destination blueprints using heuristic name matching + LLM structured outputs inside the Resolver Microservice.
    * Operates strictly on sanitized structural skeletons (zero PII/PHI exposure) and requires human-in-the-loop approval in the Angular UI before compiling into deterministic runtime rules.
   
* [ ] Resolution Microservice REST API & `pac4j` authentication integration
* [ ] Angular visual mapping workspace with AI-assisted field suggestions
* [ ] Wildcard array mapping (`/items[*]`) and pluggable XML / HL7 inbound parsers

---

##  Tech Stack

* **Java 21+ / Virtual Threads**
* **Spring Boot 4** (Spring MVC, Spring Data JPA, Spring Data Redis, `@HttpExchange`)
* **Hibernate 7 & PostgreSQL** (`jsonb` storage, isolated schemas, `SKIP LOCKED`)
* **Jackson** (JSON Pointer evaluation & structural skeleton extraction)


## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

Copyright (c) 2026 Sarath (crazylonewolfcoder)