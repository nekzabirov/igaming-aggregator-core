package domain.aggregator.adapter.command

import domain.aggregator.adapter.IAggregatorPreset
import core.value.Currency
import kotlinx.datetime.LocalDateTime

data class CreateFreenspinCommand(
    val preset: IAggregatorPreset,

    val referenceId: String,

    val playerId: String,

    val gameSymbol: String,

    val currency: Currency,

    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
)