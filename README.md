# franchise-management-api

API reactiva para administrar **franquicias**, sus **sucursales** y los **productos**
ofertados en cada sucursal.

Una franquicia se compone de un nombre y un listado de sucursales; una sucursal, de un
nombre y un listado de productos; un producto, de un nombre y una cantidad de stock.

---

## Stack

| Área | Tecnología |
|---|---|
| Lenguaje | Java 21 (LTS) |
| Framework | Spring Boot 4.1.0 · Spring WebFlux (reactivo, no bloqueante) |
| Programación | Funcional/reactiva con Project Reactor (`Mono` / `Flux`) |
| Arquitectura | Hexagonal (puertos y adaptadores) por paquetes |
| Persistencia | Amazon DynamoDB (AWS SDK v2, cliente asíncrono) |
| Build | Maven (con wrapper `./mvnw`) |
| Documentación | OpenAPI 3 vía springdoc → Swagger UI |
| Errores | RFC 9457 `application/problem+json` |
| Pruebas | JUnit 6 · AssertJ · Reactor `StepVerifier` · `WebTestClient` · Testcontainers · ArchUnit |

---

## Requisitos

- **JDK 21** — el proyecto fija la versión en `.sdkmanrc`. Con [SDKMAN!](https://sdkman.io):
  ```bash
  sdk env install && sdk env
  ```
- **Docker** — necesario para DynamoDB Local y para los tests de integración.
- **Maven** no hace falta instalarlo: usa el wrapper `./mvnw`.

---

## Cómo ejecutar en local

> El detalle completo (DynamoDB Local, creación de la tabla y ejemplos `curl`) se documenta
> al cerrar la Etapa 3. Por ahora la aplicación arranca y expone `actuator` y Swagger UI.

Compilar y ejecutar toda la batería de pruebas:

```bash
./mvnw verify
```

Levantar la aplicación:

```bash
./mvnw spring-boot:run
```

Una vez arriba:

- Health check → `http://localhost:8080/actuator/health`
- Swagger UI → `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON → `http://localhost:8080/v3/api-docs`

### Configuración

Todo se parametriza por variable de entorno, con valores por defecto aptos para local:

| Variable | Por defecto | Descripción |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto HTTP |
| `DYNAMODB_TABLE_NAME` | `franchises` | Tabla single-table del dominio |
| `AWS_REGION` | `us-east-1` | Región de AWS |
| `DYNAMODB_ENDPOINT` | *(vacío)* | Vacío = AWS real. `http://localhost:8000` = DynamoDB Local |

El perfil `local` (`SPRING_PROFILES_ACTIVE=local`) ya apunta a DynamoDB Local.

---

## Arquitectura

Hexagonal por paquetes dentro de un único módulo Maven. La regla es que **el dominio no
conoce a la infraestructura**, y se verifica automáticamente con ArchUnit:

```
co.com.nequi.franchise
├── domain              ← modelo, invariantes de negocio y puertos. Sin Spring, sin AWS.
│   ├── model
│   ├── exception
│   └── port/out
├── application         ← casos de uso. Orquestan el dominio a través de los puertos.
│   └── usecase
└── infrastructure      ← adaptadores. Aquí sí viven Spring, el SDK de AWS y el HTTP.
    ├── adapter/in/rest
    ├── adapter/out/dynamodb
    └── config
```

### Modelo de datos (single-table design)

Una sola tabla de DynamoDB con clave compuesta `pk` + `sk`:

| Ítem | `pk` | `sk` |
|---|---|---|
| Franquicia | `FRANCHISE#<franchiseId>` | `METADATA` |
| Sucursal | `FRANCHISE#<franchiseId>` | `BRANCH#<branchId>` |
| Producto | `FRANCHISE#<franchiseId>` | `BRANCH#<branchId>#PRODUCT#<productId>` |

Esto permite traer una franquicia completa —con todas sus sucursales y productos— en una
sola `Query` por `pk`, que es exactamente lo que necesita el endpoint de "producto con más
stock por sucursal" sin incurrir en N+1.

---

## Flujo de trabajo git

```
main        ← rama estable. Solo recibe merges desde develop vía PR.
 └ develop  ← rama de integración. Recibe cada etapa vía PR.
    └ feature/*  ·  fix/*  ·  chore/*   ← una rama por etapa
```

- **Commits:** [Conventional Commits](https://www.conventionalcommits.org)
  (`feat:`, `fix:`, `chore:`, `test:`, `docs:`, `refactor:`).
- **Regla:** ninguna rama se mergea con `./mvnw verify` en rojo.
- Cada etapa del roadmap corresponde a una rama y un PR contra `develop`.

---

## Roadmap

Criterios obligatorios primero; los puntos extra después.

| Etapa | Rama | Estado |
|---|---|---|
| 0 · Scaffold, build y estructura hexagonal | `chore/project-setup` | ✅ |
| 1 · Modelo de dominio e invariantes | `feature/domain-model` | ⏳ |
| 2 · Casos de uso reactivos | `feature/use-cases` | ⏳ |
| 3 · Adaptador DynamoDB + Docker Compose | `feature/dynamodb-adapter` | ⏳ |
| 4 · Adaptador REST, validación y OpenAPI | `feature/rest-api` | ⏳ |
| 5 · ArchUnit, pruebas e2e y documentación | `feature/hardening` | ⏳ |
| Extras · Docker, renombrados, Terraform, despliegue, CI | — | ⏳ |

### Endpoints previstos

Base: `/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/franchises` | Agregar una franquicia |
| `POST` | `/franchises/{fId}/branches` | Agregar una sucursal a una franquicia |
| `POST` | `/franchises/{fId}/branches/{bId}/products` | Agregar un producto a una sucursal |
| `DELETE` | `/franchises/{fId}/branches/{bId}/products/{pId}` | Eliminar un producto de una sucursal |
| `PUT` | `/franchises/{fId}/branches/{bId}/products/{pId}/stock` | Modificar el stock de un producto |
| `GET` | `/franchises/{fId}/branches/top-stock-products` | Producto con más stock por sucursal |
