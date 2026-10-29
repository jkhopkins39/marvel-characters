# Marvel Comics Characters REST API

A Spring Boot REST API for managing Marvel Comics characters. The project follows a clear layered architecture:

**Controller → Service → Repository → Database (H2)**

Characters are stored in an in-memory H2 database, seeded with starter data on startup. Deletes are logical (`deleted = true`) so records stay in the database but disappear from normal GET responses.

## Purpose

Practice building a full CRUD-style Spring Boot API with:

- Dependency injection (constructor injection)
- JPA entities and repositories
- Bean Validation on incoming JSON
- Filtering and sorting
- Custom exceptions mapped to HTTP 400 / 404
- Logical deletion instead of physical row removal

## Entity fields (`MarvelCharacter`)

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Generated primary key (`@GeneratedValue`) |
| `name` | `String` | Required, 2–100 characters (validated) |
| `alias` | `String` | Required civilian / hero identity |
| `power` | `String` | Required primary power description |
| `universe` | `String` | Required (e.g. `Earth-616`, `Earth-1610`) |
| `team` | `String` | Optional (e.g. `X-Men`, `Avengers`, `Fantastic Four`) |
| `firstAppearanceYear` | `Integer` | Required, must be between 1939 and 2100 |
| `deleted` | `boolean` | Logical deletion flag (default `false`) |

## Starter data

On first startup the app loads characters across teams (Avengers, X-Men, Fantastic Four) plus a couple of unaffiliated heroes (Doctor Strange, Miles Morales).

## Requirements

- Java 21+ (developed and tested with Java 25)
- Maven Wrapper included (`./mvnw`) — no global Maven install required

## Setup and run

```bash
# From the project root
./mvnw spring-boot:run
```

The API starts at **http://localhost:8080**

Optional H2 console: **http://localhost:8080/h2-console**

- JDBC URL: `jdbc:h2:mem:marveldb`
- User: `sa`
- Password: *(leave empty)*

## Endpoints

Base path: `/api/characters`

| Method | Path | Description | Status codes |
| --- | --- | --- | --- |
| `GET` | `/api/characters` | List all **active** characters | `200` |
| `GET` | `/api/characters/{id}` | Get one active character by ID | `200`, `404` |
| `GET` | `/api/characters?universe=Earth-616` | Filter by universe | `200`, `400` |
| `GET` | `/api/characters?team=X-Men` | Filter by team (`none` = no team) | `200`, `400` |
| `GET` | `/api/characters?firstAppearanceYear=1966` | Filter by exact debut year | `200`, `400` |
| `GET` | `/api/characters?beforeYear=1980` | Debut year **&lt;** value | `200`, `400` |
| `GET` | `/api/characters?afterYear=1970` | Debut year **&gt;** value | `200`, `400` |
| `GET` | `/api/characters?sortBy=name` | Sort by a supported field | `200`, `400` |
| `POST` | `/api/characters` | Create a character from JSON | `201`, `400` |
| `DELETE` | `/api/characters/{id}` | Logical delete (`deleted = true`) | `200`, `404` |

Filters and `sortBy` can be combined, for example:
`/api/characters?universe=Earth-616&beforeYear=1980&sortBy=firstAppearanceYear`

### Filter choices

| Param | Meaning | Example |
| --- | --- | --- |
| `universe` | Exact match (case-insensitive) | `?universe=Earth-616` |
| `team` | Exact match (case-insensitive); `none`/`solo` for unaffiliated | `?team=X-Men` |
| `firstAppearanceYear` | Exact debut year | `?firstAppearanceYear=1966` |
| `beforeYear` | `firstAppearanceYear < year` | `?beforeYear=1980` |
| `afterYear` | `firstAppearanceYear > year` | `?afterYear=1970` |

- Blank `universe` returns **400 Bad Request**
- Non-numeric year params return **400 Bad Request**

### Sort choices

- Query param: `sortBy`
- Supported fields: `name`, `alias`, `power`, `universe`, `team`, `firstAppearanceYear`
- Example: `/api/characters?sortBy=name`
- Unsupported fields return **400 Bad Request**

### POST JSON example

```json
{
  "name": "Storm",
  "alias": "Ororo Munroe",
  "power": "Weather control",
  "universe": "Earth-616",
  "team": "X-Men",
  "firstAppearanceYear": 1975
}
```

Do not send `id` or `deleted` when creating; the API generates the ID and forces `deleted = false`.

### Logical deletion

`DELETE /api/characters/{id}` sets `deleted` to `true` and saves the record. The row is **not** removed from H2. After deletion:

- `GET /api/characters` no longer includes that character
- `GET /api/characters/{id}` returns **404**

## Architecture

```
com.marvel.characters
├── MarvelCharactersApplication.java
├── model/MarvelCharacter.java          # @Entity
├── repository/MarvelCharacterRepository.java  # JpaRepository
├── service/MarvelCharacterService.java # business logic + DI
├── controller/MarvelCharacterController.java  # REST endpoints
├── exception/                          # custom 400/404 exceptions
└── config/DataLoader.java              # starter data
```

Constructor injection is used in the controller, service, and data loader (preferred over `@Autowired` field injection).

## Testing steps

With the app running (`./mvnw spring-boot:run`), use curl or any HTTP client:

```bash
# 1. GET all active characters (200)
curl http://localhost:8080/api/characters

# 2. GET existing ID (200)
curl http://localhost:8080/api/characters/1

# 3. GET missing ID (404)
curl http://localhost:8080/api/characters/999

# 4. Filter by universe (200)
curl "http://localhost:8080/api/characters?universe=Earth-1610"

# 5. Invalid filter (400)
curl "http://localhost:8080/api/characters?universe="

# 5b. Exact year (200)
curl "http://localhost:8080/api/characters?firstAppearanceYear=1966"

# 5c. Before year (200) — debut before 1980
curl "http://localhost:8080/api/characters?beforeYear=1980"

# 5d. After year (200)
curl "http://localhost:8080/api/characters?afterYear=1970"

# 6. Valid sort (200)
curl "http://localhost:8080/api/characters?sortBy=name"

# 7. Invalid sort (400)
curl "http://localhost:8080/api/characters?sortBy=height"

# 8. Valid POST (201)
curl -X POST http://localhost:8080/api/characters \
  -H "Content-Type: application/json" \
  -d '{"name":"Storm","alias":"Ororo Munroe","power":"Weather control","universe":"Earth-616","firstAppearanceYear":1975}'

# 9. Invalid POST (400) — missing name
curl -X POST http://localhost:8080/api/characters \
  -H "Content-Type: application/json" \
  -d '{"alias":"Unknown","power":"None","universe":"Earth-616","firstAppearanceYear":2000}'

# 10. DELETE existing (200) — logical delete
curl -X DELETE http://localhost:8080/api/characters/1

# 11. Confirm deleted ID is gone (404)
curl http://localhost:8080/api/characters/1

# 12. DELETE missing (404)
curl -X DELETE http://localhost:8080/api/characters/999

# 13. Confirm deleted records no longer appear in GET all
curl http://localhost:8080/api/characters
```

## Project docs

- `README.md` — this file
- `AI_PROMPTS.md` — AI prompt history used while building the project
