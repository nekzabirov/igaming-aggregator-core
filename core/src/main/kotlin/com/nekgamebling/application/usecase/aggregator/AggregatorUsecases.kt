package com.nekgamebling.application.usecase.aggregator

import com.nekgamebling.application.port.outbound.AggregatorAdapterRegistry
import com.nekgamebling.domain.aggregator.model.AggregatorInfo
import com.nekgamebling.domain.aggregator.repository.AggregatorRepository
import com.nekgamebling.domain.common.error.AggregatorNotSupportedError
import com.nekgamebling.domain.common.error.DuplicateEntityError
import com.nekgamebling.domain.common.error.NotFoundError
import com.nekgamebling.domain.game.model.Game
import com.nekgamebling.domain.game.model.GameVariant
import com.nekgamebling.domain.game.repository.GameRepository
import com.nekgamebling.domain.game.repository.GameVariantRepository
import com.nekgamebling.domain.provider.model.Provider
import com.nekgamebling.domain.provider.repository.ProviderRepository
import com.nekgamebling.shared.value.Aggregator
import com.nekgamebling.shared.value.ImageMap
import com.nekgamebling.shared.value.Page
import com.nekgamebling.shared.value.Pageable
import com.nekgamebling.shared.value.Platform
import java.util.UUID

/**
 * Use case for adding a new aggregator.
 */
class AddAggregatorUsecase(
    private val aggregatorRepository: AggregatorRepository
) {
    suspend operator fun invoke(
        identity: String,
        aggregator: Aggregator,
        config: Map<String, String>
    ): Result<AggregatorInfo> {
        if (aggregatorRepository.existsByIdentity(identity)) {
            return Result.failure(DuplicateEntityError("Aggregator", identity))
        }

        val aggregatorInfo = AggregatorInfo(
            id = UUID.randomUUID(),
            identity = identity,
            config = config,
            aggregator = aggregator,
            active = true
        )

        return try {
            val saved = aggregatorRepository.save(aggregatorInfo)
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Filter for listing aggregators.
 */
data class AggregatorFilter(
    val query: String = "",
    val active: Boolean? = null,
    val type: Aggregator? = null
) {
    class Builder {
        private var query: String = ""
        private var active: Boolean? = null
        private var type: Aggregator? = null

        fun withQuery(query: String) = apply { this.query = query }
        fun withActive(active: Boolean?) = apply { this.active = active }
        fun withType(type: Aggregator?) = apply { this.type = type }

        fun build() = AggregatorFilter(query, active, type)
    }
}

/**
 * Use case for listing aggregators.
 */
class ListAggregatorUsecase(
    private val aggregatorRepository: AggregatorRepository
) {
    suspend operator fun invoke(
        pageable: Pageable,
        filterBuilder: (AggregatorFilter.Builder) -> Unit = {}
    ): Page<AggregatorInfo> {
        val filter = AggregatorFilter.Builder().also(filterBuilder).build()

        val page = aggregatorRepository.findAll(pageable)

        val filteredItems = page.items.filter { aggregator ->
            val matchesQuery = filter.query.isBlank() ||
                    aggregator.identity.contains(filter.query, ignoreCase = true)
            val matchesActive = filter.active == null || aggregator.active == filter.active
            val matchesType = filter.type == null || aggregator.aggregator == filter.type

            matchesQuery && matchesActive && matchesType
        }

        return Page(
            items = filteredItems,
            totalPages = page.totalPages,
            totalItems = page.totalItems,
            currentPage = page.currentPage
        )
    }
}

/**
 * Use case for listing all active aggregators.
 */
class ListAllActiveAggregatorUsecase(
    private val aggregatorRepository: AggregatorRepository
) {
    suspend operator fun invoke(): List<AggregatorInfo> {
        return aggregatorRepository.findAllActive()
    }
}

/**
 * Game variant list item with game info.
 */
data class GameVariantListItem(
    val gameVariant: GameVariant,
    val game: com.nekgamebling.domain.game.repository.GameListItem
)

/**
 * Filter for game variants.
 */
data class GameVariantFilter(
    val query: String = "",
    val aggregator: Aggregator? = null,
    val gameIdentity: String? = null
) {
    class Builder {
        private var query: String = ""
        private var aggregator: Aggregator? = null
        private var gameIdentity: String? = null

        fun withQuery(query: String) = apply { this.query = query }
        fun withAggregator(aggregator: Aggregator?) = apply { this.aggregator = aggregator }
        fun withGameIdentity(gameIdentity: String?) = apply { this.gameIdentity = gameIdentity }

        fun build() = GameVariantFilter(query, aggregator, gameIdentity)
    }
}

/**
 * Use case for listing game variants.
 */
class ListGameVariantsUsecase(
    private val gameVariantRepository: GameVariantRepository,
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(
        pageable: Pageable,
        filterBuilder: GameVariantFilter.Builder.() -> Unit = {}
    ): Page<GameVariantListItem> {
        val filter = GameVariantFilter.Builder().apply(filterBuilder).build()

        val page = gameVariantRepository.findAll(pageable)

        // TODO: Implement proper filtering
        val items = page.items.mapNotNull { variant ->
            if (filter.aggregator != null && variant.aggregator != filter.aggregator) return@mapNotNull null
            if (filter.query.isNotBlank() && !variant.name.contains(filter.query, ignoreCase = true)) return@mapNotNull null

            val gameId = variant.gameId ?: return@mapNotNull null
            val game = gameRepository.findById(gameId) ?: return@mapNotNull null

            // Create a minimal GameListItem - this is a simplification
            val gameListItem = com.nekgamebling.domain.game.repository.GameListItem(
                game = game,
                variant = variant,
                provider = Provider(
                    id = game.providerId,
                    identity = "",
                    name = variant.providerName,
                    images = ImageMap.EMPTY
                ),
                collections = emptyList()
            )

            GameVariantListItem(variant, gameListItem)
        }

        return Page(
            items = items,
            totalPages = page.totalPages,
            totalItems = page.totalItems,
            currentPage = page.currentPage
        )
    }
}

/**
 * Result of game sync operation.
 */
data class SyncGameResult(
    val gameCount: Int,
    val providerCount: Int
)

/**
 * Use case for syncing games from an aggregator.
 */
class SyncGameUsecase(
    private val aggregatorRepository: AggregatorRepository,
    private val providerRepository: ProviderRepository,
    private val gameRepository: GameRepository,
    private val gameVariantRepository: GameVariantRepository,
    private val aggregatorRegistry: AggregatorAdapterRegistry
) {
    suspend operator fun invoke(aggregatorIdentity: String): Result<SyncGameResult> {
        val aggregatorInfo = aggregatorRepository.findByIdentity(aggregatorIdentity)
            ?: return Result.failure(NotFoundError("Aggregator", aggregatorIdentity))

        val factory = aggregatorRegistry.getFactory(aggregatorInfo.aggregator)
            ?: return Result.failure(AggregatorNotSupportedError(aggregatorInfo.aggregator.name))

        val gameSyncAdapter = factory.createGameSyncAdapter(aggregatorInfo)

        val games = gameSyncAdapter.listGames(aggregatorInfo).getOrElse {
            return Result.failure(it)
        }

        var gameCount = 0
        val providerNames = mutableSetOf<String>()

        for (aggregatorGame in games) {
            providerNames.add(aggregatorGame.providerName)

            // Find or create provider
            val providerIdentity = aggregatorGame.providerName.lowercase().replace(" ", "-")
            var provider = providerRepository.findByIdentity(providerIdentity)

            if (provider == null) {
                provider = providerRepository.save(
                    Provider(
                        id = UUID.randomUUID(),
                        identity = providerIdentity,
                        name = aggregatorGame.providerName,
                        images = ImageMap.EMPTY,
                        aggregatorId = aggregatorInfo.id
                    )
                )
            }

            // Find or create game variant
            var variant = gameVariantRepository.findBySymbol(aggregatorGame.symbol)

            if (variant == null) {
                variant = gameVariantRepository.save(
                    GameVariant(
                        id = UUID.randomUUID(),
                        symbol = aggregatorGame.symbol,
                        name = aggregatorGame.name,
                        providerName = aggregatorGame.providerName,
                        aggregator = aggregatorInfo.aggregator,
                        freeSpinEnable = aggregatorGame.freeSpinEnable,
                        freeChipEnable = aggregatorGame.freeChipEnable,
                        jackpotEnable = aggregatorGame.jackpotEnable,
                        demoEnable = aggregatorGame.demoEnable,
                        bonusBuyEnable = aggregatorGame.bonusBuyEnable,
                        locales = aggregatorGame.locales,
                        platforms = aggregatorGame.platforms.map { Platform.valueOf(it) },
                        playLines = aggregatorGame.playLines
                    )
                )
            }

            // Find or create game
            val gameIdentity = aggregatorGame.symbol.lowercase().replace(" ", "-")
            var game = gameRepository.findByIdentity(gameIdentity)

            if (game == null) {
                game = gameRepository.save(
                    Game(
                        id = UUID.randomUUID(),
                        identity = gameIdentity,
                        name = aggregatorGame.name,
                        providerId = provider.id,
                        images = ImageMap.EMPTY
                    )
                )
            }

            // Link variant to game
            if (variant.gameId == null) {
                gameVariantRepository.linkToGame(variant.id, game.id)
            }

            gameCount++
        }

        return Result.success(SyncGameResult(gameCount, providerNames.size))
    }
}
