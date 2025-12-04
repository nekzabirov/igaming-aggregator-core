# iGambling Core Service

A private, open-source Kotlin service for iGambling operations, providing unified game aggregator integration, session management, betting operations, and event-driven architecture.

> **IMPORTANT:** This is a **private service** designed to run behind your own infrastructure. It requires custom adapters for wallet, player, and other integrations. You must implement a public-facing decorator/API layer for client access.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Use Cases](#use-cases)
4. [gRPC API Documentation](#grpc-api-documentation)
5. [Supported Aggregators](#supported-aggregators)
6. [Integrating a New Aggregator](#integrating-a-new-aggregator)
7. [Custom Adapters (Required)](#custom-adapters-required)
8. [Event System](#event-system)
9. [How the Service Works](#how-the-service-works)
10. [Configuration](#configuration)
11. [Error Handling](#error-handling)

---

## Architecture Overview

The service follows **Hexagonal Architecture** (Ports & Adapters) with clean separation:

```
┌─────────────────────────────────────────────────────────────────────┐
│                         API Layer (gRPC/REST)                       │
├─────────────────────────────────────────────────────────────────────┤
│                       Application Layer                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │  Use Cases  │  │  Services   │  │   Events    │  │  Handlers   │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │
├─────────────────────────────────────────────────────────────────────┤
│                         Domain Layer                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                  │
│  │  Entities   │  │Repositories │  │   Errors    │                  │
│  └─────────────┘  └─────────────┘  └─────────────┘                  │
├─────────────────────────────────────────────────────────────────────┤
│                      Infrastructure Layer                            │
│  ┌────────────────┐  ┌──────────────┐  ┌───────────────────────────┐│
│  │   Aggregators  │  │  Persistence │  │  Adapters (YOU IMPLEMENT) ││
│  │ (Pragmatic,    │  │  (Exposed)   │  │  - WalletAdapter          ││
│  │  OneGameHub,   │  │              │  │  - PlayerAdapter          ││
│  │  Pateplay)     │  │              │  │  - CacheAdapter           ││
│  └────────────────┘  └──────────────┘  └───────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin (JVM 21) |
| Framework | Ktor Server |
| Database | Exposed ORM (H2/PostgreSQL) |
| DI | Koin |
| Messaging | RabbitMQ |
| API | gRPC + REST |
| Serialization | kotlinx.serialization |
| Build | Gradle (Kotlin DSL) |

---

## Use Cases

### Session Management
| Use Case | Description |
|----------|-------------|
| `OpenSessionUsecase` | Opens a new game session, generates token, gets launch URL from aggregator |

### Spin (Betting) Operations
| Use Case | Description |
|----------|-------------|
| `PlaceSpinUsecase` | Places a bet, withdraws from wallet, creates spin record |
| `SettleSpinUsecase` | Settles spin outcome (win/loss), deposits winnings |
| `RollbackUsecase` | Rolls back a transaction, refunds the bet amount |
| `GetPresetUsecase` | Gets freespin preset configuration from aggregator |
| `CreateFreespinUsecase` | Creates a freespin bonus for a player |
| `CancelFreespinUsecase` | Cancels an active freespin |

### Game Management
| Use Case | Description |
|----------|-------------|
| `ListGamesUsecase` | Lists games with filtering and pagination |
| `UpdateGameUsecase` | Updates game configuration (active, bonus settings) |
| `AddGameTagUsecase` | Adds a tag to a game |
| `RemoveGameTagUsecase` | Removes a tag from a game |
| `AddGameFavouriteUsecase` | Adds game to player's favorites |
| `RemoveGameFavouriteUsecase` | Removes game from favorites |
| `AddGameWinUsecase` | Records a game win for display |
| `DemoGameUsecase` | Gets demo game launch URL |

### Collection Management
| Use Case | Description |
|----------|-------------|
| `AddCollectionUsecase` | Creates a new game collection |
| `UpdateCollectionUsecase` | Updates collection (name, order, active) |
| `AddGameCollectionUsecase` | Adds a game to a collection |
| `RemoveGameCollectionUsecase` | Removes a game from a collection |
| `ChangeGameOrderUsecase` | Changes game order within a collection |
| `ListCollectionUsecase` | Lists collections with pagination |

### Provider Management
| Use Case | Description |
|----------|-------------|
| `ProviderListUsecase` | Lists game providers |
| `UpdateProviderUsecase` | Updates provider configuration |
| `AssignProviderToAggregatorUsecase` | Assigns a provider to an aggregator |

### Aggregator Management
| Use Case | Description |
|----------|-------------|
| `AddAggregatorUsecase` | Registers a new aggregator with config |
| `ListAggregatorUsecase` | Lists registered aggregators |
| `ListAllActiveAggregatorUsecase` | Lists all active aggregators |
| `ListGameVariantsUsecase` | Lists game variants from aggregators |
| `SyncGameUsecase` | Syncs games from aggregator to local DB |

---

## gRPC API Documentation

### Session Service

```protobuf
service Session {
  rpc OpenSession (OpenSessionCommand) returns (OpenSessionResult);
}

message OpenSessionCommand {
  string game_identity = 1;    // Game identifier (e.g., "gates-of-olympus")
  string player_id = 2;        // Your player ID
  string currency = 3;         // Currency code (e.g., "EUR", "USD")
  string locale = 4;           // Locale (e.g., "en", "de")
  PlatformType platform = 5;   // DESKTOP, MOBILE, DOWNLOAD
  string lobby_url = 6;        // Return URL after game exit
}

message OpenSessionResult {
  string launch_url = 1;       // URL to launch the game
}
```

### Game Service

```protobuf
service Game {
  rpc List (ListGameCommand) returns (ListGameResult);
  rpc Update (UpdateGameConfig) returns (EmptyResult);
  rpc AddTag (GameTagCommand) returns (EmptyResult);
  rpc RemoveTag (GameTagCommand) returns (EmptyResult);
  rpc AddFavourite (GameFavouriteCommand) returns (EmptyResult);
  rpc RemoveFavourite (GameFavouriteCommand) returns (EmptyResult);
  rpc DemoGame (DemoGameCommand) returns (DemoGameResult);
}

message ListGameCommand {
  string query = 1;                        // Search query
  optional bool active = 2;                // Filter by active status
  int32 page_number = 3;
  int32 page_size = 4;
  optional bool bonus_bet = 5;             // Filter by bonus bet support
  optional bool bonus_wagering = 6;        // Filter by wagering support
  optional bool free_spin_enable = 7;      // Filter by freespin support
  optional bool free_chip_enable = 8;
  optional bool jackpot_enable = 9;
  optional bool demo_enable = 10;
  optional bool bonus_buy_enable = 11;
  repeated PlatformType platforms = 12;    // Filter by platforms
  repeated string provider_identity = 13;  // Filter by providers
  repeated string category_identity = 14;  // Filter by categories
  repeated string tags = 15;               // Filter by tags
  optional string player_id = 16;          // For favorites filtering
}

message ListGameResult {
  repeated Item items = 1;                 // Game items with variants
  repeated ProviderDto providers = 2;      // Available providers
  repeated CollectionDto collections = 3;  // Available collections
  int32 total_page = 4;
}
```

### Freespin Service

```protobuf
service Freespin {
  rpc GetPreset (GetPresetCommand) returns (GetPresetResult);
  rpc CreateFreespin (CreateFreespinCommand) returns (EmptyResult);
  rpc CancelFreespin (CancelFreespinCommand) returns (EmptyResult);
}

message CreateFreespinCommand {
  map<string, int32> preset_value = 1;     // Preset configuration
  string reference_id = 2;                  // Your reference ID
  string player_id = 3;
  string game_identity = 4;
  string currency = 5;
  google.protobuf.Timestamp start_at = 6;
  google.protobuf.Timestamp end_at = 7;
}
```

### Collection Service

```protobuf
service Collection {
  rpc AddCollection(AddCollectionCommand) returns (EmptyResult);
  rpc UpdateCollection(UpdateCollectionCommand) returns (EmptyResult);
  rpc AddGameCollection(AddGameCollectionCommand) returns (EmptyResult);
  rpc ChangeGameOrder(ChangeGameOrderCollectionCommand) returns (EmptyResult);
  rpc RemoveGameFromCollection(AddGameCollectionCommand) returns (EmptyResult);
  rpc List(ListCollectionCommand) returns (ListCollectionResult);
}

message AddCollectionCommand {
  string identity = 1;                     // Collection identifier
  map<string, string> name = 2;            // Localized names {"en": "Popular", "de": "Beliebt"}
}
```

### Provider Service

```protobuf
service Provider {
  rpc List (ListProviderCommand) returns (ListProviderResult);
  rpc Update (UpdateProviderConfig) returns (EmptyResult);
}
```

### Sync Service

```protobuf
service Sync {
  rpc AddAggregator(AddAggregatorCommand) returns (EmptyResult);
  rpc ListAggregator(ListAggregatorCommand) returns (ListAggregatorResult);
  rpc AssignGameVariant(AssignGameVariantCommand) returns (EmptyResult);
  rpc AssignProvider(AssignProviderCommand) returns (EmptyResult);
  rpc ListVariants(ListVariantsCommand) returns (ListVariantResult);
}

message AddAggregatorCommand {
  string identity = 1;                     // Unique aggregator identifier
  string type = 2;                         // PRAGMATIC, ONEGAMEHUB, PATEPLAY
  map<string, string> config = 3;          // Aggregator-specific configuration
}
```

---

## Supported Aggregators

### 1. Pragmatic Play

**Type:** `PRAGMATIC`

**Configuration:**
| Key | Description | Required |
|-----|-------------|----------|
| `secretKey` | API secret key provided by Pragmatic | Yes |
| `secureLogin` | Secure login identifier | Yes |
| `gatewayUrl` | Pragmatic API gateway URL | Yes |

**Example:**
```kotlin
mapOf(
    "secretKey" to "your-secret-key",
    "secureLogin" to "your-secure-login",
    "gatewayUrl" to "https://api.pragmaticplay.net"
)
```

**Callback Endpoints (implemented by this service):**
- `authenticate` - Validates session token
- `balance` - Returns player balance
- `bet` - Processes bet placement
- `result` - Processes spin result/win
- `endRound` - Closes the round
- `refund` - Refunds a transaction
- `adjustment` - Manual balance adjustment

---

### 2. OneGameHub

**Type:** `ONEGAMEHUB`

**Configuration:**
| Key | Description | Required |
|-----|-------------|----------|
| `salt` | Encryption salt | Yes |
| `secret` | API secret | Yes |
| `partner` | Partner identifier | Yes |
| `gateway` | OneGameHub API gateway URL | Yes |

**Example:**
```kotlin
mapOf(
    "salt" to "your-salt",
    "secret" to "your-secret",
    "partner" to "your-partner-id",
    "gateway" to "https://api.onegamehub.com"
)
```

**Callback Endpoints:**
- `balance` - Returns player balance
- `bet` - Processes bet
- `win` - Processes win
- `cancel` - Cancels/refunds transaction

---

### 3. Pateplay

**Type:** `PATEPLAY`

**Configuration:**
| Key | Description | Required |
|-----|-------------|----------|
| `gatewayUrl` | Pateplay API gateway URL | Yes |
| `siteCode` | Site identifier | Yes |
| `gatewayApiKey` | API key for gateway | Yes |
| `gatewayApiSecret` | API secret for gateway | Yes |
| `gameLaunchUrl` | Base URL for game launch | Yes |
| `gameDemoLaunchUrl` | Base URL for demo games | Yes |
| `walletApiKey` | Wallet API key | Yes |
| `walletApiSecret` | Wallet API secret | Yes |

**Example:**
```kotlin
mapOf(
    "gatewayUrl" to "https://api.pateplay.com",
    "siteCode" to "your-site-code",
    "gatewayApiKey" to "your-api-key",
    "gatewayApiSecret" to "your-api-secret",
    "gameLaunchUrl" to "https://games.pateplay.com/launch",
    "gameDemoLaunchUrl" to "https://games.pateplay.com/demo",
    "walletApiKey" to "your-wallet-key",
    "walletApiSecret" to "your-wallet-secret"
)
```

---

## Integrating a New Aggregator

To integrate a new aggregator, follow these steps:

### Step 1: Add Aggregator Enum

Edit `src/main/kotlin/shared/value/Enums.kt`:
```kotlin
enum class Aggregator {
    ONEGAMEHUB,
    PRAGMATIC,
    PATEPLAY,
    YOUR_AGGREGATOR  // Add here
}
```

### Step 2: Create Configuration Model

Create `src/main/kotlin/infrastructure/aggregator/youraggregator/model/YourAggregatorConfig.kt`:
```kotlin
package infrastructure.aggregator.youraggregator.model

internal class YourAggregatorConfig(private val config: Map<String, String>) {
    val apiKey = config["apiKey"] ?: ""
    val secretKey = config["secretKey"] ?: ""
    val gatewayUrl = config["gatewayUrl"] ?: ""
    // Add your config properties
}
```

### Step 3: Implement Adapters

Create the following adapters in `infrastructure/aggregator/youraggregator/adapter/`:

**Launch URL Adapter:**
```kotlin
class YourAggregatorLaunchUrlAdapter(
    private val aggregatorInfo: AggregatorInfo
) : AggregatorLaunchUrlPort {

    override suspend fun getLaunchUrl(
        gameSymbol: String,
        sessionToken: String,
        playerId: String,
        locale: Locale,
        platform: Platform,
        currency: Currency,
        lobbyUrl: String,
        demo: Boolean
    ): Result<String> {
        // Implement launch URL generation
    }
}
```

**Freespin Adapter:**
```kotlin
class YourAggregatorFreespinAdapter(
    private val aggregatorInfo: AggregatorInfo
) : AggregatorFreespinPort {

    override suspend fun getPreset(gameSymbol: String): Result<Map<String, Any>> { ... }
    override suspend fun createFreespin(...): Result<Unit> { ... }
    override suspend fun cancelFreespin(referenceId: String): Result<Unit> { ... }
}
```

**Game Sync Adapter:**
```kotlin
class YourAggregatorGameSyncAdapter(
    private val aggregatorInfo: AggregatorInfo
) : AggregatorGameSyncPort {

    override suspend fun listGames(): Result<List<AggregatorGame>> {
        // Fetch games from aggregator API
    }
}
```

### Step 4: Create Adapter Factory

```kotlin
class YourAggregatorAdapterFactory : AggregatorAdapterFactory {

    override fun supports(aggregator: Aggregator): Boolean {
        return aggregator == Aggregator.YOUR_AGGREGATOR
    }

    override fun createLaunchUrlAdapter(aggregatorInfo: AggregatorInfo): AggregatorLaunchUrlPort {
        return YourAggregatorLaunchUrlAdapter(aggregatorInfo)
    }

    override fun createFreespinAdapter(aggregatorInfo: AggregatorInfo): AggregatorFreespinPort {
        return YourAggregatorFreespinAdapter(aggregatorInfo)
    }

    override fun createGameSyncAdapter(aggregatorInfo: AggregatorInfo): AggregatorGameSyncPort {
        return YourAggregatorGameSyncAdapter(aggregatorInfo)
    }
}
```

### Step 5: Implement Callback Handler

Aggregators send callbacks for betting operations. Create a handler:

```kotlin
class YourAggregatorHandler(
    private val sessionService: SessionService,
    private val walletAdapter: WalletAdapter,
    private val placeSpinUsecase: PlaceSpinUsecase,
    private val settleSpinUsecase: SettleSpinUsecase,
    private val rollbackUsecase: RollbackUsecase
) {
    suspend fun balance(token: SessionToken): YourResponse { ... }
    suspend fun bet(token: SessionToken, payload: BetPayload): YourResponse { ... }
    suspend fun win(token: SessionToken, payload: WinPayload): YourResponse { ... }
    suspend fun refund(token: SessionToken, transactionId: String): YourResponse { ... }
}
```

### Step 6: Create Route Handler

```kotlin
fun Route.yourAggregatorRoutes(handler: YourAggregatorHandler) {
    route("/callback/youraggregator") {
        post("/balance") { ... }
        post("/bet") { ... }
        post("/win") { ... }
        post("/refund") { ... }
    }
}
```

### Step 7: Register in Koin Module

Create `YourAggregatorModule.kt`:
```kotlin
val YourAggregatorModule = module {
    single { YourAggregatorAdapterFactory() }
    single { YourAggregatorHandler(get(), get(), get(), get(), get()) }
}
```

Update `AggregatorModule.kt`:
```kotlin
val AggregatorModule = module {
    includes(OneGameHubModule)
    includes(PragmaticModule)
    includes(PateplayModule)
    includes(YourAggregatorModule)  // Add here

    factory<AggregatorAdapterRegistry> {
        AggregatorAdapterRegistryImpl(get(), get(), get(), get())  // Add factory
    }
}
```

---

## Custom Adapters (Required)

**This service ships with fake/mock adapters. You MUST implement real adapters for production.**

### WalletAdapter

Interface: `application/port/outbound/WalletAdapter.kt`

```kotlin
interface WalletAdapter {
    /**
     * Get player's current balance.
     */
    suspend fun findBalance(playerId: String): Result<Balance>

    /**
     * Withdraw funds from player's wallet.
     * Called when placing a bet.
     */
    suspend fun withdraw(
        playerId: String,
        transactionId: String,
        currency: Currency,
        realAmount: BigInteger,
        bonusAmount: BigInteger
    ): Result<Unit>

    /**
     * Deposit funds to player's wallet.
     * Called when settling a win.
     */
    suspend fun deposit(
        playerId: String,
        transactionId: String,
        currency: Currency,
        realAmount: BigInteger,
        bonusAmount: BigInteger
    ): Result<Unit>

    /**
     * Rollback a previous transaction.
     * Called for refunds/cancellations.
     */
    suspend fun rollback(playerId: String, transactionId: String): Result<Unit>
}
```

### PlayerAdapter

Interface: `application/port/outbound/PlayerAdapter.kt`

```kotlin
interface PlayerAdapter {
    /**
     * Get player's current bet limit.
     * Returns null if no limit is set.
     */
    suspend fun findCurrentBetLimit(playerId: String): Result<BigInteger?>
}
```

### CacheAdapter

Interface: `application/port/outbound/CacheAdapter.kt`

```kotlin
interface CacheAdapter {
    suspend fun <T : Any> get(key: String): T?
    suspend fun <T : Any> save(key: String, value: T, ttl: Duration? = null)
    suspend fun delete(key: String): Boolean
    suspend fun exists(key: String): Boolean
    suspend fun clear()
}
```

### EventPublisherAdapter

Interface: `application/port/outbound/EventPublisherAdapter.kt`

```kotlin
interface EventPublisherAdapter {
    suspend fun publish(event: DomainEvent)
    suspend fun publishAll(events: List<DomainEvent>)
}
```

### Registering Custom Adapters

Update `infrastructure/DependencyInjection.kt`:

```kotlin
private val adapterModule = module {
    // Replace fake adapters with your implementations
    single<WalletAdapter> { YourWalletAdapter(/* dependencies */) }
    single<PlayerAdapter> { YourPlayerAdapter(/* dependencies */) }
    single<CacheAdapter> { YourCacheAdapter(/* dependencies */) }
}
```

---

## Event System

The service publishes domain events via RabbitMQ. Subscribe to these events for analytics, notifications, etc.

### Available Events

| Event | Routing Key | Description |
|-------|-------------|-------------|
| `SpinPlacedEvent` | `spin.placed` | Bet was placed |
| `SpinSettledEvent` | `spin.settled` | Spin result settled (win/loss) |
| `SessionOpenedEvent` | `session.opened` | New session created |
| `GameFavouriteAddedEvent` | `game.favourite.added` | Game added to favorites |
| `GameFavouriteRemovedEvent` | `game.favourite.removed` | Game removed from favorites |
| `GameWonEvent` | `game.won` | Win recorded |

### Event Payloads

**SpinPlacedEvent / SpinSettledEvent:**
```kotlin
data class SpinPlacedEvent(
    val gameIdentity: String,
    val amount: BigInteger,
    val currency: Currency,
    val playerId: String,
    val freeSpinId: String?,
    val timestamp: Long
)
```

**SessionOpenedEvent:**
```kotlin
data class SessionOpenedEvent(
    val sessionId: String,
    val gameId: String,
    val gameIdentity: String,
    val playerId: String,
    val currency: Currency,
    val platform: String,
    val timestamp: Long
)
```

---

## How the Service Works

### Session Flow

```
┌──────────┐    1. OpenSession     ┌─────────────┐    2. Get Launch URL    ┌────────────┐
│  Client  │ ──────────────────────► │ iGambling   │ ───────────────────────► │ Aggregator │
│          │ ◄────────────────────── │   Service   │ ◄─────────────────────── │            │
└──────────┘    4. Launch URL      └─────────────┘    3. Launch URL        └────────────┘
     │                                     │
     │         5. Launch Game              │
     └─────────────────────────────────────┼──────────────────────────────────────►
                                           │
                                           │  6. Store Session
                                           ▼
                                    ┌─────────────┐
                                    │  Database   │
                                    └─────────────┘
```

### Betting Flow

```
┌────────────┐    1. Bet Callback     ┌─────────────┐    2. Validate Session
│ Aggregator │ ───────────────────────► │ iGambling   │ ───────────────────────►
│            │ ◄─────────────────────── │   Handler   │
└────────────┘    6. Balance Response  └─────────────┘
                                              │
                                              │ 3. Place Spin
                                              ▼
                                       ┌─────────────┐    4. Withdraw
                                       │ SpinService │ ───────────────────────►
                                       └─────────────┘
                                              │
                                              │ 5. Publish Event
                                              ▼
┌────────────┐                         ┌─────────────┐
│  RabbitMQ  │ ◄─────────────────────── │   Events    │
└────────────┘                         └─────────────┘

                                                       ┌─────────────────────┐
                                                       │  WalletAdapter      │
                                                       │  (YOUR IMPL)        │
                                                       └─────────────────────┘
```

### Round Lifecycle

1. **First Bet** → Round created with `extId` from aggregator
2. **Additional Bets** → Same round used
3. **Settle** → Win/loss recorded, funds deposited
4. **End Round** → Round marked as finished
5. **Rollback** → Previous spin reversed, funds refunded

---

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | JDBC database URL | `jdbc:h2:mem:test` |
| `DATABASE_DRIVER` | JDBC driver class | `org.h2.Driver` |
| `DATABASE_USER` | Database username | (empty) |
| `DATABASE_PASSWORD` | Database password | (empty) |

### RabbitMQ Configuration

Configure in `messagingModule`:
- Exchange: Domain events exchange
- Queues: Spin settled consumer, etc.

---

## Error Handling

The service uses typed domain errors:

| Error | Code | Description |
|-------|------|-------------|
| `NotFoundError` | `NOT_FOUND` | Entity not found |
| `ValidationError` | `VALIDATION_ERROR` | Input validation failed |
| `InsufficientBalanceError` | `INSUFFICIENT_BALANCE` | Not enough funds |
| `BetLimitExceededError` | `BET_LIMIT_EXCEEDED` | Bet exceeds limit |
| `SessionInvalidError` | `SESSION_INVALID` | Session expired/invalid |
| `GameUnavailableError` | `GAME_UNAVAILABLE` | Game not playable |
| `RoundFinishedError` | `ROUND_FINISHED` | Round already closed |
| `RoundNotFoundError` | `ROUND_NOT_FOUND` | Round doesn't exist |
| `InvalidPresetError` | `INVALID_PRESET` | Bad freespin config |
| `ExternalServiceError` | `EXTERNAL_SERVICE_ERROR` | External API error |
| `AggregatorNotSupportedError` | `AGGREGATOR_NOT_SUPPORTED` | Unknown aggregator |

Aggregator handlers map these to provider-specific error codes.

---

## Public API Decorator

**This service is private and should NOT be exposed directly to clients.**

You must implement a public-facing decorator that:

1. **Authenticates** requests (JWT, API keys, etc.)
2. **Authorizes** player access
3. **Rate limits** requests
4. **Logs** and monitors traffic
5. **Transforms** responses for your client format

Example architecture:

```
┌──────────┐      ┌────────────────┐      ┌─────────────────┐
│  Client  │ ───► │  Your Public   │ ───► │  iGambling      │
│          │ ◄─── │  API Gateway   │ ◄─── │  Core Service   │
└──────────┘      └────────────────┘      └─────────────────┘
                         │
                         ▼
                  ┌─────────────┐
                  │ Auth/Rate   │
                  │ Limiting    │
                  └─────────────┘
```

---