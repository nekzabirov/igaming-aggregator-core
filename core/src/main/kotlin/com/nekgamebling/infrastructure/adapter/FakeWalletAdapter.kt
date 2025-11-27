package com.nekgamebling.infrastructure.adapter

import com.nekgamebling.application.port.outbound.WalletPort
import com.nekgamebling.domain.session.model.Balance
import com.nekgamebling.shared.value.Currency
import java.util.concurrent.ConcurrentHashMap

/**
 * Fake wallet adapter for development/testing.
 * Replace with real implementation in production.
 */
class FakeWalletAdapter : WalletPort {
    private val balances = ConcurrentHashMap<String, MutableMap<String, Balance>>()

    init {
        // Initialize with some test balances
        setBalance("test-player-1", Currency("USD"), Balance(10000, 500, Currency("USD")))
        setBalance("test-player-1", Currency("EUR"), Balance(8000, 300, Currency("EUR")))
    }

    fun setBalance(playerId: String, currency: Currency, balance: Balance) {
        balances.getOrPut(playerId) { mutableMapOf() }[currency.value] = balance
    }

    override suspend fun findBalance(playerId: String): Result<Balance> {
        // Return a default balance if not found
        val playerBalances = balances[playerId]
        val balance = playerBalances?.values?.firstOrNull()
            ?: Balance(10000, 500, Currency("USD"))

        return Result.success(balance)
    }

    override suspend fun withdraw(
        playerId: String,
        transactionId: String,
        currency: Currency,
        realAmount: Int,
        bonusAmount: Int
    ): Result<Unit> {
        val playerBalances = balances.getOrPut(playerId) { mutableMapOf() }
        val current = playerBalances[currency.value]
            ?: Balance(10000, 500, currency)

        val newBalance = Balance(
            real = current.real - realAmount,
            bonus = current.bonus - bonusAmount,
            currency = currency
        )

        if (newBalance.real < 0 || newBalance.bonus < 0) {
            return Result.failure(IllegalStateException("Insufficient balance"))
        }

        playerBalances[currency.value] = newBalance
        return Result.success(Unit)
    }

    override suspend fun deposit(
        playerId: String,
        transactionId: String,
        currency: Currency,
        realAmount: Int,
        bonusAmount: Int
    ): Result<Unit> {
        val playerBalances = balances.getOrPut(playerId) { mutableMapOf() }
        val current = playerBalances[currency.value]
            ?: Balance(0, 0, currency)

        playerBalances[currency.value] = Balance(
            real = current.real + realAmount,
            bonus = current.bonus + bonusAmount,
            currency = currency
        )

        return Result.success(Unit)
    }

    override suspend fun rollback(playerId: String, transactionId: String): Result<Unit> {
        // In a real implementation, this would reverse the transaction
        return Result.success(Unit)
    }
}
