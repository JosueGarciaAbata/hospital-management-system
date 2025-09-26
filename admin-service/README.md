# Admin Service

## Propósito
Servicio de **Administración** del dominio: gestiona **doctores**, **centros médicos** y **especialidades**.  
Se expone tras el **API Gateway** bajo el prefijo **`/admin/**`**. Autenticación JWT en el gateway; entre microservicios se confía mediante cabeceras (p. ej., `X-ROLES`).

---

## Endpoints principales

> Prefijo de servicio: **`/admin`** (vía Gateway)  
> Paginación: soporta `Pageable` (p. ej. `?page=0&size=20&sort=field,asc`)  
> Borrado lógico: `includeDeleted=false` por defecto

### Doctores `/admin/doctors`
| Método | Ruta                                   | Descripción                                           | Rol requerido |
|-------:|----------------------------------------|-------------------------------------------------------|---------------|
| GET    | `/admin/doctors`                       | Listar **paginado** (opcional `includeDeleted`)       | `ADMIN`       |
| GET    | `/admin/doctors/all`                   | Listar **todo** (opcional `includeDeleted`)           | `ADMIN`       |
| GET    | `/admin/doctors/{id}`                  | Obtener por **ID** (opcional `includeDeleted`)        | `ADMIN`       |
| GET    | `/admin/doctors/by-user/{userId}`      | Obtener por **userId**                                | `ADMIN` / `DOCTOR` |
| GET    | `/admin/doctors/by-specialty/{id}`     | Listar por **especialidad** (paginado)                | `ADMIN`       |
| GET    | `/admin/doctors/exists-by-user/{id}`   | **200** si existe / **404** si no                     | `ADMIN`       |
| POST   | `/admin/doctors/register`              | **Registrar** usuario (rol DOCTOR) + doctor           | `ADMIN`       |
| POST   | `/admin/doctors`                       | **Crear** doctor (asociar especialidad)               | `ADMIN`       |
| PUT    | `/admin/doctors/{id}`                  | **Actualizar** doctor + especialidad                  | `ADMIN`       |
| DELETE | `/admin/doctors/{id}`                  | **Borrado lógico**                                    | `ADMIN`       |

### Centros Médicos `/admin/centers`
| Método | Ruta                             | Descripción                                                       | Rol requerido |
|-------:|----------------------------------|-------------------------------------------------------------------|---------------|
| GET    | `/admin/centers`                 | Listar **paginado** (opcional `includeDeleted`)                   | `ADMIN`       |
| GET    | `/admin/centers/all`             | Listar **todo** (opcional `includeDeleted`)                       | `ADMIN` / `DOCTOR` |
| GET    | `/admin/centers/{id}`            | Obtener por **ID** (ETag en respuesta)                            | `ADMIN` / `DOCTOR` |
| POST   | `/admin/centers/batch`           | Obtener **varios** por IDs                                        | `ADMIN`       |
| POST   | `/admin/centers`                 | **Crear** (ETag en respuesta)                                     | `ADMIN`       |
| PUT    | `/admin/centers/{id}`            | **Actualizar** (usa `If-Match` opcional; control con `@Version`)  | `ADMIN`       |
| DELETE | `/admin/centers/{id}`            | **Borrado lógico**                                                | `ADMIN`       |
| GET    | `/admin/centers/validate/{id}`   | **200** si existe / **404** si no                                  | Público interno |

### Especialidades `/admin/specialties`
| Método | Ruta                               | Descripción                                                       | Rol requerido |
|-------:|------------------------------------|-------------------------------------------------------------------|---------------|
| GET    | `/admin/specialties`               | Listar **paginado** (opcional `includeDeleted`)                   | `ADMIN` / `DOCTOR` |
| GET    | `/admin/specialties/all`           | Listar **todo** (opcional `includeDeleted`)                       | `ADMIN` / `DOCTOR` |
| GET    | `/admin/specialties/{id}`          | Obtener por **ID** (ETag en respuesta)                            | `ADMIN` / `DOCTOR` |
| POST   | `/admin/specialties`               | **Crear** (ETag en respuesta)                                     | `ADMIN`       |
| PUT    | `/admin/specialties/{id}`          | **Actualizar** (usa `If-Match` opcional; control con `@Version`)  | `ADMIN`       |
| DELETE | `/admin/specialties/{id}`          | **Borrado lógico**                                                | `ADMIN`       |

> **ETag/If-Match**: en *centers* y *specialties* se devuelve `ETag` y puede enviarse `If-Match` para evitar colisiones (optimistic locking con `@Version`).

---

## Configuración / Variables de entorno

Variables típicas (además de las comunes del `.env` del proyecto padre):
```env
ADMIN_SERVICE_NAME=admin-service
ADMIN_SERVICE_PORT=8091

DB_HOSTNAME=hospital-db
DB_PORT=5432
DB_NAME=hospital_system
DB_USERNAME=postgres
DB_PASSWORD=root

EUREKA_URI=http://eureka-server:8761/eureka/
```

## Dependencias

Dependencias del microservicio:
- **PostgreSQL** (BD compartida por todos los servicios).
- **Eureka Server** para registro y descubrimiento (`${EUREKA_URI}`).
- **API Gateway** para exponer los endpoints bajo el prefijo **`/admin/**`**.
- **auth-service** (integración para `POST /admin/doctors/register` al crear usuario con rol **DOCTOR**).

## Ejecutar solo este servicio

### Con Docker (usando el compose del proyecto padre)
```bash
docker compose build admin-service
docker compose up -d admin-service
```

Asegúrate de que los servicios db y eureka-server estén arriba si levantas solo este módulo.

### Local (sin Docker)

Requisitos previos en ejecución (pueden levantarse en Docker):
- **PostgreSQL** en `jdbc:postgresql://localhost:5432/hospital_system`
- **Eureka Server** en `http://localhost:8761/eureka/`

Ejecutar el servicio:
```bash
cd admin-service
./mvnw spring-boot:run
```
### Endpoints útiles (vía Gateway)

- **Swagger agrupado:** [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)
- **OpenAPI específico (Admin Service):** [http://localhost:8080/admin/v3/api-docs](http://localhost:8080/admin/v3/api-docs)
- **Actuator (Admin Service):** [http://localhost:8080/admin/actuator/health](http://localhost:8080/admin/actuator/health)  

## Notas de seguridad

Seguridad en el servicio:

- **Autenticación** → JWT validado en el **API Gateway**.
- **Autorización** → anotación `@RequireRole` en controladores. Roles disponibles: `ADMIN` y `DOCTOR`.
- **Tráfico interno** → confianza entre microservicios mediante cabeceras propagadas por el Gateway (`X-ROLES`).
- **Header requerido** (para clientes externos al Gateway):
    - `Authorization: Bearer <JWT>`

**Buenas prácticas**:
- Usar `ETag` / `If-Match` donde aplique.
- Minimizar datos sensibles en logs.
- Validar la variable `FRONTEND_URL` para **CORS** en el Gateway.  
