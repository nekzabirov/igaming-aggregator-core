package infrastructure.aggregator.pateplay.adapter

import application.port.outbound.AggregatorLaunchUrlPort
import domain.aggregator.model.AggregatorInfo
import infrastructure.aggregator.pateplay.model.PateplayConfig
import shared.value.Currency
import shared.value.Locale
import shared.value.Platform

/**
 * Pateplay implementation for getting game launch URLs.
 */
class PateplayLaunchUrlAdapter(
    private val aggregatorInfo: AggregatorInfo
) : AggregatorLaunchUrlPort {

    private val config = PateplayConfig(aggregatorInfo.config)

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
        // TODO: Implement Pateplay launch URL generation
        return Result.failure(NotImplementedError("Pateplay getLaunchUrl not implemented yet"))
    }
}
