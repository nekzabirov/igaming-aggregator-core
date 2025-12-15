package com.nekgamebling.infrastructure.wallet

import application.port.outbound.WalletAdapter
import com.nekgamebling.infrastructure.wallet.dto.AccountDto
import com.nekgamebling.infrastructure.wallet.dto.AccountRequest
import com.nekgamebling.infrastructure.wallet.dto.BalanceType
import com.nekgamebling.infrastructure.wallet.dto.BetTransactionRequest
import com.nekgamebling.infrastructure.wallet.dto.SettleTransactionRequest
import com.nekgamebling.infrastructure.wallet.dto.WalletResponse
import domain.session.model.Balance
import infrastructure.persistence.exposed.table.SpinTable.bonusAmount
import infrastructure.persistence.exposed.table.SpinTable.realAmount
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.Json
import shared.value.Currency
import java.math.BigInteger

/**
 * Fake wallet adapter for development/testing.
 * Replace with real implementation in production.
 */
class TurboWalletAdapter : WalletAdapter {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 30000
        }

        install(Logging) {
            logger = Logger.Companion.DEFAULT
            level = LogLevel.ALL
        }
    }

    private val urlAddress by lazy {
        System.getenv()["TURBO_WALLET_URL"] ?: "http://localhost:8080"
    }

    override suspend fun findBalance(playerId: String): Result<Balance> = runCatching {
        val walletResponse: WalletResponse<List<AccountDto>> = client.get("$urlAddress/accounts/find") {
            parameter("playerId", playerId)
        }.body()

        if (walletResponse.data == null) throw Exception("Failed to fetch balance from TurboWallet")

        val account = walletResponse.data.firstOrNull { it.status == 1 } ?: throw Exception("Failed to fetch balance from TurboWallet")

        return@runCatching Balance(
            real = account.realBalance.toBigInteger() + account.lockedBalance.toBigInteger(),
            bonus = account.bonusBalance.toBigInteger(),
            currency = Currency(account.currency)
        )
    }

    override suspend fun withdraw(
        playerId: String,
        transactionId: String,
        currency: Currency,
        realAmount: BigInteger,
        bonusAmount: BigInteger
    ): Result<Unit> = runCatching {
        val request = BetTransactionRequest(
            playerId = playerId,
            amount = (realAmount + bonusAmount).toLong(),
            currency = currency.value,
            externalId = transactionId,
            balanceTypeOrder = listOf(BalanceType.REAL, BalanceType.LOCKED, BalanceType.BONUS)
        )

        client.post("$urlAddress/bets/placebet") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyAsText()

        return Result.success(Unit)
    }

    override suspend fun deposit(
        playerId: String,
        transactionId: String,
        currency: Currency,
        realAmount: BigInteger,
        bonusAmount: BigInteger
    ): Result<Unit> = runCatching {
        if (realAmount + bonusAmount <= BigInteger.ZERO) return Result.success(Unit)

        /**
         *     "externalId": "0119cd59-812a-4775-af6e-91aebed34d75",
         *     "referencedExternalId": "0119cd59-812a-4775-af6e-91aebed34d75",
         */

        val request = SettleTransactionRequest(
            playerId = playerId,
            amount = (realAmount + bonusAmount).toLong(),
            currency = currency.value,
            externalId = transactionId,
            referencedExternalId = transactionId,
            balanceType = if (bonusAmount > BigInteger.ZERO) BalanceType.BONUS else BalanceType.REAL
        )

        println("Send deposit request: $request")

        val body = client.post("$urlAddress/bets/settle") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyAsText()

        println("Deposit response: $body")

        return Result.success(Unit)
    }

    override suspend fun rollback(playerId: String, transactionId: String): Result<Unit> {
        return Result.success(Unit)
    }

}