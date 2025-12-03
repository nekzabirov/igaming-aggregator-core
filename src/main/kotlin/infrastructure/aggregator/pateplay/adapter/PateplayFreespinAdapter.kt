package infrastructure.aggregator.pateplay.adapter

import application.port.outbound.AggregatorFreespinPort
import domain.aggregator.model.AggregatorInfo
import infrastructure.aggregator.pateplay.model.PateplayConfig
import kotlinx.datetime.LocalDateTime
import shared.value.Currency

/**
 * Pateplay implementation for freespin operations.
 */
class PateplayFreespinAdapter(
    aggregatorInfo: AggregatorInfo,
    private val providerCurrencyAdapter: PateplayCurrencyAdapter
) : AggregatorFreespinPort {

    private val config = PateplayConfig(aggregatorInfo.config)

    override suspend fun getPreset(gameSymbol: String): Result<Map<String, Any>> {
        // TODO: Implement Pateplay freespin preset retrieval
        return Result.failure(NotImplementedError("Pateplay getPreset not implemented yet"))
    }

    override suspend fun createFreespin(
        presetValue: Map<String, Int>,
        referenceId: String,
        playerId: String,
        gameSymbol: String,
        currency: Currency,
        startAt: LocalDateTime,
        endAt: LocalDateTime
    ): Result<Unit> {
        // TODO: Implement Pateplay freespin creation
        return Result.failure(NotImplementedError("Pateplay createFreespin not implemented yet"))
    }

    override suspend fun cancelFreespin(
        referenceId: String,
    ): Result<Unit> {
        // TODO: Implement Pateplay freespin cancellation
        return Result.failure(NotImplementedError("Pateplay cancelFreespin not implemented yet"))
    }
}
