package infrastructure.aggregator.pateplay.adapter

import application.port.outbound.AggregatorFreespinPort
import domain.aggregator.model.AggregatorInfo
import domain.common.error.AggregatorError
import infrastructure.aggregator.pateplay.client.PateplayHttpClient
import infrastructure.aggregator.pateplay.client.dto.CancelFreespinRequestDto
import infrastructure.aggregator.pateplay.client.dto.CreateFreespinRequestDto
import infrastructure.aggregator.pateplay.model.PateplayConfig
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import shared.value.Currency

/**
 * Pateplay implementation for freespin operations.
 */
class PateplayFreespinAdapter(
    aggregatorInfo: AggregatorInfo,
    private val providerCurrencyAdapter: PateplayCurrencyAdapter
) : AggregatorFreespinPort {

    private val config = PateplayConfig(aggregatorInfo.config)
    private val client = PateplayHttpClient(config)

    override suspend fun getPreset(gameSymbol: String): Result<Map<String, Any>> {
        return Result.success(
            mapOf(
                "stake" to mapOf(
                    "minimal" to 100
                ),
                "rounds" to mapOf(
                    "minimal" to 1
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
        var stake = 0

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
                "stake" -> stake = valNum
            }
        }

        // Convert stake from system format to provider format (real currency units as string)
        val stakeDecimal = providerCurrencyAdapter.convertSystemToProvider(
            stake.toBigInteger(),
            currency
        )

        // Calculate TTL in seconds
        val startTimestamp = startAt.toInstant(TimeZone.UTC).epochSeconds
        val endTimestamp = endAt.toInstant(TimeZone.UTC).epochSeconds
        val ttlSeconds = endTimestamp - startTimestamp

        // Format expiration date as ISO-8601
        val expiresAt = "${endAt}Z"

        val payload = CreateFreespinRequestDto(
            referenceId = referenceId,
            playerId = playerId,
            currency = currency.value,
            ttlSeconds = ttlSeconds,
            gameSymbol = gameSymbol,
            stake = stakeDecimal.toPlainString(),
            rounds = rounds,
            expiresAt = expiresAt
        )

        return client.createFreespin(payload)
    }

    override suspend fun cancelFreespin(
        referenceId: String,
    ): Result<Unit> {
        // PatePlay cancel API expects numeric bonus ID
        val bonusId = referenceId.toLongOrNull()
            ?: return Result.failure(AggregatorError("Invalid bonus reference ID: $referenceId"))

        val payload = CancelFreespinRequestDto(
            bonusId = bonusId,
            reason = "Bonus cancelled by operator",
            force = false
        )

        return client.cancelFreespin(payload)
    }
}
