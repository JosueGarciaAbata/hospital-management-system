# Hospital Management System 🏥

Microservices-based hospital management system built with **Spring Boot 3**, **Spring Cloud** (Gateway, Eureka), and **JWT Authentication**.

## 🚀 Features
- Centralized authentication (Auth Service) with JWT.
- API Gateway for routing and security validation.
- Service discovery with Eureka Server.
- Centralized configuration with Config Server.
- Core microservices:
    - **Auth Service** → login, register, roles, JWT issuance.
    - **Administration Service** → manage users, doctors, and medical centers.
    - **Consulting Service** → manage medical consultations.
    - **Reporting Service** → reports and analytics.
- PostgreSQL as the central database (via Docker Compose).

## 🛠️ Tech Stack
- Java 17
- Spring Boot 3.5.x
- Spring Cloud Gateway
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Maven

## 📂 Project Structure

```text
hospital-system-parent/
├── 📡 hospital-eureka-server # Service Discovery
├── 🚪 hospital-gateway # API Gateway
├── 🔑 auth-service # Authentication & JWT
├── 👨‍💼 admin-service # Admin management
├── 🩺 consulting-service # Medical consultations
└── 📊 reporting-service # Reports
```