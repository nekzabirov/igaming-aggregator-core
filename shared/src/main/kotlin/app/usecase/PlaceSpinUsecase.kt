package app.usecase

import app.service.spin.ISpinCommand
import app.service.spin.SpinServiceSpec
import core.error.GameUnavailableError
import core.error.SessionUnavailingError
import core.value.SessionToken
import domain.game.dao.findBySymbol
import domain.game.table.GameTable
import domain.session.dao.findByToken
import domain.session.table.SessionTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.component.KoinComponent

class PlaceSpinUsecase : KoinComponent {
    val spinService = getKoin().get<SpinServiceSpec>()

    suspend operator fun invoke(
        token: SessionToken,
        gameSymbol: String,
        extRoundId: String,
        exTransactionId: String,
        freeSpinId: String?,
        amount: Int
    ): Result<Unit> {
        val (session, game) = newSuspendedTransaction {
            val session = SessionTable.findByToken(token.value) ?: return@newSuspendedTransaction Result.failure(
                SessionUnavailingError()
            )

            val game = GameTable.findBySymbol(gameSymbol) ?: return@newSuspendedTransaction Result.failure(
                GameUnavailableError()
            )

            Result.success(session to game)
        }.getOrElse { return Result.failure(it) }

        val command = ISpinCommand.Builder()
            .withExtRoundId(extRoundId)
            .withTransactionId(exTransactionId)
            .withAmount(amount)
            .let {
                if (freeSpinId != null) {
                    it.withFreeSpinId(freeSpinId)
                }

                it
            }
            .build()

        spinService.place(session, game, command)

        return Result.success(Unit)
    }
}