package com.nekgamebling.application.port.outbound

import com.nekgamebling.domain.session.model.Balance
import com.nekgamebling.shared.value.Currency

/**
 * Port interface for wallet operations.
 * Implementations connect to external wallet service.
 */
interface WalletPort {
    /**
     * Get player's current balance.
     */
    suspend fun findBalance(playerId: String): Result<Balance>

    /**
     * Withdraw funds from player's wallet.
     */
    suspend fun withdraw(
        playerId: String,
        transactionId: String,
        currency: Currency,
        realAmount: Int,
        bonusAmount: Int
    ): Result<Unit>

    /**
     * Deposit funds to player's wallet.
     */
    suspend fun deposit(
        playerId: String,
        transactionId: String,
        currency: Currency,
        realAmount: Int,
        bonusAmount: Int
    ): Result<Unit>

    /**
     * Rollback a previous transaction.
     */
    suspend fun rollback(playerId: String, transactionId: String): Result<Unit>
}
