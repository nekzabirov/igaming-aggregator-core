package com.nekgamebling.application.service

import com.nekgamebling.application.port.outbound.CachePort
import com.nekgamebling.domain.common.error.GameUnavailableError
import com.nekgamebling.domain.common.error.NotFoundError
import com.nekgamebling.domain.game.model.Game
import com.nekgamebling.domain.game.model.GameWithDetails
import com.nekgamebling.domain.game.repository.GameRepository
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

/**
 * Application service for game-related operations.
 * Uses constructor injection for all dependencies.
 */
class GameService(
    private val gameRepository: GameRepository,
    private val cachePort: CachePort
) {
    companion object {
        private val CACHE_TTL = 5.minutes
        private const val CACHE_PREFIX = "game:"
    }

    /**
     * Find game by identity with caching.
     */
    suspend fun findByIdentity(identity: String): Result<GameWithDetails> {
        // Check cache first
        cachePort.get<GameWithDetails>("$CACHE_PREFIX$identity")?.let {
            return Result.success(it)
        }

        val game = gameRepository.findWithDetailsByIdentity(identity)
            ?: return Result.failure(NotFoundError("Game", identity))

        // Cache the result
        cachePort.save("$CACHE_PREFIX$identity", game, CACHE_TTL)

        return Result.success(game)
    }

    /**
     * Find game by ID.
     */
    suspend fun findById(id: UUID): Result<GameWithDetails> {
        val cacheKey = "$CACHE_PREFIX$id"

        cachePort.get<GameWithDetails>(cacheKey)?.let {
            return Result.success(it)
        }

        val game = gameRepository.findWithDetailsById(id)
            ?: return Result.failure(NotFoundError("Game", id.toString()))

        cachePort.save(cacheKey, game, CACHE_TTL)

        return Result.success(game)
    }

    /**
     * Find game by symbol with caching.
     */
    suspend fun findBySymbol(symbol: String): Result<Game> {
        val cacheKey = "${CACHE_PREFIX}symbol:$symbol"

        cachePort.get<Game>(cacheKey)?.let {
            return Result.success(it)
        }

        val game = gameRepository.findBySymbol(symbol)
            ?: return Result.failure(GameUnavailableError(symbol))

        cachePort.save(cacheKey, game, CACHE_TTL)

        return Result.success(game)
    }

    /**
     * Invalidate cache for a game.
     */
    suspend fun invalidateCache(identity: String) {
        cachePort.delete("$CACHE_PREFIX$identity")
    }
}
