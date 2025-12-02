package infrastructure.aggregator.pragmatic.adapter

import application.port.outbound.AggregatorFreespinPort
import domain.aggregator.model.AggregatorInfo
import domain.common.error.AggregatorError
import infrastructure.aggregator.pragmatic.client.PragmaticHttpClient
import infrastructure.aggregator.pragmatic.client.dto.CreateFreespinDto
import infrastructure.aggregator.pragmatic.client.dto.FreespinBetValueDto
import infrastructure.aggregator.pragmatic.client.dto.FreespinGameDto
import infrastructure.aggregator.pragmatic.model.PragmaticConfig
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import shared.value.Currency

/**
 * Pragmatic implementation for freespin operations.
 */
class PragmaticFreespinAdapter(
    aggregatorInfo: AggregatorInfo,
    private val providerCurrencyAdapter: PragmaticCurrencyAdapter
) : AggregatorFreespinPort {

    private val client = PragmaticHttpClient(PragmaticConfig(aggregatorInfo.config))

    override suspend fun getPreset(gameSymbol: String): Result<Map<String, Any>> {
        return Result.success(
            mapOf(
                "totalBet" to mapOf(
                    "minimal" to 100,
                ),
                "rounds" to mapOf(
                    "minimal" to 10
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

        var rounds = 0
        var totalBet = 0

        for (entry in mainPreset) {
            val key = entry.key
            val value = entry.value as Map<*, *>

            val valNum = if (presetValue.containsKey(key))
                presetValue[key]!!
            else if (value.containsKey("default"))
                value["default"]!! as Int
            else
                return Result.failure(AggregatorError("Missing required preset value: $key"))

            if (value.containsKey("minimal") && valNum < value["minimal"] as Int) {
                return Result.failure(AggregatorError("$key value too small: $valNum < ${value["minimal"]}"))
            } else if (value.containsKey("maximum") && valNum > value["maximum"] as Int) {
                return Result.failure(AggregatorError("$key value too large: $valNum > ${value["maximum"]}"))
            }

            when (key) {
                "rounds" -> rounds = valNum
                "totalBet" -> totalBet = valNum
            }
        }

        // Convert totalBet from system format to provider format (real currency units)
        val totalBetDecimal = providerCurrencyAdapter.convertSystemToProvider(
            totalBet.toBigInteger(),
            currency
        ).toDouble()

        // Convert LocalDateTime to Unix timestamp (seconds)
        val startTimestamp = startAt.toInstant(TimeZone.UTC).epochSeconds
        val expirationTimestamp = endAt.toInstant(TimeZone.UTC).epochSeconds

        val payload = CreateFreespinDto(
            bonusCode = referenceId,
            playerId = playerId,
            currency = currency.value,
            rounds = rounds,
            startTimestamp = startTimestamp,
            expirationTimestamp = expirationTimestamp,
            gameList = listOf(
                FreespinGameDto(
                    gameId = gameSymbol,
                    betValues = listOf(
                        FreespinBetValueDto(
                            currency = currency.value,
                            totalBet = totalBetDecimal
                        )
                    )
                )
            )
        )

        return client.createFreespin(payload)
    }

    override suspend fun cancelFreespin(
        referenceId: String,
    ): Result<Unit> {
        return client.cancelFreespin(referenceId)
    }
}
