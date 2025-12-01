package com.nekgamebling.infrastructure.aggregator.onegamehub.adapter

import com.nekgamebling.application.port.outbound.AggregatorFreespinPort
import com.nekgamebling.domain.aggregator.model.AggregatorInfo
import com.nekgamebling.domain.common.error.AggregatorError
import com.nekgamebling.infrastructure.aggregator.onegamehub.OneGameHubConfig
import com.nekgamebling.infrastructure.aggregator.onegamehub.client.OneGameHubHttpClient
import com.nekgamebling.infrastructure.aggregator.onegamehub.client.dto.CancelFreespinDto
import com.nekgamebling.infrastructure.aggregator.onegamehub.client.dto.CreateFreespinDto
import com.nekgamebling.shared.value.Currency
import kotlinx.datetime.LocalDateTime

/**
 * OneGameHub implementation for freespin operations.
 */
class OneGameHubFreespinAdapter(
    private val aggregatorInfo: AggregatorInfo
) : AggregatorFreespinPort {

    private val config = OneGameHubConfig(aggregatorInfo.config)
    private val client = OneGameHubHttpClient(config)

    override suspend fun getPreset(gameSymbol: String): Result<Map<String, Any>> {
        return Result.success(
            mapOf(
                "quantity" to mapOf(
                    "minimal" to 1,
                ),
                "betAmount" to mapOf(
                    "minimal" to 10
                ),
                "lines" to mapOf(
                    "default" to 10,
                    "minimum" to 1,
                    "maximum" to 10
                )
            )
        )
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
        val mainPreset = getPreset(gameSymbol).getOrElse {
            return Result.failure(it)
        }

        var quantity: Int = 0
        var betAmount: Int = 0
        var lines: Int = 0

        for (entry in mainPreset) {
            val key = entry.key
            val value = entry.value as Map<*, *>

            val valNum = if (presetValue.containsKey(key))
                presetValue[key]!!
            else if (value.containsKey("default"))
                value["default"]!! as Int
            else
                return Result.failure(AggregatorError("Missing quantity"))

            if (value.containsKey("minimal") && valNum < value["minimal"] as Int) {
                return Result.failure(AggregatorError("$key too small"))
            } else if (value.containsKey("maximum") && valNum > value["maximum"] as Int) {
                return Result.failure(AggregatorError("$key too large"))
            }

            when (key) {
                "quantity" -> quantity = valNum
                "betAmount" -> betAmount = valNum
                "lines" -> lines = valNum
            }

        }

        val response = client.createFreespin(
            CreateFreespinDto(
                id = referenceId,

                startAt = startAt,
                endAt = endAt,

                number = quantity,

                playerId = playerId,

                currency = currency.value,

                gameId = gameSymbol,

                bet = betAmount,

                lineNumber = lines
            )
        ).getOrElse {
            return Result.failure(it)
        }

        if (!response.success) return Result.failure(
            AggregatorError("Cannot load game from aggregator OneGameHub. status : ${response.status}")
        )

        return Result.success(Unit)
    }

    override suspend fun cancelFreespin(
        referenceId: String,
    ): Result<Unit> {
        return client.cancelFreespin(CancelFreespinDto(referenceId))
            .map {
                Unit
            }
    }
}
