package com.nekgamebling.application.usecase.spin

import com.nekgamebling.application.port.outbound.AggregatorAdapterRegistry
import com.nekgamebling.application.service.GameService
import com.nekgamebling.domain.common.error.AggregatorNotSupportedError
import com.nekgamebling.domain.common.error.InvalidPresetError
import com.nekgamebling.shared.value.Currency
import kotlinx.datetime.LocalDateTime

/**
 * Result of getting preset.
 */
data class GetPresetResult(
    val preset: Map<String, Any?>
)

/**
 * Use case for getting freespin preset.
 */
class GetPresetUsecase(
    private val gameService: GameService,
    private val aggregatorRegistry: AggregatorAdapterRegistry
) {
    suspend operator fun invoke(gameIdentity: String): Result<GetPresetResult> {
        val game = gameService.findByIdentity(gameIdentity).getOrElse {
            return Result.failure(it)
        }

        val factory = aggregatorRegistry.getFactory(game.aggregator.aggregator)
            ?: return Result.failure(AggregatorNotSupportedError(game.aggregator.aggregator.name))

        val freespinAdapter = factory.createFreespinAdapter(game.aggregator)

        val preset = freespinAdapter.getPreset(game.symbol, game.aggregator.identity).getOrElse {
            return Result.failure(it)
        }

        return Result.success(GetPresetResult(preset))
    }
}

/**
 * Use case for creating a freespin.
 */
class CreateFreespinUsecase(
    private val gameService: GameService,
    private val aggregatorRegistry: AggregatorAdapterRegistry
) {
    suspend operator fun invoke(
        presetValue: Map<String, String>,
        referenceId: String,
        playerId: String,
        gameIdentity: String,
        currency: Currency,
        startAt: LocalDateTime,
        endAt: LocalDateTime
    ): Result<Unit> {
        val game = gameService.findByIdentity(gameIdentity).getOrElse {
            return Result.failure(it)
        }

        if (!game.freeSpinEnable) {
            return Result.failure(
                InvalidPresetError(gameIdentity, "Free spins not enabled for this game")
            )
        }

        val factory = aggregatorRegistry.getFactory(game.aggregator.aggregator)
            ?: return Result.failure(AggregatorNotSupportedError(game.aggregator.aggregator.name))

        val freespinAdapter = factory.createFreespinAdapter(game.aggregator)

        return freespinAdapter.createFreespin(
            aggregatorIdentity = game.aggregator.identity,
            presetValue = presetValue,
            referenceId = referenceId,
            playerId = playerId,
            gameSymbol = game.symbol,
            currency = currency,
            startAt = startAt,
            endAt = endAt
        )
    }
}

/**
 * Use case for canceling a freespin.
 */
class CancelFreespinUsecase(
    private val gameService: GameService,
    private val aggregatorRegistry: AggregatorAdapterRegistry
) {
    suspend operator fun invoke(referenceId: String, gameIdentity: String): Result<Unit> {
        val game = gameService.findByIdentity(gameIdentity).getOrElse {
            return Result.failure(it)
        }

        val factory = aggregatorRegistry.getFactory(game.aggregator.aggregator)
            ?: return Result.failure(AggregatorNotSupportedError(game.aggregator.aggregator.name))

        val freespinAdapter = factory.createFreespinAdapter(game.aggregator)

        return freespinAdapter.cancelFreespin(
            aggregatorIdentity = game.aggregator.identity,
            referenceId = referenceId,
            gameSymbol = game.symbol
        )
    }
}
