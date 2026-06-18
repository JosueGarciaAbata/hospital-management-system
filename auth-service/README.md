# Auth Service

## Propósito
Servicio de **Usuarios y Autenticación**. Gestiona cuentas de usuario, emisión/validación de credenciales (JWT validado en el **API Gateway**), y **recuperación de contraseñas** por correo.  
Prefijo expuesto vía Gateway: **`/auth/**`**.

---

## Endpoints principales

> Paginación donde aplica: `?page=0&size=10&sort=id`  
> Cabeceras internas inyectadas por Gateway: `X-User-Id`, `X-ROLES` (confianza entre microservicios).  
> Roles: `ADMIN`, `DOCTOR`.

### Usuarios
| Método | Ruta                          | Descripción                                                                  | Rol |
|-------:|-------------------------------|------------------------------------------------------------------------------|-----|
| GET    | `/auth/users/all-testing`     | Listar **todos** para pruebas (map a DTO)                                    | ADMIN |
| GET    | `/auth/users`                 | Listar **paginado** (excluye al usuario actual), admite `includeDeleted`     | ADMIN |
| GET    | `/auth/users/me`              | Datos del **usuario autenticado** (usa `X-User-Id`)                           | — |
| GET    | `/auth/users/{id}`            | Obtener por **ID** (opcional `enabled=true`)                                  | — |
| GET    | `/auth/users/by-center/{id}`  | Obtener usuario por **CenterId** (opcional `includeDisabled`)                 | — |
| HEAD   | `/auth/users/by-center/{id}/exists` | **204** si existe / **404** si no (opcional `includeDisabled`)          | — |
| POST   | `/auth/register`              | **Registrar** nuevo usuario                                                   | ADMIN |
| PUT    | `/auth/users/{id}`            | **Actualizar** usuario                                                        | ADMIN |
| DELETE | `/auth/{id}`                  | **Eliminar** (`hard=true` borra físico; si no, deshabilita)                   | ADMIN |

### Recuperación de contraseña
| Método | Ruta                   | Descripción                                      |
|-------:|------------------------|--------------------------------------------------|
| POST   | `/auth/request-reset`  | Solicitar token de **reset** (envío por correo)  |
| POST   | `/auth/reset-password` | Aplicar **reset** usando el token                |

> **Swagger (vía Gateway):** `http://localhost:8080/swagger-ui/index.html#/`  
> **OpenAPI (este servicio):** `http://localhost:8080/auth/v3/api-docs`

---

## Configuración / Variables de entorno

Variables típicas (además de las comunes del proyecto padre):
```env
AUTH_NAME=auth-service
AUTH_PORT=8090

DB_HOSTNAME=hospital-db
DB_PORT=5432
DB_NAME=hospital_system
DB_USERNAME=postgres
DB_PASSWORD=root

EUREKA_URI=http://eureka-server:8761/eureka/

# Correo para recuperación de contraseña
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<tu-correo>
MAIL_PASSWORD=<tu-pass/app-pass>
```
### Ajustes relevantes (`application.yml`)
- **JPA**: `ddl-auto=update`, `show-sql=true`.
- **Mail**: SMTP con `AUTH` y `STARTTLS`.
- **Eureka**: `register-with-eureka=true`, `fetch-registry=true`, `instance-id=${app}:${ip}:${port}`.
- **Springdoc**: `api-docs=/v3/api-docs`, `swagger-ui=/swagger-ui.html`.
- **Feign/Resilience**: circuit breaker habilitado.

---

## Dependencias
Dependencias del microservicio:
- **PostgreSQL** → base de datos compartida.
- **Eureka Server** → registro y descubrimiento de servicios.
- **API Gateway** → valida JWT y enruta tráfico con prefijo `/auth/**`.  

## Ejecutar solo este servicio

### Con Docker (desde el proyecto padre)
```bash
docker compose build auth-service
docker compose up -d auth-service
```
>⚠️ Asegúrate de que PostgreSQL y Eureka Server estén en ejecución antes de levantar este módulo.

### Local (sin Docker)

Requisitos previos (pueden ejecutarse en contenedores Docker):
- **PostgreSQL** → `jdbc:postgresql://localhost:5432/hospital_system`
- **Eureka Server** → `http://localhost:8761/eureka/`

Ejecución del servicio:
```bash
cd auth-service
./mvnw spring-boot:run
```
### Endpoints útiles (vía Gateway)

- **Swagger agrupado** → [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)
- **OpenAPI (Auth Service)** → [http://localhost:8080/auth/v3/api-docs](http://localhost:8080/auth/v3/api-docs)  

## Notas de seguridad

Seguridad de acceso a los endpoints:
- **Autenticación** → JWT validado en el **API Gateway**.
- **Autorización** → control de acceso mediante roles: `ADMIN` y `DOCTOR`.
- **Tráfico interno** → confianza a través de cabeceras propagadas (`X-ROLES`, `X-User-Id`).
- **Headers externos requeridos** (clientes vía Gateway):
    - `Authorization: Bearer <JWT>`

**Buenas prácticas**:
- Usar contraseñas de aplicación para **SMTP**.
- No registrar datos sensibles en logs.
- Rotar periódicamente la variable `MAIL_PASSWORD`.  
