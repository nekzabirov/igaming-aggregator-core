package application.usecase.spin

import application.port.outbound.AggregatorAdapterRegistry
import application.service.GameService
import domain.common.error.AggregatorNotSupportedError
import domain.common.error.InvalidPresetError
import shared.value.Currency
import kotlinx.datetime.LocalDateTime

/**
 * Use case for creating a freespin.
 */
class CreateFreespinUsecase(
    private val gameService: GameService,
    private val aggregatorRegistry: AggregatorAdapterRegistry
) {
    suspend operator fun invoke(
        presetValue: Map<String, Int>,
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
