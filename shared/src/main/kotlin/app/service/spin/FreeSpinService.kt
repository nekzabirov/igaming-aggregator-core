package app.service.spin

import core.model.SpinType
import domain.game.model.Game
import domain.session.model.Session
import domain.session.table.SpinTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.component.KoinComponent

class FreeSpinService : ISpinService(), KoinComponent {
    override suspend fun place(session: Session, game: Game, command: ISpinCommand): Result<Unit> {
        return newSuspendedTransaction {
            val roundId = createRoundId(session, game, command)
                .getOrElse { return@newSuspendedTransaction Result.failure(it) }

            SpinTable.insert {
                it[SpinTable.type] = SpinType.PLACE
                it[SpinTable.amount] = command.amount
                it[SpinTable.realAmount] = 0
                it[SpinTable.bonusAmount] = 0
                it[SpinTable.extId] = command.extRoundId
                it[SpinTable.roundId] = roundId
            }

            Result.success(Unit)
        }
    }

    override suspend fun settle(session: Session, extRoundId: String, command: ISpinCommand): Result<Unit> {
        return newSuspendedTransaction {
            val roundIdN = findRoundId(session, extRoundId)
                .getOrElse { return@newSuspendedTransaction Result.failure(it) }

            SpinTable.insert {
                it[SpinTable.type] = SpinType.SETTLE
                it[SpinTable.amount] = command.amount
                it[SpinTable.realAmount] = 0
                it[SpinTable.bonusAmount] = 0
                it[SpinTable.extId] = command.transactionId
                it[SpinTable.roundId] = roundIdN
            }

            Result.success(Unit)
        }
    }
}