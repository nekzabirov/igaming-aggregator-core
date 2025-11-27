package com.nekgamebling.infrastructure.aggregator.onegamehub

import com.nekgamebling.application.port.outbound.AggregatorLaunchUrlPort
import com.nekgamebling.domain.aggregator.model.AggregatorInfo
import com.nekgamebling.shared.value.Locale
import com.nekgamebling.shared.value.Platform

/**
 * OneGameHub implementation for getting game launch URLs.
 */
class OneGameHubLaunchUrlAdapter(
    private val aggregatorInfo: AggregatorInfo
) : AggregatorLaunchUrlPort {

    override suspend fun getLaunchUrl(
        aggregator: AggregatorInfo,
        gameSymbol: String,
        sessionToken: String,
        locale: Locale,
        platform: Platform,
        demo: Boolean
    ): Result<String> {
        val baseUrl = aggregator.config["base_url"]
            ?: return Result.failure(IllegalStateException("Missing base_url in aggregator config"))

        val operatorId = aggregator.config["operator_id"]
            ?: return Result.failure(IllegalStateException("Missing operator_id in aggregator config"))

        val mode = if (demo) "demo" else "real"
        val platformStr = platform.name.lowercase()

        val url = buildString {
            append(baseUrl)
            append("/launch")
            append("?operator=").append(operatorId)
            append("&game=").append(gameSymbol)
            append("&token=").append(sessionToken)
            append("&locale=").append(locale.value)
            append("&platform=").append(platformStr)
            append("&mode=").append(mode)
        }

        return Result.success(url)
    }
}
