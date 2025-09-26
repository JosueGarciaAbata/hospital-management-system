# Consulting Service

Microservicio del dominio **clínico** que gestiona **consultas médicas**, **pacientes** y **reportes estadísticos**.  
Se expone tras el **API Gateway** bajo el prefijo **`/api/consulting/**`** y colabora con **Admin Service** para validar doctores/centros.

## Propósito
Administrar el **ciclo de vida de consultas** (crear, listar, actualizar, eliminar lógicamente), **gestión de pacientes** por centro y **reportes** (por especialidad, doctor, centro y mes).

---

## Endpoints principales

> Paginación cuando aplica: `?page=0&size=10`  
> Roles soportados: `ADMIN`, `DOCTOR`.

### Consultas médicas — `/api/consulting/medical-consultations`
| Método | Ruta                                             | Descripción                                                | Roles |
|-------:|--------------------------------------------------|------------------------------------------------------------|------|
| GET    | `/api/consulting/medical-consultations`          | Listar por **doctorId** (query) – paginado                | ADMIN, DOCTOR |
| GET    | `/api/consulting/medical-consultations/{id}`     | Obtener **consulta** por ID                                | ADMIN, DOCTOR |
| GET    | `/api/consulting/medical-consultations/all`      | Listar **todas** – paginado                                | ADMIN, DOCTOR |
| GET    | `/api/consulting/medical-consultations/by-center/{centerId}`    | Listar por **centro** – paginado                           | ADMIN, DOCTOR |
| GET    | `/api/consulting/medical-consultations/by-specialty/{specialtyId}` | Listar por **especialidad** – paginado                 | ADMIN, DOCTOR |
| GET    | `/api/consulting/medical-consultations/center-has-consultations/{centerId}` | **200/404** si el centro tiene consultas | ADMIN, DOCTOR |
| GET    | `/api/consulting/medical-consultations/doctor-has-consultations/{doctorId}` | **200/404** si el doctor tiene consultas | ADMIN, DOCTOR |
| POST   | `/api/consulting/medical-consultations`          | **Crear** consulta                                         | DOCTOR |
| PUT    | `/api/consulting/medical-consultations/{id}`     | **Actualizar** consulta                                    | DOCTOR |
| DELETE | `/api/consulting/medical-consultations/{id}`     | **Borrado lógico** de consulta                             | DOCTOR |

### Pacientes — `/api/consulting/patients`
| Método | Ruta                                   | Descripción                                                         | Roles |
|-------:|----------------------------------------|---------------------------------------------------------------------|------|
| GET    | `/api/consulting/patients?centerId=`   | Listar **paginado** pacientes por centro                            | ADMIN, DOCTOR |
| GET    | `/api/consulting/patients/all?centerId=` | Listar **todos** los pacientes por centro                          | ADMIN, DOCTOR |
| GET    | `/api/consulting/patients/center-has-patients/{centerId}` | **200/404** si el centro tiene pacientes            | ADMIN, DOCTOR |
| GET    | `/api/consulting/patients/{id}`        | Obtener paciente por **ID**                                         | ADMIN, DOCTOR |
| POST   | `/api/consulting/patients`             | **Registrar** paciente                                              | DOCTOR |
| PUT    | `/api/consulting/patients/{id}`        | **Actualizar** paciente                                             | DOCTOR |
| DELETE | `/api/consulting/patients/{id}`        | **Borrado lógico** de paciente                                      | DOCTOR |

### Reportes — `/api/consulting/reports`
| Método | Ruta                          | Descripción                                  | Roles |
|-------:|-------------------------------|----------------------------------------------|------|
| GET    | `/api/consulting/reports/by-specialty` | Reporte por **especialidad**             | ADMIN, DOCTOR |
| GET    | `/api/consulting/reports/by-doctor`    | Reporte por **doctor**                   | ADMIN, DOCTOR |
| GET    | `/api/consulting/reports/by-center`    | Reporte por **centro**                   | ADMIN, DOCTOR |
| GET    | `/api/consulting/reports/by-month`     | Reporte **mensual**                      | ADMIN, DOCTOR |

> **Swagger (vía Gateway):** `http://localhost:8080/swagger-ui/index.html#/`  
> **OpenAPI (Consulting):** `http://localhost:8080/api/consulting/v3/api-docs`

---

## Configuración / Variables de entorno

Variables típicas (además de las comunes del `.env` del proyecto padre):
```env
CONSULTING_NAME=consulting-service
CONSULTING_PORT=8095

DB_HOSTNAME=hospital-db
DB_PORT=5432
DB_NAME=hospital_system
DB_USERNAME=postgres
DB_PASSWORD=root

EUREKA_URI=http://eureka-server:8761/eureka/
```

### Ajustes relevantes (`application.yml`)

- **JPA**: `ddl-auto=validate`, `show-sql=true`, `PostgreSQLDialect`.
- **Eureka**: `register-with-eureka=true`, `fetch-registry=true`, `instance-id=${app}:${ip}:${port}`.
- **springdoc**: `api-docs=/v3/api-docs`, `swagger-ui=/swagger-ui.html`.
- **Forward headers**: `forward-headers-strategy=framework` (compatibilidad detrás del gateway).

---

## Dependencias

Dependencias del microservicio:

- **PostgreSQL** → base de datos compartida por los microservicios.
- **Eureka Server** → registro y descubrimiento de servicios.
- **API Gateway** → enruta `/api/consulting/**` y valida JWT.
- **Admin Service** → consultas de metadatos/validaciones (ej.: doctores, centros) vía cliente Feign.  

## Ejecutar solo este servicio

### Con Docker (desde el proyecto padre)
```bash
docker compose build consulting-service
docker compose up -d consulting-service
```
>⚠️ Asegúrate de que PostgreSQL y Eureka Server estén en ejecución.
Para los endpoints que validan doctores o centros médicos, también debe estar disponible el Admin Service.

### Local (sin Docker)

Requisitos previos (pueden ejecutarse en contenedores Docker):
- **PostgreSQL** → `jdbc:postgresql://localhost:5432/hospital_system`
- **Eureka Server** → `http://localhost:8761/eureka/`

Ejecución del servicio:
```bash
cd consulting-service
./mvnw spring-boot:run
```

### Endpoints útiles (vía Gateway)

- **Swagger agrupado** → [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)
- **OpenAPI (Consulting)** → [http://localhost:8080/api/consulting/v3/api-docs](http://localhost:8080/api/consulting/v3/api-docs)

---

## Notas de seguridad

Seguridad de los endpoints:

- **Autenticación** → JWT validado en el **API Gateway**.
- **Autorización** → anotación `@RolesAllowed` en controladores (`ADMIN`, `DOCTOR`).
- **Tráfico interno** → confianza mediante cabeceras propagadas (`X-ROLES`, `X-User-Id`, `X-Center-Id` cuando aplique).
- **Header externo requerido** (clientes vía Gateway):
    - `Authorization: Bearer <JWT>`
- **CORS** → controlado en el Gateway con la variable `FRONTEND_URL`.  
