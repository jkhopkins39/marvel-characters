# AI Prompt History

This file documents the AI prompts used while building the Marvel Comics Characters Spring Boot REST API. Each entry explains what was asked, what changed, and what was learned.

---

## 1. Create and run the Spring Boot project

**Prompt used:**
> Create a Spring Boot Maven project for a Marvel Comics Characters REST API. Include Web, Spring Data JPA, H2, and Validation. Configure an in-memory H2 database and make sure the application starts.

**What changed:**
- Generated the Spring Boot project (`pom.xml`, main application class, `application.properties`)
- Configured H2 (`jdbc:h2:mem:marveldb`) and JPA `ddl-auto=update`

**Example snippet:**
```properties
spring.datasource.url=jdbc:h2:mem:marveldb
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

**Learned:** Spring Initializr + Maven Wrapper is enough to get a runnable API without installing Maven globally. Constructor-ready packages can be added later as features grow.

---

## 2. Add the MarvelCharacter entity

**Prompt used:**
> Create a JPA entity named MarvelCharacter with a generated Long ID, at least three other fields, at least one validated field, and a boolean deleted flag for logical deletion.

**What changed:**
- Added `MarvelCharacter` with `@Entity`, `@Id`, `@GeneratedValue`, `@Column`
- Added Bean Validation (`@NotBlank`, `@Size`, `@Min`, `@Max`)

**Example snippet:**
```java
@Entity
@Table(name = "marvel_characters")
public class MarvelCharacter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Column(nullable = false)
    private boolean deleted = false;
}
```

**Learned:** Validation annotations on the entity work with `@Valid` on `@RequestBody` so bad JSON fails with 400 before save.

---

## 3. Add the repository

**Prompt used:**
> Add a Spring Data JPA repository for MarvelCharacter that extends JpaRepository so I can use findAll(), findById(), and save().

**What changed:**
- Added `MarvelCharacterRepository` interface

**Example snippet:**
```java
@Repository
public interface MarvelCharacterRepository extends JpaRepository<MarvelCharacter, Long> {
}
```

**Learned:** Extending `JpaRepository` gives CRUD methods for free; no custom SQL was needed for this assignment.

---

## 4. Add starter data

**Prompt used:**
> Add a CommandLineRunner that seeds several Marvel characters into H2 on startup if the table is empty. Use constructor injection for the repository.

**What changed:**
- Added `DataLoader` under `config` package
- Seeded Spider-Man, Iron Man, Black Panther, and others

**Example snippet:**
```java
@Component
public class DataLoader implements CommandLineRunner {
    private final MarvelCharacterRepository repository;

    public DataLoader(MarvelCharacterRepository repository) {
        this.repository = repository;
    }
}
```

**Learned:** Checking `repository.count()` prevents duplicate seed data on restart when using a file-based DB; with in-memory H2 it still keeps the loader safe.

---

## 5. Add the service layer

**Prompt used:**
> Create a MarvelCharacterService that uses constructor injection (not field @Autowired). Include methods for listing active characters, finding by ID with Optional, filtering, sorting with a Comparator/stream, saving, and logical deletion.

**What changed:**
- Added service class with business rules
- Active-only listing skips `deleted == true`

**Example snippet:**
```java
@Service
public class MarvelCharacterService {
    private final MarvelCharacterRepository repository;

    public MarvelCharacterService(MarvelCharacterRepository repository) {
        this.repository = repository;
    }

    public List<MarvelCharacter> getAllActive() {
        List<MarvelCharacter> active = new ArrayList<>();
        for (MarvelCharacter character : repository.findAll()) {
            if (!character.isDeleted()) {
                active.add(character);
            }
        }
        return active;
    }
}
```

**Learned:** Constructor injection makes dependencies clear and testable. Field `@Autowired` works, but constructor injection is preferred.

---

## 6. Add the REST controller skeleton

**Prompt used:**
> Create a @RestController mapped to /api/characters that injects MarvelCharacterService through its constructor.

**What changed:**
- Added empty controller ready for endpoints

**Example snippet:**
```java
@RestController
@RequestMapping("/api/characters")
public class MarvelCharacterController {
    private final MarvelCharacterService service;

    public MarvelCharacterController(MarvelCharacterService service) {
        this.service = service;
    }
}
```

**Learned:** Keeping the controller thin and putting logic in the service preserves Controller → Service → Repository layering.

---

## 7. Add GET all

**Prompt used:**
> Add GET /api/characters that returns only active records with HTTP 200 using ResponseEntity.

**What changed:**
- Added `@GetMapping` list endpoint

**Example snippet:**
```java
@GetMapping
public ResponseEntity<List<MarvelCharacter>> getAll() {
    return ResponseEntity.ok(service.getAllActive());
}
```

**Learned:** `ResponseEntity.ok(...)` sets status 200 and returns JSON automatically via Jackson.

---

## 8. Add GET by ID

**Prompt used:**
> Add GET /api/characters/{id} using @PathVariable, findById(), and Optional. Return 200 if found and 404 if missing.

**What changed:**
- Added path-variable lookup with Optional handling

**Example snippet:**
```java
@GetMapping("/{id}")
public ResponseEntity<MarvelCharacter> getById(@PathVariable Long id) {
    Optional<MarvelCharacter> optional = service.findById(id);
    if (optional.isPresent()) {
        return ResponseEntity.ok(optional.get());
    }
    throw new ResourceNotFoundException("Character not found with id: " + id);
}
```

**Learned:** `Optional` avoids null checks; combining it with a custom 404 exception keeps API responses consistent.

---

## 9. Add filtering

**Prompt used:**
> Add filtering with @RequestParam for universe. Return 400 for invalid/blank input.

**What changed:**
- Added `?universe=` support in GET all
- Service throws when the value is blank

**Example snippet:**
```java
public List<MarvelCharacter> filterByUniverse(String universe) {
    if (universe == null || universe.isBlank()) {
        throw new InvalidRequestException("Universe filter must not be blank");
    }
    return getAllActive().stream()
            .filter(c -> c.getUniverse().equalsIgnoreCase(universe.trim()))
            .collect(Collectors.toList());
}
```

**Learned:** `@RequestParam(required = false)` lets the same GET endpoint support plain listing and filtered listing.

---

## 10. Add sorting and validation

**Prompt used:**
> Add sorting by a supported field using a Comparator and stream. Return 400 for unsupported fields.

**What changed:**
- Added `?sortBy=` with a switch over allowed fields
- Unsupported values throw `InvalidRequestException`

**Example snippet:**
```java
case "name":
    comparator = Comparator.comparing(MarvelCharacter::getName, String.CASE_INSENSITIVE_ORDER);
    break;
default:
    throw new InvalidRequestException("Unsupported sort field: " + field);
```

**Learned:** Sorting in the service with streams keeps the controller free of comparison logic and makes invalid fields easy to reject.

---

## 11. Add POST and request validation

**Prompt used:**
> Add POST /api/characters that accepts @RequestBody JSON, validates it, saves the character, and returns 201 or 400.

**What changed:**
- Added `@PostMapping` with `@Valid`
- Forces new ID and `deleted = false`

**Example snippet:**
```java
@PostMapping
public ResponseEntity<MarvelCharacter> create(@Valid @RequestBody MarvelCharacter character) {
    character.setId(null);
    character.setDeleted(false);
    MarvelCharacter saved = service.save(character);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

**Learned:** `@Valid` triggers entity constraints. A global handler converts `MethodArgumentNotValidException` into a readable 400 JSON body.

---

## 12. Add logical DELETE

**Prompt used:**
> Add DELETE /api/characters/{id} that sets deleted to true, saves the record, and returns 200 or 404. Do not physically remove the row.

**What changed:**
- Added `@DeleteMapping`
- Service updates the boolean flag instead of calling `deleteById`

**Example snippet:**
```java
public MarvelCharacter logicalDelete(Long id) {
    MarvelCharacter character = repository.findById(id)
            .filter(c -> !c.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Character not found with id: " + id));
    character.setDeleted(true);
    return repository.save(character);
}
```

**Learned:** Logical deletion preserves history and lets GET endpoints hide “removed” records without losing data.

---

## 13. Add exceptions and status codes

**Prompt used:**
> Add custom exceptions with @ResponseStatus for 400 Bad Request and 404 Not Found, plus a RestControllerAdvice for validation errors.

**What changed:**
- Added `ResourceNotFoundException`, `InvalidRequestException`, and `GlobalExceptionHandler`

**Example snippet:**
```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**Learned:** Throwing domain exceptions from the service keeps controllers simple; `@ResponseStatus` and `@RestControllerAdvice` map them to the correct HTTP codes.

---

## 14. Test and fix

**Prompt used:**
> Help me test every endpoint: GET all, existing/missing ID, valid/invalid POST, filtering, valid/invalid sort, existing/missing DELETE, and confirm deleted records no longer appear. Fix anything that fails.

**What changed:**
- Verified status codes with curl
- Restored explicit `Optional` usage in GET by ID for clarity

**Example curl:**
```bash
curl -X DELETE http://localhost:8080/api/characters/1
curl http://localhost:8080/api/characters/1   # expect 404
curl http://localhost:8080/api/characters     # id 1 absent
```

**Learned:** Manual curl testing catches status-code and logical-deletion issues quickly before writing docs.

---

## 15. Complete README and this prompt history

**Prompt used:**
> Write a README that explains purpose, data, entity fields, setup, endpoints, POST JSON, filter/sort choices, logical deletion, and testing steps. Also create AI_PROMPTS.md with plausible prompts and code examples for each development step.

**What changed:**
- Added `README.md` and `AI_PROMPTS.md`

**Learned:** Documenting prompts alongside commits makes the development process easier to explain for class submission and future maintenance.
