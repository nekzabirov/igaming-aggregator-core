package com.nekgamebling.domain.session.model

import com.nekgamebling.shared.value.Currency
import com.nekgamebling.shared.value.Locale
import com.nekgamebling.shared.value.Platform
import java.util.UUID

/**
 * Player gaming session entity.
 */
data class Session(
    val id: UUID = UUID.randomUUID(),
    val gameId: UUID,
    val aggregatorId: UUID,
    val playerId: String,
    val token: String,
    val externalToken: String?,
    val currency: Currency,
    val locale: Locale,
    val platform: Platform
) {
    init {
        require(playerId.isNotBlank()) { "Player ID cannot be blank" }
        require(token.isNotBlank()) { "Session token cannot be blank" }
    }
}

/**
 * Betting round within a session.
 */
data class Round(
    val id: UUID = UUID.randomUUID(),
    val sessionId: UUID,
    val gameId: UUID,
    val extId: String,
    val finished: Boolean = false
) {
    fun finish(): Round = copy(finished = true)
}

/**
 * Individual spin/bet within a round.
 */
data class Spin(
    val id: UUID = UUID.randomUUID(),
    val roundId: UUID,
    val type: com.nekgamebling.shared.value.SpinType,
    val amount: Int,
    val realAmount: Int,
    val bonusAmount: Int,
    val extId: String,
    val referenceId: UUID? = null,
    val freeSpinId: String? = null
)

/**
 * Bet amount breakdown.
 */
data class BetAmount(
    val real: Int,
    val bonus: Int,
    val currency: Currency
) {
    val total: Int get() = real + bonus
}

/**
 * Player balance.
 */
data class Balance(
    val real: Int,
    val bonus: Int,
    val currency: Currency
) {
    val totalAmount: Int get() = real + bonus
}
