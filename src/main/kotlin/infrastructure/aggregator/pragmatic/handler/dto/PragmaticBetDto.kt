package infrastructure.aggregator.pragmatic.handler.dto

import java.math.BigDecimal

data class PragmaticBetDto(
    val gameSymbol: String,

    val roundId: String,

    val transactionId: String,

    val freeSpinId: String?,

    val amount: BigDecimal
)
