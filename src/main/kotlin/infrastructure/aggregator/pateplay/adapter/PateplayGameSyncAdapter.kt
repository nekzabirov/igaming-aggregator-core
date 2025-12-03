package infrastructure.aggregator.pateplay.adapter

import application.port.outbound.AggregatorGameSyncPort
import domain.aggregator.model.AggregatorGame
import domain.aggregator.model.AggregatorInfo
import infrastructure.aggregator.pateplay.model.PateplayConfig

/**
 * Pateplay implementation for syncing games.
 */
class PateplayGameSyncAdapter(aggregatorInfo: AggregatorInfo) : AggregatorGameSyncPort {

    private val config = PateplayConfig(aggregatorInfo.config)

    override suspend fun listGames(): Result<List<AggregatorGame>> {
        // TODO: Implement Pateplay game list fetching
        return Result.failure(NotImplementedError("Pateplay listGames not implemented yet"))
    }
}
