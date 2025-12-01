package application.usecase.spin

import application.port.outbound.AggregatorAdapterRegistry
import application.service.GameService
import domain.common.error.AggregatorNotSupportedError

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

        val preset = freespinAdapter.getPreset(game.symbol).getOrElse {
            return Result.failure(it)
        }

        return Result.success(GetPresetResult(preset))
    }
}
