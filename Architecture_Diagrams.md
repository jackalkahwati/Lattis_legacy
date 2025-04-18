# Core Product Architecture Diagrams - Lattis Dashboard and Apps

This document provides architecture diagrams for the core product. The diagrams below illustrate the key components:

- **Backend (APIs):** Powered by `lattis-platform`, which provides core APIs and business logic.
- **Website/Frontend:** `lattis-dashboard` is the web-based administrative interface.
- **Mobile Apps:** Includes consumer and operator apps for both iOS and Android:
   - **Consumer Apps:** `lattis_ios` (iOS) and `lattis_android` (Android)
   - **Operator Apps:** `operator-ios` (iOS) and `operator-android` (Android)
- **SDKs:** `lattis_sdk_ios` and `lattis_sdk_ios_public`, offering integration capabilities for external developers.

Below is a Mermaid diagram that visualizes these components and their connections:

```mermaid
flowchart LR
    %% Backend
    A[Lattis Platform\n(Backend & APIs)]

    %% Website/Frontend
    B[Lattis Dashboard\n(Website/Frontend)]

    %% Mobile Apps
    subgraph Mobile_Apps [Mobile Applications]
      C[Lattis iOS\n(Consumer)]
      D[Operator iOS]
      E[Lattis Android\n(Consumer)]
      F[Operator Android]
    end

    %% SDKs
    subgraph SDKs [SDKs]
      G[Lattis SDK iOS]
      H[Lattis SDK iOS Public]
    end

    %% Connections
    A -->|Exposes APIs| B
    A -->|Exposes APIs| C
    A -->|Exposes APIs| D
    A -->|Exposes APIs| E
    A -->|Exposes APIs| F
    A -->|Exposes APIs| G
    A -->|Exposes APIs| H

    %% Additional labels
    B --- A
    C --- A
    D --- A
    E --- A
    F --- A
```

### Explanation

- The **Lattis Platform** serves as the backbone of the system, offering RESTful APIs and core business logic.
- The **Lattis Dashboard** is the administrative web interface that communicates with the backend to manage the system.
- **Mobile apps** (both consumer and operator) interact with the backend via the same APIs, ensuring consistent data and operations.
- **SDKs** provide a way for third-party developers to integrate and extend Lattis functionalities into their own applications.

This architecture diagram reflects the core product being sold: a fully integrated solution including a backend, web dashboard, mobile applications, and SDKs. 