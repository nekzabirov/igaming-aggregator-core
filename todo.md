# Core Module Architecture Recommendations

> **Overall Score: 5.3/10** - Major refactoring needed

---

## Critical Issues (Fix Immediately)

### 1. Fake Adapters in Production
**Location:** `shared/src/main/kotlin/Module.kt`

```kotlin
// CURRENT - WRONG!
single<WalletAdapter> { FakeWalletAdapter() }
single<PlayerAdapter> { FakePlayerAdapter() }
```

**Problem:** Production code uses fake/mock implementations for wallet and player services.

**Recommendation:**
- [ ] Create real `WalletAdapterImpl` that connects to actual wallet service
- [ ] Create real `PlayerAdapterImpl` that connects to actual player service
- [ ] Move fake implementations to test sources only
- [ ] Use environment-based DI configuration for different environments

---

### 2. Service Locator Anti-Pattern
**Location:** All usecases and services

```kotlin
// CURRENT - WRONG!
class PlaceSpinUsecase : KoinComponent {
    private val spinService = getKoin().get<SpinServiceSpec>()
    private val eventProducer = getKoin().get<EventProducerAdapter>()
}
```

**Problem:** Dependencies are implicit, hard to test, violates Dependency Inversion.

**Recommendation:**
- [ ] Refactor all usecases to use constructor injection:
```kotlin
// RECOMMENDED
class PlaceSpinUsecase(
    private val spinService: SpinServiceSpec,
    private val eventProducer: EventProducerAdapter
)
```
- [ ] Update Module.kt to inject dependencies:
```kotlin
factory { PlaceSpinUsecase(get(), get()) }
```

**Affected files (29 usecases + services):**
- [ ] `app/usecase/AddAggregatorUsecase.kt`
- [ ] `app/usecase/AddCollectionUsecase.kt`
- [ ] `app/usecase/AddGameCollectionUsecase.kt`
- [ ] `app/usecase/AddGameFavouriteUsecase.kt`
- [ ] `app/usecase/AddGameWonUsecase.kt`
- [ ] `app/usecase/AssignGameVariantUsecase.kt`
- [ ] `app/usecase/AssignProviderToAggregatorUsecase.kt`
- [ ] `app/usecase/CancelFreespinUsecase.kt`
- [ ] `app/usecase/ChangeGameOrderCollectionUsecase.kt`
- [ ] `app/usecase/CloseRoundUsecase.kt`
- [ ] `app/usecase/CreateFreespinUsecase.kt`
- [ ] `app/usecase/DemoGameUsecase.kt`
- [ ] `app/usecase/ListAggregatorUsecase.kt`
- [ ] `app/usecase/ListAllActiveAggregatorUsecase.kt`
- [ ] `app/usecase/ListCollectionUsecase.kt`
- [ ] `app/usecase/ListGameUsecase.kt`
- [ ] `app/usecase/ListGameVariantUsecase.kt`
- [ ] `app/usecase/OpenSessionUsecase.kt`
- [ ] `app/usecase/PlaceSpinUsecase.kt`
- [ ] `app/usecase/ProviderListUsecase.kt`
- [ ] `app/usecase/RemoveGameCollectionUsecase.kt`
- [ ] `app/usecase/RemoveGameFavouriteUsecase.kt`
- [ ] `app/usecase/RollbackSpinUsecase.kt`
- [ ] `app/usecase/SettleSpinUsecase.kt`
- [ ] `app/usecase/SyncGameUsecase.kt`
- [ ] `app/usecase/UpdateCollectionUsecase.kt`
- [ ] `app/usecase/UpdateGameTagsUsecase.kt`
- [ ] `app/usecase/UpdateGameUsecase.kt`
- [ ] `app/usecase/UpdateProviderUsecase.kt`
- [ ] `app/service/GameService.kt`
- [ ] `app/service/spin/SpinService.kt`

---

### 3. No Repository Pattern
**Location:** All usecases directly access database tables

```kotlin
// CURRENT - WRONG!
suspend operator fun invoke(...) = newSuspendedTransaction {
    val game = GameTable.full()
        .andWhere { GameTable.identity eq gameIdentity }
        .singleOrNull()?.toGameFull()

    GameWonTable.insert { ... }
}
```

**Problem:** Database queries mixed with business logic, no abstraction layer.

**Recommendation:**
- [ ] Create repository interfaces in `domain/*/repository/`:
```kotlin
// domain/game/repository/GameRepository.kt
interface GameRepository {
    suspend fun findById(id: UUID): Game?
    suspend fun findByIdentity(identity: String): GameFull?
    suspend fun save(game: Game): Game
    suspend fun update(game: Game): Game
}
```

- [ ] Create implementations in `infrastructure/repository/`:
```kotlin
// infrastructure/repository/ExposedGameRepository.kt
class ExposedGameRepository : GameRepository {
    override suspend fun findById(id: UUID): Game? = newSuspendedTransaction {
        GameTable.selectAll()
            .where { GameTable.id eq id }
            .singleOrNull()?.toGame()
    }
}
```

**Repositories to create:**
- [ ] `GameRepository` / `ExposedGameRepository`
- [ ] `GameVariantRepository` / `ExposedGameVariantRepository`
- [ ] `ProviderRepository` / `ExposedProviderRepository`
- [ ] `CollectionRepository` / `ExposedCollectionRepository`
- [ ] `SessionRepository` / `ExposedSessionRepository`
- [ ] `SpinRepository` / `ExposedSpinRepository`
- [ ] `RoundRepository` / `ExposedRoundRepository`
- [ ] `AggregatorRepository` / `ExposedAggregatorRepository`
- [ ] `GameWonRepository` / `ExposedGameWonRepository`
- [ ] `GameFavouriteRepository` / `ExposedGameFavouriteRepository`

---

### 4. God Usecase: ListGameUsecase (280 lines)
**Location:** `app/usecase/ListGameUsecase.kt`

**Problems:**
- 280 lines of code
- Contains TODO comment admitting technical debt (line 184)
- Manual pagination with `subList()` instead of SQL LIMIT/OFFSET
- In-memory grouping with `LinkedHashMap`
- Multiple responsibilities

**Recommendation:**
- [ ] Split into smaller usecases or extract to service:
  - `ListGameUsecase` - orchestration only
  - `GameQueryService` - handles complex queries
  - `GameFilterBuilder` - builds query filters
- [ ] Fix pagination: use SQL LIMIT/OFFSET
- [ ] Move grouping logic to repository layer
- [ ] Remove the TODO and implement proper solution

---

## High Priority Issues

### 5. Mixed Exception Types
**Location:** Various usecases

```kotlin
// CURRENT - INCONSISTENT!
Result.failure(SessionUnavailingError())      // Domain error
Result.failure(NotFoundException("..."))       // Ktor framework exception
Result.failure(BadRequestException("..."))     // Ktor framework exception
```

**Problem:** Framework exceptions leak into domain layer.

**Recommendation:**
- [ ] Create domain-specific errors for all cases:
```kotlin
// core/error/DomainErrors.kt
sealed class DomainError : Exception() {
    data class NotFound(val entity: String, val id: String) : DomainError()
    data class ValidationFailed(val message: String) : DomainError()
    data class Unauthorized(val reason: String) : DomainError()
}
```
- [ ] Map domain errors to HTTP/gRPC errors at API boundary only
- [ ] Remove all Ktor exceptions from usecases

---

### 6. Wrong Package Declaration
**Location:** `core/ext/String.ext.kt`

```kotlin
// CURRENT - WRONG!
package com.djmhub.game.shared.domain.core.ext
```

**Problem:** Package declaration doesn't match file location.

**Recommendation:**
- [ ] Fix package to match structure: `package core.ext`
- [ ] Or move file to match package: `domain/core/ext/`
- [ ] Audit all files for package consistency

---

### 7. Inconsistent Service Patterns
**Location:** `app/service/`

```kotlin
// Three different patterns:
object SessionService { ... }           // Singleton object
object GameService : KoinComponent { }  // Singleton + Service Locator
class SpinService : KoinComponent { }   // Class + Service Locator
```

**Recommendation:**
- [ ] Standardize all services as classes with constructor injection:
```kotlin
class SessionService(
    private val sessionRepository: SessionRepository
)

class GameService(
    private val gameRepository: GameRepository,
    private val cacheAdapter: CacheAdapter
)

class SpinService(
    private val spinRepository: SpinRepository,
    private val walletAdapter: WalletAdapter,
    private val playerAdapter: PlayerAdapter
)
```

---

### 8. Resource Leak: HttpClient
**Location:** `infrastructure/aggregator/onegamehub/adapter/OneGameHubAdapter.kt`

```kotlin
// CURRENT - POTENTIAL LEAK!
class OneGameHubAdapter(...) : IAggregatorAdapter {
    private val client = HttpClient(CIO) { ... }  // New client per instance
}
```

**Recommendation:**
- [ ] Inject shared HttpClient via DI:
```kotlin
class OneGameHubAdapter(
    private val httpClient: HttpClient,
    private val config: OneGameHubConfig
) : IAggregatorAdapter
```
- [ ] Configure single HttpClient in Module.kt with connection pooling
- [ ] Add proper client lifecycle management (close on shutdown)

---

### 9. In-Memory Cache Not Production Ready
**Location:** `infrastructure/adapter/MapCacheAdapter.kt`

```kotlin
// CURRENT - NOT THREAD-SAFE, NOT PERSISTENT!
class MapCacheAdapter : CacheAdapter {
    private val cache = mutableMapOf<String, Any>()
}
```

**Recommendation:**
- [ ] Implement Redis-based cache adapter:
```kotlin
class RedisCacheAdapter(
    private val redisClient: RedisClient
) : CacheAdapter
```
- [ ] Or use ConcurrentHashMap for thread safety as minimum
- [ ] Add TTL support for cache entries
- [ ] Consider using Caffeine for local caching

---

## Medium Priority Issues

### 10. Anemic Domain Models
**Location:** `domain/*/model/`

```kotlin
// CURRENT - NO BUSINESS LOGIC!
data class Game(
    val id: UUID,
    val identity: String,
    val name: String,
    // ... just data
)
```

**Recommendation:**
- [ ] Add business logic and invariants to domain models:
```kotlin
data class Game(
    val id: UUID,
    val identity: String,
    val name: String,
    val tags: List<String>,
    val active: Boolean
) {
    init {
        require(identity.isNotBlank()) { "Game identity cannot be blank" }
        require(name.isNotBlank()) { "Game name cannot be blank" }
    }

    fun isPlayable(): Boolean = active
    fun hasTag(tag: String): Boolean = tags.contains(tag)
    fun addTag(tag: String): Game = copy(tags = tags + tag)
}
```

---

### 11. Bloated GameFull Model (18 properties)
**Location:** `domain/game/model/GameFull.kt`

**Recommendation:**
- [ ] Split into smaller, focused models:
```kotlin
data class Game(val id: UUID, val identity: String, val name: String, ...)
data class GameConfig(val bonusBetEnable: Boolean, val freeSpinEnable: Boolean, ...)
data class GameMedia(val images: ImageMap)
data class GameMetadata(val locales: List<Locale>, val platforms: List<Platform>)
```
- [ ] Use composition instead of flat structure
- [ ] Create specific DTOs for different use cases

---

### 12. Extension Mappers Not Testable
**Location:** `domain/*/mapper/`

```kotlin
// CURRENT - EXTENSION FUNCTIONS CAN'T BE MOCKED!
fun ResultRow.toGame() = Game(...)
fun ResultRow.toGameFull() = GameFull(...)
```

**Recommendation:**
- [ ] Convert to mapper classes/objects:
```kotlin
object GameMapper {
    fun fromResultRow(row: ResultRow): Game = Game(...)
    fun toEntity(game: Game): GameEntity = ...
}
```
- [ ] Or create mapper interfaces for testing:
```kotlin
interface GameMapper {
    fun fromResultRow(row: ResultRow): Game
}

class DefaultGameMapper : GameMapper { ... }
```

---

### 13. Events Contain Too Much Data
**Location:** `app/event/SpinEvent.kt`

```kotlin
// CURRENT - SERIALIZES 18-PROPERTY OBJECT!
data class SpinEvent(
    val type: SpinType,
    val game: GameFull,  // Too much data!
    val amount: Int,
    ...
)
```

**Recommendation:**
- [ ] Use IDs and minimal data in events:
```kotlin
data class SpinEvent(
    val type: SpinType,
    val gameId: UUID,
    val gameIdentity: String,
    val amount: Int,
    val currency: Currency,
    val playerId: String,
    val freeSpinId: String?
)
```
- [ ] Consumers can fetch full game data if needed

---

### 14. Single Event Type with Discriminator
**Location:** `app/event/SpinEvent.kt`

```kotlin
// CURRENT - ONE CLASS, MULTIPLE TYPES
data class SpinEvent(val type: SpinType, ...) {
    override val key = "spin." + when (type) { ... }
}
```

**Recommendation:**
- [ ] Create separate event classes:
```kotlin
sealed interface SpinEvent : IEvent

data class SpinPlacedEvent(...) : SpinEvent {
    override val key = "spin.placed"
}

data class SpinSettledEvent(...) : SpinEvent {
    override val key = "spin.settled"
}

data class SpinRolledBackEvent(...) : SpinEvent {
    override val key = "spin.rolled_back"
}
```

---

### 15. Complex DAO Extension Functions
**Location:** `domain/game/dao/GameDao.kt`

```kotlin
// CURRENT - COMPLEX JOINS HIDDEN IN EXTENSION
fun GameTable.full() = this
    .innerJoin(ProviderTable, ...)
    .innerJoin(AggregatorInfoTable, ...)
    .innerJoin(GameVariantTable, ...)
    .selectAll()
```

**Recommendation:**
- [ ] Move to repository implementations
- [ ] Make join logic explicit and documented
- [ ] Consider using views or stored procedures for complex queries

---

## Low Priority / Nice to Have

### 16. File Naming Conventions
**Current:** `String.ext.kt`, `Column.ext.kt`

**Recommendation:**
- [ ] Rename to standard Kotlin conventions:
  - `StringExtensions.kt` or `StringExt.kt`
  - `ColumnExtensions.kt` or `ColumnExt.kt`

---

### 17. Add Validation Layer
**Recommendation:**
- [ ] Create validation utilities for input data:
```kotlin
object GameValidation {
    fun validateIdentity(identity: String): Result<String>
    fun validateName(name: String): Result<String>
}
```
- [ ] Use validation in usecases before processing

---

### 18. Add Unit Tests
**Recommendation:**
- [ ] Add tests for all usecases
- [ ] Add tests for repositories (with in-memory DB)
- [ ] Add tests for domain models
- [ ] Add tests for mappers
- [ ] Target: 80% code coverage

---

## Recommended New Package Structure

```
core/src/main/kotlin/
├── application/                    # Application layer (was: app/)
│   ├── usecase/                   # Business use cases
│   │   ├── game/
│   │   ├── session/
│   │   ├── spin/
│   │   ├── collection/
│   │   ├── provider/
│   │   └── aggregator/
│   ├── service/                   # Application services
│   ├── port/                      # Port interfaces (was: adapter/)
│   │   ├── inbound/              # Driving ports
│   │   └── outbound/             # Driven ports (WalletPort, PlayerPort, etc.)
│   └── event/                     # Application events
│
├── domain/                        # Domain layer (keep as is, enhance)
│   ├── game/
│   │   ├── model/                # Domain models with logic
│   │   ├── repository/           # Repository interfaces (NEW)
│   │   └── error/                # Domain-specific errors
│   ├── session/
│   ├── spin/
│   ├── collection/
│   ├── provider/
│   └── aggregator/
│
├── infrastructure/                # Infrastructure layer
│   ├── persistence/              # Database implementations (NEW)
│   │   ├── exposed/             # Exposed ORM implementations
│   │   │   ├── table/           # Table definitions
│   │   │   ├── mapper/          # DB mappers
│   │   │   └── repository/      # Repository implementations
│   │   └── migration/           # DB migrations
│   ├── messaging/                # RabbitMQ implementations
│   ├── cache/                    # Cache implementations
│   ├── http/                     # HTTP client configurations
│   └── aggregator/               # Aggregator adapters
│       └── onegamehub/
│
├── config/                        # Configuration (was: Module.kt)
│   ├── DependencyInjection.kt
│   ├── DatabaseConfig.kt
│   └── MessagingConfig.kt
│
└── shared/                        # Cross-cutting concerns (was: core/)
    ├── error/                    # Base error types
    ├── extension/                # Kotlin extensions
    ├── serializer/               # Custom serializers
    └── value/                    # Value objects
```

---

## Priority Matrix

| Priority | Issue | Effort | Impact |
|----------|-------|--------|--------|
| P0 | Fake adapters in production | Medium | Critical |
| P0 | Service Locator anti-pattern | High | High |
| P1 | No Repository pattern | High | High |
| P1 | ListGameUsecase refactor | Medium | High |
| P1 | Mixed exception types | Medium | Medium |
| P2 | Wrong package declaration | Low | Low |
| P2 | Inconsistent service patterns | Medium | Medium |
| P2 | HttpClient resource leak | Low | Medium |
| P2 | In-memory cache | Medium | Medium |
| P3 | Anemic domain models | High | Medium |
| P3 | Bloated GameFull | Medium | Low |
| P3 | Extension mappers | Medium | Low |
| P3 | Event data size | Low | Low |
| P3 | Event type splitting | Low | Low |

---

## Suggested Refactoring Order

1. **Week 1:** Fix critical issues
   - [ ] Implement real WalletAdapter and PlayerAdapter
   - [ ] Fix wrong package declaration

2. **Week 2-3:** Implement Repository pattern
   - [ ] Create repository interfaces
   - [ ] Create Exposed implementations
   - [ ] Migrate usecases to use repositories

3. **Week 4:** Constructor injection
   - [ ] Refactor all usecases to constructor injection
   - [ ] Refactor all services to constructor injection
   - [ ] Update Module.kt

4. **Week 5:** Clean up
   - [ ] Refactor ListGameUsecase
   - [ ] Standardize error handling
   - [ ] Fix HttpClient lifecycle

5. **Ongoing:** Improvements
   - [ ] Add domain logic to models
   - [ ] Split events
   - [ ] Add tests
   - [ ] Implement Redis cache

---

*Generated: 2025-11-27*
