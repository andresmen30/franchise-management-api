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
| Pruebas | JUnit 6 · AssertJ · Reactor `StepVerifier` · `WebTestClient` · Testcontainers · ArchUnit · JaCoCo |

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

### 1. Levantar DynamoDB Local

```bash
docker compose up -d
```

Levanta dos contenedores:

| Servicio | Puerto | Para que sirve |
|---|---|---|
| `dynamodb-local` | 8000 | La base de datos, en memoria: al detener el contenedor los datos se pierden |
| `dynamodb-admin` | 8001 | Visor web para inspeccionar la tabla y sus items |

DynamoDB Local no tiene consola propia, por eso se incluye el visor: abre
`http://localhost:8001` y podras recorrer la tabla `franchises`, ver cada item con su `pk` y
`sk`, y editarlos o borrarlos a mano.

### 2. Ejecutar la aplicación

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Desde IntelliJ, el repositorio incluye la configuracion de ejecucion
**FranchiseApplication (local)**, que ya trae el perfil activo. Ejecutar la clase principal
sin ese perfil hace que la aplicacion apunte a AWS real en lugar de al contenedor local.

El perfil `local` apunta a DynamoDB Local y **crea la tabla al arrancar** si no existe.
En cualquier otro perfil esa creación automática está apagada: en la nube la tabla se
aprovisiona con infraestructura como código, no desde la aplicación.

Una vez arriba:

- Health check → `http://localhost:8080/actuator/health`
  Incluye el estado de la tabla de DynamoDB. `health/readiness` responde `503` si la base no
  esta accesible, mientras que `health/liveness` sigue en `200` porque el proceso vive.
- Swagger UI → `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON → `http://localhost:8080/v3/api-docs`

### 3. Ejecutar las pruebas

```bash
./mvnw verify
```

Las unitarias corren siempre. Las de integración (`*IT`) levantan su propio contenedor de
DynamoDB Local con Testcontainers, así que **requieren Docker en ejecución** pero no
dependen del `docker compose` anterior.

`verify` incluye tres verificaciones adicionales:

- **Reglas de arquitectura** (ArchUnit): la dirección de dependencias entre capas.
- **Prueba end to end**: recorre la API completa contra la aplicación arrancada.
- **Cobertura** (JaCoCo): mínimos de 90 % en instrucciones y 70 % en ramas sobre la suite
  combinada. El reporte queda en `target/site/jacoco/index.html`.

### 4. Ejecutar contra DynamoDB en AWS

La tabla se aprovisiona fuera de la aplicación. Para crearla:

```bash
aws dynamodb create-table --table-name franchises --billing-mode PAY_PER_REQUEST --attribute-definitions AttributeName=pk,AttributeType=S AttributeName=sk,AttributeType=S --key-schema AttributeName=pk,KeyType=HASH AttributeName=sk,KeyType=RANGE
```

Si tu sesión de AWS vive en el CLI y no en un archivo de credenciales, expórtala al entorno
antes de arrancar; el SDK de Java no lee todos los formatos de sesión del CLI:

```bash
eval "$(aws configure export-credentials --format env)"
```

```bash
AWS_REGION=us-east-1 DYNAMODB_TABLE_NAME=franchises ./mvnw spring-boot:run
```

Sin `DYNAMODB_ENDPOINT`, las credenciales se resuelven con la cadena estándar del SDK, que
en la nube toma el rol de la tarea o instancia.

### Probar la API con Postman

En [`postman/`](postman/) hay una coleccion lista para importar
(*Import → File* en Postman).

Trae dos carpetas. **1 - Flujo principal** recorre los seis endpoints obligatorios en orden y
cada peticion guarda los identificadores generados en variables de coleccion, de modo que no
hay que copiar y pegar ids: basta con ejecutarlas de arriba abajo, o usar el Collection Runner.
**2 - Errores** cubre los `404` y `400` y comprueba que la respuesta sea `problem+json` con su
codigo estable.

La variable `baseUrl` viene apuntando a `http://localhost:8080/api/v1`.

Como alternativa, Swagger UI en `http://localhost:8080/swagger-ui.html` permite ejecutar los
mismos endpoints desde el navegador.

### Configuración

Todo se parametriza por variable de entorno, con valores por defecto aptos para local:

| Variable | Por defecto | Descripción |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto HTTP |
| `DYNAMODB_TABLE_NAME` | `franchises` | Tabla single-table del dominio |
| `AWS_REGION` | `us-east-1` | Región de AWS |
| `DYNAMODB_ENDPOINT` | *(vacío)* | Vacío = AWS real. `http://localhost:8000` = DynamoDB Local |

Cuando `DYNAMODB_ENDPOINT` está vacío las credenciales se resuelven con la cadena estándar
del SDK (variables de entorno, perfil de `~/.aws`, rol de instancia). Contra DynamoDB Local
se usan credenciales ficticias, porque el emulador no las valida.

---

## Arquitectura

Hexagonal por paquetes dentro de un único módulo Maven. La regla es que **el dominio no
conoce a la infraestructura**: `domain` y `application` no importan Spring, el SDK de AWS ni
nada de `infrastructure`. Ocho reglas de ArchUnit lo verifican en cada `verify`, incluida la
única excepción admitida: el puerto expresa su contrato en tipos de Reactor.

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
 └ develop  ← rama de integración.
    └ feature/*  ·  fix/*  ·  chore/*
```

- **Commits:** [Conventional Commits](https://www.conventionalcommits.org)
  (`feat:`, `fix:`, `chore:`, `test:`, `docs:`, `refactor:`).
- Cada rama de trabajo se integra a `develop` mediante pull request.
- Ninguna rama se mergea con `./mvnw verify` en rojo.

---

## API

Base: `/api/v1`. Los errores se devuelven como `application/problem+json` (RFC 9457).

| Criterio | Método | Ruta | Éxito |
|---|---|---|---|
| 2 | `POST` | `/franchises` | `201` + `Location` |
| 3 | `POST` | `/franchises/{fId}/branches` | `201` + `Location` |
| 4 | `POST` | `/franchises/{fId}/branches/{bId}/products` | `201` + `Location` |
| 5 | `DELETE` | `/franchises/{fId}/branches/{bId}/products/{pId}` | `204` |
| 6 | `PUT` | `/franchises/{fId}/branches/{bId}/products/{pId}/stock` | `200` |
| 7 | `GET` | `/franchises/{fId}/branches/top-stock-products` | `200` |

Puntos extra:

| Método | Ruta | Éxito |
|---|---|---|
| `PUT` | `/franchises/{fId}/name` | `200` |
| `PUT` | `/franchises/{fId}/branches/{bId}/name` | `200` |
| `PUT` | `/franchises/{fId}/branches/{bId}/products/{pId}/name` | `200` |

Los tres renombrados afectan solo al nombre: renombrar una franquicia no toca sus sucursales,
renombrar una sucursal no toca sus productos, y renombrar un producto no altera su stock. Cada
entidad vive en su propio ítem de la tabla, así que la escritura queda acotada a ese ítem.

El endpoint de stock usa `PUT` sobre el subrecurso `/stock` porque fijar el stock a un valor
absoluto es un reemplazo idempotente: reintentar la misma petición no acumula. Un `POST` con
un delta no lo sería.

El listado de mayor stock omite las sucursales sin productos, y ante empate de stock desempata
por nombre ascendente para que la respuesta no dependa del orden de almacenamiento.

### Contrato de errores

| Situación | Status | `code` |
|---|---|---|
| Campo inválido o ausente | `400` | `VALIDATION_ERROR` |
| Cuerpo mal formado | `400` | `BAD_REQUEST` |
| Almacenamiento no disponible | `503` | `REPOSITORY_UNAVAILABLE` |
| Franquicia inexistente | `404` | `FRANCHISE_NOT_FOUND` |
| Sucursal inexistente | `404` | `BRANCH_NOT_FOUND` |
| Producto inexistente | `404` | `PRODUCT_NOT_FOUND` |

```json
{
  "type": "urn:franchise-api:solicitud-invalida",
  "title": "Solicitud invalida",
  "status": 400,
  "detail": "Uno o mas campos de la solicitud no son validos",
  "instance": "/api/v1/franchises",
  "code": "VALIDATION_ERROR",
  "errors": [ { "field": "name", "message": "must not be blank" } ]
}
```

Las respuestas de error nunca incluyen stack traces, nombres de clases Java, detalles del SDK
de AWS ni el valor rechazado.

### Ejemplo de uso

```bash
curl -i -X POST localhost:8080/api/v1/franchises -H 'Content-Type: application/json' -d '{"name":"Nequi Store"}'
```

```bash
curl -s localhost:8080/api/v1/franchises/$FRANCHISE_ID/branches/top-stock-products
```

```json
[
  { "branchId": "...", "branchName": "Norte",  "productId": "...", "productName": "Pan",  "stock": 120 },
  { "branchId": "...", "branchName": "Centro", "productId": "...", "productName": "Cafe", "stock": 500 }
]
```
