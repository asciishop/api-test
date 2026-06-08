# Imports API – Booking Requests

REST API construida con **Java 17 + Spring Boot 3.3** para gestionar solicitudes de reserva de importaciones.

---

## Como iniciar la aplicación

### Paso 1 — Requisitos previos

Verifica que tienes instalado:

| Herramienta | Versión mínima | Verificar con |
|---|---|---|
| Java | 17 | `java -version` |
| Maven | 3.9 | `mvn -version` |
| PostgreSQL | 15 | `psql --version` |

---

### Paso 2 — Base de datos

Conéctate a PostgreSQL como superusuario y ejecuta:

```sql
-- 1. Crear la base de datos
CREATE DATABASE imports_db;
```

```bash
# 2. Crear las tablas e insertar datos de prueba
psql -U postgres -d imports_db -f sql/schema.sql
```

Esto crea las tablas `suppliers`, `booking_requests`, `booking_items` y `users`.


---

### Paso 3 — Configuración

Abre `src/main/resources/application.yml` y verifica:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/imports_db
    username: postgres   
    password: postgres   

jwt:
  secret: "8f3a7c91d5e2b6f0a4c8d1e7f9b2a6c3d8e4f1a7b9c5d2e6f3a8c1b7d4e9f2a5"
  expiration-ms: 3600000  # 1 hora
```

---

### Paso 4 — Ejecutar la aplicación

```bash
mvn spring-boot:run
```

Al arrancar verás en los logs:

```
Started ImportsApiApplication in X seconds
```

La API queda disponible en:

| Recurso | URL |
|---|---|
| API base | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| OpenAPI JSON | http://localhost:8081/v3/api-docs |

> En el primer arranque, `DataInitializer` crea automáticamente los 3 usuarios de prueba en la tabla `users`.

---

### Paso 5 — Ejecutar los tests

```bash
mvn test
```

No requieren base de datos. Todos son tests unitarios con mocks.

---

### Paso 6 — Flujo en Postman

**1. Importar la colección**

```
Postman → Import → seleccionar postman/booking-request.json
```

**2. Obtener el token (una sola vez)**

Ejecuta el request **"0. Login (obtener token)"**:

```
POST http://localhost:8081/auth/login
{
  "username": "juan",
  "password": "secret123"
}
```

El script del request guarda el token automáticamente en la variable `{{jwtToken}}`. No necesitas copiar nada manualmente.

**3. Llamar a cualquier endpoint**

A partir de este momento todos los demás requests ya incluyen el header:

```
Authorization: Bearer {{jwtToken}}
```

Simplemente selecciona el request que quieras y presiona **Send**.

**4. Flujo de ejemplo — crear y confirmar una reserva**

```
① POST /auth/login          → obtener token
② POST /api/bookings        → crear reserva (estado: DRAFT)
③ GET  /api/bookings/{id}   → verificar datos
④ PATCH /api/bookings/{id}/status  { "status": "CONFIRMED" }
⑤ GET  /api/bookings        → listar todas las reservas del proveedor
```

**5. Cambiar de proveedor**

Edita el body del request de login con otro usuario:

| username | password | Proveedor |
|---|---|---|
| `juan` | `secret123` | Acme Corp |
| `maria` | `secret123` | Global Imports Ltd |
| `diego` | `secret123` | EuroSupply GmbH |

**6. Token expirado**

El token dura **1 hora**. Si recibes `401 Token inválido o expirado`, vuelve a ejecutar **"0. Login"** para obtener uno nuevo.

---

## Arquitectura

### Hexagonal (Ports & Adapters)

Las dependencias siempre apuntan **hacia adentro** — el dominio no conoce Spring, JPA ni HTTP.

```
┌──────────────────────────────────────────────────────┐
│  Infrastructure  (adapters, controllers, persistence) │
│  ┌────────────────────────────────────────────────┐   │
│  │  Application  (BookingService, AuthService)    │   │
│  │  ┌──────────────────────────────────────────┐  │   │
│  │  │  Domain  (models, ports, exceptions)     │  │   │
│  │  └──────────────────────────────────────────┘  │   │
│  └────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────┘
```

| Capa | Paquete | Responsabilidad |
|---|---|---|
| **Domain** | `domain.model`, `domain.port`, `domain.exception` | Lógica de negocio pura. Sin dependencias de frameworks. |
| **Application** | `application.service` | Orquesta casos de uso. Depende solo de puertos del dominio. |
| **Infrastructure – in** | `infrastructure.adapter.in.rest` | Controller REST, DTOs, mapper REST. |
| **Infrastructure – out** | `infrastructure.adapter.out.persistence` | Entidades JPA, repositorios Spring Data, adaptadores, mapper de persistencia. |
| **Infrastructure – config** | `infrastructure.config` | JWT, seguridad, inicialización de datos. |

### Decisiones de diseño

- **Puertos (interfaces) en el dominio** — `BookingRepositoryPort`, `UserRepositoryPort`, `PasswordHasherPort`, `*UseCase` desacoplan el servicio de JPA, Spring Security y HTTP.
- **Records para comandos y DTOs** — `CreateBookingCommand`, `BookingFilterQuery`, `PageResult<T>`, etc. son inmutables y concisos.
- **Modelos de dominio inmutables con `@Builder(toBuilder = true)`** — `toBuilder()` crea copias actualizadas sin mutación.
- **Lógica de transición encapsulada en el enum** — `BookingStatus.canTransitionTo(target)` con `switch` expression.
- **JPA Specification para filtros dinámicos** — `BookingSpecification.withFilters(...)` evita múltiples métodos `findBy*`.
- **Soft delete con flag `active`** — los registros nunca se eliminan físicamente.
- **`totalAmount` calculado en el servidor** — el cliente nunca envía este campo; siempre es `quantity × unitPrice`.
- **`callerTaxId` como parámetro explícito** — el servicio no conoce Spring Security; el controller extrae la identidad del token y la pasa como argumento.
- **`PasswordHasherPort` en el dominio** — `AuthService` no importa Spring Security; la verificación de contraseñas se delega a `BCryptPasswordHasherAdapter` en infraestructura.

---

## Estructura del proyecto

```

├── src/
│   ├── main/java/com/example/importsapi/
│   │   ├── domain/
│   │   │   ├── model/          # BookingRequest, BookingItem, Supplier, User, PageResult, enums
│   │   │   ├── port/in/        # Interfaces de casos de uso + records de comandos
│   │   │   ├── port/out/       # Interfaces de repositorios (BookingRepositoryPort, UserRepositoryPort, PasswordHasherPort)
│   │   │   └── exception/      # Excepciones de dominio
│   │   ├── application/
│   │   │   └── service/        # BookingService, AuthService
│   │   └── infrastructure/
│   │       ├── adapter/in/rest/ # BookingController, AuthController, DTOs, mappers
│   │       ├── adapter/out/persistence/ # Entidades JPA, repositorios, adaptadores, mapper
│   │       ├── config/         # SecurityConfig, JwtService, JwtAuthFilter, JwtProperties, BCryptPasswordHasherAdapter, DataInitializer, OpenApiConfig
│   │       └── exception/      # GlobalExceptionHandler
│   ├── main/resources/
│   │   └── application.yml
│   └── test/java/com/example/importsapi/
│       └── application/service/
│           ├── CreateBookingUseCaseTest.java
│           ├── GetBookingUseCaseTest.java
│           ├── ListBookingsUseCaseTest.java
│           ├── UpdateBookingUseCaseTest.java
│           ├── ChangeBookingStatusUseCaseTest.java
│           ├── DeleteBookingUseCaseTest.java
│           └── LoginUseCaseTest.java
├── sql/
│   └── schema.sql
├── postman/
│   └── booking-request.json
└── README.md
```

---

## Prerrequisitos

| Herramienta | Versión |
|---|---|
| Java | 17+ |
| Maven | 3.9+ |
| PostgreSQL | 15+ |

---

## Configuración de la base de datos

1. Crear la base de datos:

```sql
CREATE DATABASE imports_db;
```

2. Ejecutar el esquema:

```bash
psql -U postgres -d imports_db -f sql/schema.sql
```

Crea las tablas `suppliers`, `booking_requests`, `booking_items` y `users`, e inserta datos de prueba con 3 proveedores y 2 reservas de ejemplo.

3. Verificar credenciales en `src/main/resources/application.yml` (por defecto: usuario `postgres`, contraseña `postgres`, puerto `5432`).

---

## Configuración JWT

En `application.yml`:

```yaml
jwt:
  secret: "8f3a7c91d5e2b6f0a4c8d1e7f9b2a6c3d8e4f1a7b9c5d2e6f3a8c1b7d4e9f2a5"
  expiration-ms: 3600000   # 1 hora
```

---

## Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La API arranca en **http://localhost:8081**.

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

Al iniciar, `DataInitializer` crea automáticamente los 3 usuarios si la tabla `users` está vacía.

---

## Ejecutar los tests

```bash
mvn test
```

Tests unitarios con **JUnit 5 + Mockito**. No requieren base de datos. Cada caso de uso tiene su propia clase de test.

| Clase de test | Caso de uso | Escenarios cubiertos |
|---|---|---|
| `CreateBookingUseCaseTest` | `createBooking` | éxito, totalAmount calculado, estado inicial DRAFT, items vacíos, fechas inválidas, código duplicado, proveedor inexistente |
| `GetBookingUseCaseTest` | `getBooking` | éxito, no encontrada, ownership violation |
| `ListBookingsUseCaseTest` | `listBookings` | página con resultados, página vacía, delegación del filtro al repositorio |
| `UpdateBookingUseCaseTest` | `updateBooking` | éxito con updatedAt, campos nulos conservados, no en DRAFT, no encontrada, ownership violation |
| `ChangeBookingStatusUseCaseTest` | `changeStatus` | todas las transiciones válidas, CANCELLED terminal, CONFIRMED→DRAFT inválido, ownership violation |
| `DeleteBookingUseCaseTest` | `deleteBooking` | soft delete DRAFT con updatedAt, soft delete CANCELADO, CONFIRMADA lanza error, no encontrada, ownership violation |
| `LoginUseCaseTest` | `login` | éxito retorna taxId, usuario inexistente, contraseña incorrecta, misma excepción para ambos fallos |

---

## Seguridad

### Autenticación JWT

Todos los endpoints `/api/**` requieren un token JWT válido en el header:

```
Authorization: Bearer <token>
```

Los endpoints públicos (no requieren token):

```
POST /auth/login
GET  /swagger-ui/**
GET  /v3/api-docs/**
GET  /actuator/**
```

### Prevención de Parameter Tampering

| Vulnerabilidad | Cómo se previene |
|---|---|
| `taxId` manipulable en listado | Se extrae del token JWT, nunca del query param |
| `taxId` manipulable en creación | Se extrae del token JWT, nunca del body |
| IDOR en `/{id}` | `validateOwnership()` verifica que la reserva pertenece al caller; devuelve 404 si no coincide |

### Validación de entradas

| Campo | Validación |
|---|---|
| `bookingCode` | `@NotBlank`, `@Size(max=100)` |
| `currency` | `@NotBlank`, `@Size(min=3, max=3)` |
| `fobValue` | `@DecimalMin("0.00")` |
| `quantity` | `@NotNull`, `@Positive` |
| `unitPrice` | `@NotNull`, `@Positive` |
| `sku` | `@NotBlank`, `@Size(max=100)` |
| `items` | `@NotNull`, `@NotEmpty` |
| `page` | `@Min(0)` |
| `size` | `@Min(1)`, `@Max(100)` |

---

## Endpoints

### Autenticación

| Método | Path | Descripción |
|---|---|---|
| `POST` | `/auth/login` | Obtener token JWT |

**Request:**
```json
{
  "username": "juan",
  "password": "secret123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Reservas

| Método | Path | Descripción |
|---|---|---|
| `GET` | `/api/bookings` | Listar reservas con filtros y paginación |
| `POST` | `/api/bookings` | Crear reserva |
| `GET` | `/api/bookings/{id}` | Obtener reserva por ID |
| `PATCH` | `/api/bookings/{id}` | Actualizar reserva en estado DRAFT |
| `PATCH` | `/api/bookings/{id}/status` | Cambiar estado |
| `DELETE` | `/api/bookings/{id}` | Soft-delete (DRAFT o CANCELADO) |

### Parámetros de `GET /api/bookings`

| Param | Requerido | Descripción |
|---|---|---|
| `status` | No | `DRAFT`, `CONFIRMED`, `CANCELLED` |
| `freightMode` | No | `AIR`, `SEA`, `ROAD` |
| `dateFrom` | No | issueDate ≥ (yyyy-MM-dd) |
| `dateTo` | No | issueDate ≤ (yyyy-MM-dd) |
| `bookingCode` | No | Coincidencia exacta |
| `page` | No | Número de página (default: 0) |
| `size` | No | Tamaño de página (default: 20, máximo: 100) |

**Response paginado:**
```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 45,
  "totalPages": 3
}
```

---



---

## Campos de auditoría

| Campo | Descripción |
|---|---|
| `createdAt` | Fecha y hora de creación (inmutable) |
| `updatedAt` | Fecha y hora de la última modificación (`null` si nunca fue modificada) |

`updatedAt` se actualiza automáticamente en `updateBooking`, `changeStatus` y `deleteBooking`.

---

## Usuarios de prueba

Creados automáticamente al iniciar la aplicación:

| username | password | Proveedor | taxId |
|---|---|---|---|
| `juan` | `secret123` | Acme Corp | `12-3456789-0` |
| `maria` | `secret123` | Global Imports Ltd | `98-7654321-0` |
| `diego` | `secret123` | EuroSupply GmbH | `76-5432198-0` |

---

## Postman

1. Importar `postman/booking-request.json` en Postman.
2. Ejecutar **"0. Login (obtener token)"** — el script guarda el token automáticamente en la variable `{{jwtToken}}`.
3. Todos los demás requests usan `Authorization: Bearer {{jwtToken}}` de forma automática.

Para cambiar de usuario, edita el body del request de login con otro `username`/`password`.
