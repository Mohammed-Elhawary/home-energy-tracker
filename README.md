# Home Energy Tracker

A microservices-based **Home Energy Tracker** application built with **Spring Boot 4.1**, **Java 21**, and a streaming architecture powered by **Apache Kafka**. The system ingests energy usage data from smart devices, stores historical usage in **InfluxDB**, manages users and devices in **MariaDB**, evaluates consumption thresholds, sends email alerts, and generates AI-powered energy-saving recommendations via **NVIDIA NIM (z-ai/glm-5.3)**.

## Table of Contents

- [Architecture](#architecture)
- [Services](#services)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Kafka Topics](#kafka-topics)
- [Database Schema](#database-schema)
- [Monitoring & Observability](#monitoring--observability)
- [Configuration](#configuration)
- [Building & Running](#building--running)
- [Testing](#testing)

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            API Gateway (9000)                            │
│                              Spring Cloud Gateway                       │
│                     OAuth2 / JWT (Keycloak) • Circuit Breaker           │
└────┬────────┬────────┬──────────┬───────────┬─────────────┬─────────────┘
     │        │        │          │           │             │
     ▼        ▼        ▼          ▼           ▼             ▼
  User     Device   Ingestion  Usage        Alert        Insight
  (8080)   (8081)   (8082)      (8083)       (8084)       (8085)
     │        │        │          │           │             │
     │        │        ▼          │           │             │
     │        │    Kafka Topic:   │           │             │
     │        │    energy-usage   │           │             │
     │        │        │          │           │             │
     │        │        ▼          │           │             │
     │        │   Usage Service   │           │             │
     │        │   ┌───────────┐   │           │             │
     │        │   │ InfluxDB  │   │           │             │
     │        │   │ (time-    │   │           │             │
     │        │   │  series)  │   │           │             │
     │        │   └────┬──────┘   │           │             │
     │        │        │          │           │             │
     │        │        │    Kafka Topic:     │             │
     │        │        │    energy-alerts    │             │
     │        │        │        │            │             │
     │        │        │        ▼            ▼             │
     │        │        │   Alert Service   Email (Mailpit)│
     │        │        │   ┌───────────┐   │             │
     │        │        │   │  MariaDB  │   │             │
     │        │        │   │ (alerts)  │   │             │
     │        │        │   └───────────┘   │             │
     │        │        │          │        │             │
     │        │        │          │   Usage API  ────────┼─────┐
     │        │        │          │        │             │     │
     │        │        │          │        ▼             │     ▼
     │        │        │          │  Insight Service    AI (NVIDIA NIM)
     │        │        │          │  ┌────────────────┐  │     │
     │        │        │          │  │ z-ai/glm-5.3   │  │     │
     │        │        │          │  │ (Spring AI)    │  │     │
     │        │        │          │  └────────────────┘  │     │
     │        │        │          │          │           │     │
     └────────┴────────┴──────────┴──────────┴───────────┴─────┘
     MariaDB (users, devices, alerts)   Prometheus + Grafana
```

### Data Flow

1. **Data Ingestion** — `ingestion-service` accepts energy usage readings via HTTP and publishes them to the `energy-usage` Kafka topic.
2. **Usage Processing** — `usage-service` consumes from `energy-usage`, writes time-series data to **InfluxDB**, and every 10 seconds runs an aggregation job that evaluates each user's hourly consumption against their configured threshold.
3. **Alerting** — When a user's threshold is exceeded, `usage-service` publishes an `AlertingEvent` to the `energy-alerts` Kafka topic. `alert-service` consumes these events and sends email notifications via **Mailpit** (SMTP).
4. **Insights** — `insight-service` queries usage data from the usage-service API and leverages **Spring AI** with an NVIDIA NIM GLM model to generate personalized energy-saving tips and consumption overviews.
5. **API Gateway** — All external traffic is routed through the Spring Cloud Gateway, which provides JWT-based authentication (via Keycloak), circuit breakers (Resilience4j), and a centralized Swagger/OpenAPI UI.

## Services

| Service | Port | Description |
|---------|------|-------------|
| **api-gateway** | 9000 | Entry point; routes requests, JWT auth (Keycloak), circuit breakers, Swagger UI |
| **User-service** | 8080 | Manages users (CRUD) with MariaDB + Flyway migrations |
| **device_service** | 8081 | Manages smart devices (CRUD) associated with users |
| **ingestion-service** | 8082 | Accepts energy usage readings and publishes to Kafka (`energy-usage`) |
| **usage-service** | 8083 | Consumes `energy-usage`, writes to InfluxDB, aggregates hourly, triggers alerts |
| **alert-service** | 8084 | Consumes `energy-alerts` from Kafka and sends threshold breach emails |
| **insight-service** | 8085 | Generates AI-powered energy-saving tips and usage overviews via NVIDIA NIM GLM |

## Technology Stack

| Category | Technology |
|----------|------------|
| **Language & Runtime** | Java 21, Spring Boot 4.1.0 |
| **Build Tool** | Apache Maven (wrapper included) |
| **Microservices Framework** | Spring Boot, Spring Cloud Gateway, Resilience4j |
| **Messaging** | Apache Kafka |
| **Databases** | MariaDB (users, devices, alerts), InfluxDB v2 (time-series energy data) |
| **Authentication** | Keycloak (OpenID Connect / OAuth2 JWT) |
| **AI** | Spring AI, NVIDIA NIM (z-ai/glm-5.3) |
| **Mail** | Spring Mail via Mailpit (SMTP sandbox) |
| **Monitoring** | Micrometer, Prometheus, Grafana |
| **Documentation** | SpringDoc OpenAPI 3.1.0 (Swagger UI) |
| **Containerization** | Docker, Docker Compose |
| **Testing** | Spring Boot Test, Testcontainers, JUnit 5 |

## Project Structure

```
home-energy-tracker/
├── pom.xml                              # Parent POM (Spring Boot 4.1.0, Java 21)
├── docker-compose.yml                   # All infra services (MariaDB, Kafka, InfluxDB, Keycloak, etc.)
├── docker/
│   ├── mysql/init.sql                   # MariaDB initialization script
│   ├── keycloak/realms/                 # Keycloak realm configuration
│   ├── prometheus/prometheus.yml        # Prometheus scrape configuration
│   ├── grafana/provisioning/            # Grafana dashboards & datasource provisioning
│   ├── influxdb_data/                   # InfluxDB persistent data
│   ├── kafka_data/                      # Kafka persistent data
│   └── (volumes: db_data, mysql_keycloak_data, etc.)
├── api-gateway/                         # API Gateway service (port 9000)
│   └── src/main/java/.../
│       ├── routes/                      # Route definitions for each service
│       ├── config/                      # SecurityConfig, FallbackConfig
│       └── ApiGetwayApplication.java
├── User-service/                        # User management service (port 8080)
│   └── src/main/resources/
│       └── db/migration/                # Flyway migrations (V1, V2, V3)
├── device_service/                      # Device management service (port 8081)
├── ingestion-service/                   # Data ingestion service (port 8082)
├── usage-service/                       # Usage processing & aggregation (port 8083)
├── alert-service/                       # Alert/email notification service (port 8084)
├── insight-service/                     # AI insights service (port 8085)
├── .vscode/                             # VS Code Java debug & formatter config
└── .github/                             # GitHub modernize workflows
```

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ (or use the included Maven Wrapper) |
| Docker | 24+ |
| Docker Compose | 2.20+ |
| Git | any |

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd home-energy-tracker
```

### 2. Create an `.env` File

Create a `.env` file in the project root:

```env
MARIADB_ROOT_PASSWORD=your_password
MARIADB_DATABASE=home_energy_tracker
MARIADB_USER=your_user
MARIADB_PASSWORD=your_password
```

### 3. Start Infrastructure Services

```bash
docker-compose up -d
```

This brings up:

| Service | Container | Port | Purpose |
|---------|-----------|------|---------|
| MariaDB | `maria-db` | 3307 | Primary database (users, devices, alerts) |
| Kafka | `kafka` | 9092 / 9094 | Message broker for energy events |
| Kafka UI | `kafka-ui` | 8070 | Web UI for Kafka topic inspection |
| InfluxDB | `influxdb` | 8072 | Time-series storage for energy usage |
| Mailpit | `mailpit` | 8025 / 1025 | Mock SMTP server for email testing |
| Keycloak | `keycloak` | 8091 | Identity & access management (OIDC) |
| Prometheus | `prometheus` | 9090 | Metrics collection |
| Grafana | `grafana` | 3000 | Metrics dashboard |

### 4. Build the Services

```bash
./mvnw clean install -DskipTests
```

### 5. Run the Services

Run the services in any order (API Gateway depends on the backend services being up):

```bash
# User Service (8080)
./mvnw -pl User-service spring-boot:run

# Device Service (8081)
./mvnw -pl device_service spring-boot:run

# Ingestion Service (8082)
./mvnw -pl ingestion-service spring-boot:run

# Usage Service (8083)
./mvnw -pl usage-service spring-boot:run

# Alert Service (8084)
./mvnw -pl alert-service spring-boot:run

# Insight Service (8085)
./mvnw -pl insight-service spring-boot:run

# API Gateway (9000)
./mvnw -pl api-gateway spring-boot:run
```

> Use VS Code launch configurations (see `.vscode/launch.json`) for debugging individual services.

### 6. Access the Application

| URL | Service | Description |
|-----|---------|-------------|
| `http://localhost:9000` | API Gateway | Main entry point (secured) |
| `http://localhost:9000/swagger-ui.html` | API Gateway | Centralized Swagger UI for all services |
| `http://localhost:8070` | Kafka UI | Kafka topic inspection |
| `http://localhost:8072` | InfluxDB | Time-series database UI |
| `http://localhost:8091` | Keycloak | Identity provider admin console |
| `http://localhost:3000` | Grafana | Metrics dashboards (admin/admin) |
| `http://localhost:9090` | Prometheus | Metrics query UI |
| `http://localhost:8025` | Mailpit | Email inbox (mock) |

## API Endpoints

All service APIs are available through the API Gateway at `http://localhost:9000`.

### User Service (`/api/v1/users`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/users` | Create a new user |
| `GET` | `/api/v1/users/{id}` | Get user by ID |
| `GET` | `/api/v1/users` | List all users |
| `PUT` | `/api/v1/users/{id}` | Update a user |
| `PATCH` | `/api/v1/users/{id}` | Partially update a user |
| `DELETE` | `/api/v1/users/{id}` | Delete a user |

**User DTO:**

```json
{
  "id": 1,
  "name": "John",
  "surname": "Doe",
  "email": "john.doe@example.com",
  "address": "123 Main St",
  "alerting": true,
  "energyAlertingThreshold": 150.0
}
```

### Device Service (`/api/v1/devices`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/devices/create` | Create a new device |
| `GET` | `/api/v1/devices/{deviceId}` | Get device by ID |
| `GET` | `/api/v1/devices/users/{userId}` | List all devices for a user |
| `PUT` | `/api/v1/devices/update/{deviceId}` | Update a device |
| `DELETE` | `/api/v1/devices/delete/{deviceId}` | Delete a device |

**Device DTO:**

```json
{
  "id": 1,
  "deviceName": "Living Room AC",
  "deviceType": "THERMOSTAT",
  "location": "Living Room",
  "userId": 1
}
```

**Device Types:** `SPEAKER`, `CAMERA`, `LIGHT`, `LOCK`, `THERMOSTAT`, `SENSOR`, `DOORBELL`, `VACUUM`, `OTHER`

### Ingestion Service (`/api/v1/ingestion`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/ingestion` | Ingest a single energy usage reading |

**Request Body:**

```json
{
  "deviceId": 1,
  "energyUsage": 2.5,
  "timestamp": "2025-01-15T10:30:00.000Z"
}
```

### Usage Service (`/api/v1/usage`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/usage/{userId}?day=3` | Get energy usage for a user (default: last 3 days) |

### Insight Service (`/api/v1/insight`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/insight/saving-tips/{userId}` | Get AI-generated energy-saving tips |
| `GET` | `/api/v1/insight/overview/{userId}` | Get AI-generated usage overview & recommendations |

## Kafka Topics

| Topic | Producer | Consumer | Description |
|-------|----------|----------|-------------|
| `energy-usage` | Ingestion Service | Usage Service | Raw energy usage readings from devices |
| `energy-alerts` | Usage Service | Alert Service | Threshold breach alerts for users |

## Database Schema

The application uses two databases:

### MariaDB (Port 3307)

Flyway-managed migrations are located in `User-service/src/main/resources/db/migration/`:

| Version | File | Tables |
|---------|------|--------|
| V1 | `V1__user_table.sql` | `users` |
| V2 | `V2__device_table.sql` | `devices` |
| V3 | `V3__alert_table.sql` | `alert` |

**Key tables:**

- `users` — User profile with email (unique), address, alerting preferences, and energy threshold
- `devices` — Smart devices linked to users via foreign key (`user_id`)
- `alert` — Alert records (user_id, sent status, created_at)

### InfluxDB (Port 8072)

- **Org:** `home_energy_tracker`
- **Bucket:** `usage-bucket`
- **Token:** `myToken`
- **Retention:** 1 week
- **Measurement:** `energy_usage` with fields: `energyConsumed` (float), tags: `deviceId`

### Keycloak (Port 8091)

- **Realm:** `het-security-realm`
- Realm configuration is imported from `docker/keycloak/realms/` on startup

## Monitoring & Observability

Each microservice exposes Micrometer/Prometheus metrics at:

```
http://localhost:<port>/actuator/prometheus
```

| Service | Port | Prometheus Job |
|---------|------|-----------------|
| User Service | 8080 | `user-service` |
| Device Service | 8081 | `device-service` |
| Ingestion Service | 8082 | `ingestion-service` |
| Usage Service | 8083 | `usage-service` |
| Alert Service | 8084 | `alert-service` |
| Insight Service | 8085 | `insight-service` |
| API Gateway | 9000 | `api-gateway` |

Prometheus scrapes these endpoints at a 15-second interval. Grafana is pre-provisioned with a datasource and a dashboard (`het-overview.json`).

## Configuration

### Service Ports

| Service | Port | Config Key |
|---------|------|------------|
| User Service | 8080 | `server.port=8080` |
| Device Service | 8081 | `server.port=8081` |
| Ingestion Service | 8082 | `server.port=8082` |
| Usage Service | 8083 | `server.port=8083` |
| Alert Service | 8084 | `server.port=8084` |
| Insight Service | 8085 | `server.port=8085` |
| API Gateway | 9000 | `server.port=9000` |

### Key Configuration Files

- Each service: `src/main/resources/application.properties`
- API Gateway routes: `api-gateway/src/main/java/.../routes/`
- Prometheus: `docker/prometheus/prometheus.yml`
- Grafana provisioning: `docker/grafana/provisioning/`

## Building & Running

### Build All Services

```bash
./mvnw clean install
```

### Run All Services (Maven)

```bash
./mvnw spring-boot:run -pl User-service,device_service,ingestion-service,usage-service,alert-service,insight-service,api-gateway
```

### Docker Compose

```bash
# Start infrastructure
docker-compose up -d

# Stop everything (removes volumes too)
docker-compose down -v
```

## Testing

```bash
# Run all tests
./mvnw test

# Run tests for a specific service
./mvnw test -pl User-service
```

## License

This project is provided as-is for educational purposes.
