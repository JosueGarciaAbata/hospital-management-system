# Reporting Service

Microservicio encargado de generar **reportes estadísticos** a partir de las consultas médicas registradas en el sistema.  
Expone endpoints para obtener información agregada por especialidad, médico, centro médico y por mes.

---

## Propósito
Facilitar la **analítica clínica** mediante reportes que permiten evaluar la carga de consultas y distribución según diferentes criterios (especialidad, doctor, centro, mes).

---

## Endpoints principales

| Recurso | Método | Descripción | Roles |
|---------|--------|-------------|-------|
| `/api/report/consultas-por-especialidad` | GET | Reporte de consultas agrupadas por especialidad | ADMIN, DOCTOR, STAFF |
| `/api/report/consultas-por-medico` | GET | Reporte de consultas agrupadas por médico | ADMIN, DOCTOR, STAFF |
| `/api/report/consultas-por-centro` | GET | Reporte de consultas agrupadas por centro médico | ADMIN, DOCTOR, STAFF |
| `/api/report/consultas-mensuales` | GET | Reporte de consultas agrupadas por mes y especialidad | ADMIN, DOCTOR, STAFF |

---

## Configuración y variables

Variables típicas (además de las comunes del `.env.docker.example` del proyecto padre)

```env
REPORTING_NAME=reporting-service
REPORTING_PORT=8093

DB_HOSTNAME=hospital-db
DB_PORT=5432
DB_NAME=hospital_system
DB_USERNAME=postgres
DB_PASSWORD=root

EUREKA_URI=http://eureka-server:8761/eureka/
```

### Ajustes relevantes (`application.yml`)
- **JPA** → `ddl-auto=validate`, `show-sql=true`, `PostgreSQLDialect`.
- **Eureka** → `register-with-eureka=true`, `fetch-registry=true`, `instance-id=${app}:${ip}:${port}`.
- **springdoc** → `api-docs=/v3/api-docs`, `swagger-ui=/swagger-ui.html`.
- **Forward headers** → `forward-headers-strategy=framework`.

---

## Dependencias
Dependencias del microservicio:
- **PostgreSQL** → base de datos compartida.
- **Eureka Server** → registro y descubrimiento de servicios.
- **API Gateway** → enruta `/api/report/**` y valida JWT.

---

## Ejecutar solo este servicio

### Con Docker (desde el proyecto padre)
```bash
docker compose build report-service
docker compose up -d report-service
```
>⚠️ Requiere que PostgreSQL y Eureka Server estén en ejecución.

### Local (sin Docker)

Requisitos previos:
- **PostgreSQL** → `jdbc:postgresql://localhost:5432/hospital_system`
- **Eureka Server** → `http://localhost:8761/eureka/`

Ejecución del servicio:
```bash
cd report-service
./mvnw spring-boot:run
```
### Endpoints útiles (vía Gateway)

- **Swagger agrupado** → [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)
- **OpenAPI (Reporting)** → [http://localhost:8080/api/report/v3/api-docs](http://localhost:8080/api/report/v3/api-docs)

---

## Notas de seguridad

Seguridad de los endpoints:

- **Autenticación** → JWT validado en el **API Gateway**.
- **Autorización** → con `@PreAuthorize` (`ADMIN`, `DOCTOR`, `STAFF`).
- **Tráfico interno** → confianza en cabeceras propagadas (`X-ROLES`, `X-User-Id`).
- **Header externo requerido** → `Authorization: Bearer <JWT>`.
- **CORS** → controlado en el Gateway con la variable `FRONTEND_URL`.  
