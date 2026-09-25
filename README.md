# Arquetipo Hexagonal — Spring Boot

Plantilla base para construir microservicios en **Java + Spring Boot** siguiendo la **arquitectura hexagonal** (puertos y adaptadores). Incluye un CRUD mínimo de ejemplo (`Object`) que recorre todas las capas, para que lo copies, lo renombres y empieces a construir tu propio dominio.

### Stack

* ![Java](https://img.shields.io/badge/java-26-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
* ![Spring](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
* ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
* ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

Además: Spring Data JPA, Bean Validation, MapStruct, Lombok, springdoc-openapi (Swagger UI), H2 para tests y JaCoCo para cobertura.

---

## ¿Qué es la arquitectura hexagonal?

La arquitectura hexagonal (Alistair Cockburn, también llamada *Ports & Adapters*) busca que **la lógica de negocio no dependa de ninguna tecnología**. El dominio queda en el centro y todo lo demás —HTTP, base de datos, colas, APIs externas— se conecta a él desde afuera.

```
                ┌─────────────────────────────────────────────┐
   HTTP  ──►    │  Adaptador de entrada (RestController)      │
                │              │                              │
                │              ▼                              │
                │  Aplicación (Handler, DTOs, Mappers)        │
                │              │                              │
                │              ▼                              │
                │   ┌──────────────────────────────────┐      │
                │   │ DOMINIO                          │      │
                │   │  api/  ◄── puerto de entrada     │      │
                │   │  usecase/ (reglas de negocio)    │      │
                │   │  spi/  ──► puerto de salida      │      │
                │   └──────────────────────────────────┘      │
                │              │                              │
                │              ▼                              │
                │  Adaptador de salida (JpaAdapter)  ──►  DB  │
                └─────────────────────────────────────────────┘
```

Los conceptos clave:

| Concepto | Qué es | En este proyecto |
|---|---|---|
| **Dominio** | Modelos y reglas de negocio. Java puro, sin Spring ni JPA. | `domain/model`, `domain/usecase` |
| **Puerto de entrada** (*driving port*) | Interfaz que expone lo que el dominio sabe hacer. | `domain/api/IObjectServicePort` |
| **Puerto de salida** (*driven port*) | Interfaz que declara lo que el dominio *necesita* del exterior (persistir, notificar…). | `domain/spi/IObjectPersistencePort` |
| **Adaptador de entrada** | Traduce una tecnología de entrada (REST) a llamadas al dominio. | `infrastructure/input/rest` |
| **Adaptador de salida** | Implementa un puerto de salida con una tecnología concreta (JPA). | `infrastructure/out/jpa/adapter` |

**La regla de oro: las dependencias apuntan hacia adentro.** `infrastructure` conoce a `application` y a `domain`; `domain` no conoce a nadie. Por eso el dominio define la interfaz `IObjectPersistencePort` y es la infraestructura la que la implementa (inversión de dependencias).

Beneficios:

- **Cambiar de tecnología sin tocar el negocio**: pasar de PostgreSQL a Mongo, o de REST a Kafka, es escribir un adaptador nuevo.
- **Tests de dominio rápidos**: los casos de uso se prueban con mocks de los puertos, sin levantar Spring ni base de datos.
- **Límites claros**: cada capa tiene una responsabilidad y es fácil saber dónde va cada pieza de código.

---

## Estructura del proyecto

```
src/main/java/co/com/srdejo
├── domain                      # Núcleo: sin dependencias de frameworks
│   ├── api/                    # Puertos de entrada (IObjectServicePort)
│   ├── spi/                    # Puertos de salida (IObjectPersistencePort)
│   ├── usecase/                # Implementación de los puertos de entrada (ObjectUseCase)
│   ├── model/                  # Modelos de dominio (ObjectModel)
│   └── exception/              # Excepciones de negocio
│
├── application                 # Orquestación entre el mundo exterior y el dominio
│   ├── handler/                # Handlers: reciben DTOs, llaman al puerto de entrada
│   ├── dto/request|response/   # Contratos de la API
│   └── mapper/                 # DTO ⇄ modelo de dominio (MapStruct)
│
└── infrastructure              # Detalles técnicos
    ├── input/rest/             # Adaptador de entrada: controladores REST
    ├── out/jpa/                # Adaptador de salida: entidades, repositorios, mappers, adapter
    ├── configuration/          # BeanConfiguration: conecta puertos con adaptadores
    ├── exceptionhandler/       # @ControllerAdvice
    ├── exception/              # Excepciones técnicas
    └── documentation/          # Configuración de OpenAPI
```

### Flujo de una petición

`POST /api/v1/object/`

1. `ObjectRestController` recibe el `ObjectRequestDto`.
2. `ObjectHandler` lo convierte a `ObjectModel` con `IObjectRequestMapper` y llama a `IObjectServicePort`.
3. `ObjectUseCase` aplica las reglas de negocio y llama a `IObjectPersistencePort`.
4. `ObjectJpaAdapter` convierte el modelo a `ObjectEntity` y lo guarda con `IObjectRepository`.

Observa que `ObjectUseCase` **no tiene anotaciones de Spring**: se registra como bean en `BeanConfiguration`, que es el único lugar donde se "enchufan" los adaptadores a los puertos.

---

## Cómo empezar

### Prerrequisitos

* JDK 26 (Gradle toolchain lo puede descargar automáticamente)
* PostgreSQL (o Docker)
* Gradle — opcional, el proyecto incluye el wrapper `./gradlew`

### Instalación

1. Clona el repositorio

   ```sh
   git clone git@github.com:srdejo/arquetipo-hexagonal.git
   cd arquetipo-hexagonal
   ```

2. Levanta una base de datos PostgreSQL llamada `powerup`. Con Docker:

   ```sh
   docker run -d --name powerup-db -p 5432:5432 \
     -e POSTGRES_DB=powerup -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
     postgres:17
   ```

3. Configura la conexión (opcional). `application.yml` lee variables de entorno con valores por defecto:

   | Variable | Por defecto |
   |---|---|
   | `DB_HOST` | `localhost` |
   | `DB_PORT` | `5432` |
   | `DB_NAME` | `powerup` |
   | `DB_USERNAME` | `postgres` |
   | `DB_PASSWORD` | `postgres` |

### Ejecutar

```sh
./gradlew bootRun
```

La aplicación arranca en el puerto **8081**. Abre la documentación interactiva en
[http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html).

Prueba rápida:

```sh
curl -X POST http://localhost:8081/api/v1/object/ \
  -H "Content-Type: application/json" -d '{"name":"mi primer objeto"}'

curl http://localhost:8081/api/v1/object/
```

### Tests

```sh
./gradlew test
```

Los tests usan H2 en memoria (`src/test/resources/application.yml`). El reporte de cobertura de JaCoCo queda en `build/reports/jacoco/test/html/index.html`.

---

## Cómo usar el arquetipo para tu propio dominio

Supongamos que quieres modelar `Restaurant`:

1. **Renombra el paquete base** `co.com.srdejo` (y `group` en `build.gradle`) al de tu proyecto.
2. **Dominio** (empieza siempre por aquí):
   - `domain/model/Restaurant` — el modelo con sus reglas.
   - `domain/api/IRestaurantServicePort` — qué operaciones ofrece.
   - `domain/spi/IRestaurantPersistencePort` — qué necesita persistir.
   - `domain/usecase/RestaurantUseCase` — implementa el puerto de entrada, recibe el de salida por constructor. Sin `@Service`, sin `@Autowired`.
3. **Infraestructura de salida**: `RestaurantEntity`, `IRestaurantRepository`, `IRestaurantEntityMapper` y `RestaurantJpaAdapter implements IRestaurantPersistencePort`.
4. **Aplicación**: DTOs de request/response, sus mappers y `RestaurantHandler`.
5. **Infraestructura de entrada**: `RestaurantRestController`, que solo habla con el handler.
6. **Cablea** los nuevos puertos y adaptadores en `BeanConfiguration`.
7. Elimina el ejemplo `Object*` cuando ya no lo necesites.

### Reglas para no romper la arquitectura

- `domain` **no importa** nada de `application`, `infrastructure`, Spring, JPA ni Jackson.
- Los controladores **nunca** usan repositorios ni entidades JPA directamente.
- Las entidades JPA (`*Entity`) no salen de `infrastructure/out`; el resto del sistema trabaja con modelos de dominio.
- Las validaciones de negocio viven en el caso de uso y lanzan excepciones de `domain/exception`; `ControllerAdvisor` las traduce a respuestas HTTP.
- Si necesitas hablar con algo nuevo (otra API, una cola, un bucket), primero define el puerto en `domain/spi` y después el adaptador en `infrastructure/out`.
