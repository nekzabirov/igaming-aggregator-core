package app.service

import app.adapter.PlayerAdapter
import app.adapter.WalletAdapter
import core.error.BetOutLimitError
import core.error.InsufficientBalanceError
import core.error.RoundFinishedError
import core.model.Balance
import core.model.SpinType
import domain.game.table.GameTable
import domain.session.model.Session
import domain.session.table.RoundTable
import domain.session.table.SpinTable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.upsertReturning
import org.koin.core.component.KoinComponent

object SpinService : KoinComponent {
    val walletAdapter = getKoin().get<WalletAdapter>()
    val playerAdapter = getKoin().get<PlayerAdapter>()

    suspend fun findBalance(session: Session): Result<Balance> = newSuspendedTransaction {
        val balance = walletAdapter.findBalance(session.playerId).getOrElse {
            return@newSuspendedTransaction Result.failure(it)
        }

        val isBonusBetAllow = GameTable.select(GameTable.id, GameTable.bonusBetEnable)
            .where { GameTable.id eq session.gameId }
            .singleOrNull()?.get(GameTable.bonusBetEnable) ?: false

        Result.success(balance.copy(bonus = if (isBonusBetAllow) balance.bonus else 0))
    }

    suspend fun placeBet(session: Session, extRoundId: String, transactionId: String, amount: Int): Result<Balance> {
        val isRoundFinished = newSuspendedTransaction {
            RoundTable.select(RoundTable.extId, RoundTable.sessionId, RoundTable.endAt)
                .where { RoundTable.extId eq extRoundId and (RoundTable.sessionId eq session.id) }
                .count() > 0
        }

        if (isRoundFinished) {
            return Result.failure(RoundFinishedError())
        }

        val (balanceAsync, playerBetLimitAsync) = coroutineScope {
            val balanceAsync = async { findBalance(session) }
            val playerBetLimitAsync = async { playerAdapter.findCurrentBetLimit(session.playerId) }

            balanceAsync.await() to playerBetLimitAsync.await()
        }

        val balance = balanceAsync.getOrElse { return Result.failure(it) }
        val playerBetLimit = playerBetLimitAsync.getOrElse { return Result.failure(it) }

        if (amount > balance.totalAmount) {
            return Result.failure(InsufficientBalanceError())
        }

        if (playerBetLimit != null && amount > playerBetLimit) {
            return Result.failure(BetOutLimitError())
        }

        val realAmount = if (balance.real < amount) balance.real else amount
        val bonusAmount = if (realAmount == amount) 0 else amount - realAmount

        walletAdapter.withdraw(session.playerId, session.id.toString(), balance.currency, realAmount, bonusAmount)
            .getOrElse { return Result.failure(it) }

        newSuspendedTransaction {
            val roundId = RoundTable.upsertReturning(
                keys = arrayOf(RoundTable.extId, RoundTable.sessionId),
                onUpdateExclude = listOf(RoundTable.endAt, RoundTable.createdAt),
                returning = listOf(RoundTable.id)
            ) {
                it[RoundTable.sessionId] = session.id
                it[RoundTable.gameId] = session.gameId
                it[RoundTable.extId] = extRoundId
            }.single()[RoundTable.id].value

            SpinTable.insert {
                it[SpinTable.roundId] = roundId
                it[SpinTable.type] = SpinType.PLACE
                it[SpinTable.realAmount] = realAmount
                it[SpinTable.bonusAmount] = bonusAmount
                it[SpinTable.extId] = transactionId
            }
        }

        return Result.success(
            balance.copy(
                real = balance.real - realAmount,
                bonus = balance.bonus - bonusAmount
            )
        )
    }

    suspend fun settleBet(session: Session, extRoundId: String, transactionId: String, amount: Int): Result<Balance> {
        val balance = findBalance(session).getOrElse { return Result.failure(it) }

        val (roundId, spinPlaceId, isBonusUsed) = newSuspendedTransaction {
            val roundRow = RoundTable
                .select(RoundTable.id, RoundTable.endAt)
                .where { RoundTable.extId eq extRoundId and (RoundTable.sessionId eq session.id) }
                .singleOrNull() ?: return@newSuspendedTransaction null

            val roundId = roundRow[RoundTable.id].value
            val roundFinished = roundRow[RoundTable.endAt] != null
            if (roundFinished) {
                return@newSuspendedTransaction null
            }

            val spinRow = SpinTable
                .select(SpinTable.id, SpinTable.type, SpinTable.roundId, SpinTable.bonusAmount)
                .where { SpinTable.roundId eq roundId and (SpinTable.type eq SpinType.PLACE) }
                .singleOrNull() ?: return@newSuspendedTransaction null

            val spinPlaceId = spinRow[SpinTable.id].value
            val isBonusUsed = spinRow[SpinTable.bonusAmount] > 0

            Triple(roundId, spinPlaceId, isBonusUsed)
        } ?: return Result.failure(RoundFinishedError())

        val realAmount = if (isBonusUsed) 0 else amount
        val bonusAmount = if (isBonusUsed) amount else 0

        walletAdapter.deposit(session.playerId, session.id.toString(), balance.currency, realAmount, bonusAmount)
            .getOrElse { return Result.failure(it) }

        newSuspendedTransaction {
            SpinTable.upsert {
                it[SpinTable.id] = spinPlaceId
                it[SpinTable.type] = SpinType.SETTLE
                it[SpinTable.realAmount] = realAmount
                it[SpinTable.bonusAmount] = bonusAmount
                it[SpinTable.extId] = transactionId
                it[SpinTable.roundId] = roundId
                it[SpinTable.referenceId] = spinPlaceId
            }
        }

        return Result.success(
            balance.copy(
                real = balance.real + realAmount,
                bonus = balance.bonus + bonusAmount
            )
        )
    }
}