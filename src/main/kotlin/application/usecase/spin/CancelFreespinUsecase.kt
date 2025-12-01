package application.usecase.spin

import application.port.outbound.AggregatorAdapterRegistry
import application.service.GameService
import domain.common.error.AggregatorNotSupportedError

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

        return freespinAdapter.cancelFreespin(referenceId = referenceId,)
    }
}
