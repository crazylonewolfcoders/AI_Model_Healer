# OmniJ

**Zero-PHI, Compile-Time JSON-to-POJO Integration Framework for Enterprise Spring Boot Applications**

OmniJ minimizes manual JSON mapping code and deployment bottlenecks when integrating external B2B partners, clinics, labs, and financial institutions. Instead of writing custom DTOs and mappers for every partner's payload variation—or routing sensitive data through third-party cloud iPaaS tools—OmniJ parses payloads directly inside your host application's JVM while offloading unknown schema resolution to a decoupled UI and AI-assisted control plane.

---

## Core Architecture

OmniJ separates **high-throughput runtime execution** from **schema discovery and resolution**:

```text
┌────────────────────────────────────────────────────────────────────────────┐
│                        HOST SPRING BOOT APPLICATION                        │
│                                                                            │
│  Incoming JSON ──► OmniJ Runtime Parser ──► Target Java POJO               │
│       │                    │                                               │
│       │ (Cache Miss)       ▼                                               │
│       │           ITemplateContainer (In-Memory / Redis Hash)              │
│       ▼                                                                    │
│  Value-Stripped Skeleton + Compiled .oinf Target Schema                    │
└───────────────────────────────┬────────────────────────────────────────────┘
                                │ Single Discovery Event (Zero PHI)
                                ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                     OMNIJ RESOLUTION MICROSERVICE                          │
│                                                                            │
│  Discovery Ingest ──► IResolutionStrategy (AI / Heuristic / Custom)        │
│                                │                                           │
│                                ▼                                           │
│                     Angular Mapping & Approval UI                          │
│                                │ (Approve & Publish)                       │
│                                ▼                                           │
│               Promotes Approved Template to Redis Hash ("success")         │
└────────────────────────────────────────────────────────────────────────────┘
```

### Key Architectural Principles

* **Zero-PHI Control Plane:** Live business and patient payloads stay strictly inside the host application's JVM. When an unmapped schema arrives, OmniJ strips all values (emitting only structural type tokens) alongside the compile-time `.oinf` target definition before notifying the Resolution Microservice.
* **Compile-Time `.oinf` Generation:** An annotation processor inspects target POJOs during compilation and generates deterministic JSON Pointer templates (`.oinf`), avoiding runtime reflection overhead and classpath-scanning issues.
* **Pluggable Storage & Provisioning (`ITemplateContainer`):** State management (`success`, `failure`, `pending`) is abstracted behind a storage-agnostic strategy interface supporting local `ConcurrentHashMap` execution, multi-pod Redis Hashes, or custom enterprise stores.
* **Resilient Fail-Safe Boundaries:** If external cache infrastructure is unreachable at startup, `PluginConfig` degrades gracefully to a local in-memory circuit-breaker container so host services stay online.
* **Bring Your Own Infrastructure (BYO-Infra):** Works with your existing Spring Boot stack, database, Redis cluster, and transport layer (HTTP, Redis Streams, or Kafka) without forcing a rigid infrastructure footprint.

---

## Module Overview

| Module | Responsibility |
| :--- | :--- |
| **`omnij-compiler` / Annotation Processor** | Generates `.oinf` JSON Pointer schemas from annotated target POJOs at build time. |
| **`omnij-plugin`** | Runtime Spring Boot starter providing JSON Pointer parsing, `ITemplateContainer` state lifecycle, `ITemplateProvisionStrategy`, and value-stripped schema discovery. |
| **`omnij-utils` / Common Contracts** | Shared SPI definitions (`Template`, `TemplateInfo`, `IResolutionStrategy`, `RedisKeySerializer`) across host apps and control-plane services. |
| **`omnij-resolution-engine`** | Control-plane microservice and extensible Spring Boot core that ingests discovery skeletons, runs AI/custom resolution strategies, and publishes approved mappings. |
| **`omnij-resolution-ui`** | Angular web interface for side-by-side schema inspection, AI auto-suggestion review, and one-click template approval. |

---

## Quick Start: Host Application Integration

### 1. Define Your Target POJO

Annotate your domain models so the OmniJ compiler plugin generates the baseline `.oinf` JSON Pointer template during build:

```java
package com.example.billing.model;

import lombok.Data;

@Data
public class CostAnalysis {
    private String id;
    private String name;
    private Double cost;
    private String date;
    private CostDetails costDetails;
}
```

At compile time, OmniJ generates `CostAnalysis.oinf`:

```json
{
  "id": "/id",
  "name": "/name",
  "cost": "/cost",
  "date": "/date",
  "costDetails": {
    "dept": "/costDetails/dept",
    "startDate": "/costDetails/startDate",
    "endDate": "/costDetails/endDate"
  }
}
```

### 2. Configure `application.yml`

Enable OmniJ engine features and specify whether to use the built-in in-memory container or a custom/Redis storage strategy:

```yaml
omnij:
  engine:
    enableAutoHealing: true
  jsonvalidations:
    enableMandatoryChecks: true
  memoryContainer:
    useCustomContainer: true
```

### 3. Implement `ITemplateProvisionStrategy` (For Multi-Pod / Redis Deployments)

Provide a Spring `@Configuration` bean implementing `ITemplateProvisionStrategy` to connect OmniJ to your enterprise Redis cluster:

```java
@Configuration
public class RedisStorageStrategy implements ITemplateProvisionStrategy {

    private final RedisConnectionFactory connectionFactory;

    public RedisStorageStrategy(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public ITemplateContainer provisionContainer() {
        return new RedisTemplateContainer(connectionFactory, "billing-service");
    }
}
```

---

## State Lifecycle & Redis Hash Contract

For each unique `(appName, clientId, upstreamKey, className, operation)` tuple, `RedisTemplateContainer` maintains a single Redis Hash with three mutually exclusive lifecycle fields:

| Hash Field | Meaning | Runtime Behavior |
| :--- | :--- | :--- |
| **`success`** | Active, verified JSON Pointer mapping exists. | Parses incoming JSON directly into the target Java POJO in-JVM. |
| **`pending`** | Unmapped schema discovered; awaiting review in Resolution UI. | Emits a single discovery event and short-circuits duplicate requests until resolved. |
| **`failure`** | Schema mismatch or validation failure recorded. | Prevents repeated parse errors and surfaces the broken contract for review. |

When an engineer approves a mapping in the Resolution UI, the Resolution Microservice writes the finalized template to the **`success`** field and deletes **`pending`** and **`failure`**, allowing all host application pods to pick up the new mapping immediately without restarts.

---
## Communication Between Resolution Engine & Source Application (`IResolutionStrategy`)

`IResolutionStrategy` defines the communication contract between the **OmniJ Resolution Engine** and the **source (host) application** during schema discovery and template lifecycle updates. Implementing this interface lets enterprise teams integrate their own transport layer (such as REST/HTTP, Kafka, or Redis Streams) to exchange value-stripped discovery events and finalized `.oinf` mappings:

```java
@Component
public class CustomEngineCommunicationStrategy implements IResolutionStrategy {

    @Override
    public String resolveCandidate(TemplateEntity templateEntity, String inputSkeletonJson) {
        // Transmit the value-stripped inputSkeletonJson and template metadata
        // between the source application and the OmniJ Resolution Engine
        // over your enterprise transport layer (e.g., HTTP, Kafka, Redis Streams)
        return resolutionResponseJson;
    }
}
```

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

Copyright (c) 2026 Sarath (crazylonewolfcoder)