package app.usecase

import app.service.spin.ISpinCommand
import app.service.spin.SpinServiceSpec
import core.error.SessionUnavailingError
import core.value.SessionToken
import domain.session.dao.findByToken
import domain.session.table.SessionTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.component.KoinComponent

class SettleSpinUsecase : KoinComponent {
    val spinService = getKoin().get<SpinServiceSpec>()

    suspend operator fun invoke(
        token: SessionToken,
        extRoundId: String,
        exTransactionId: String,
        freeSpinId: String?,
        amount: Int
    ): Result<Unit> {
        val session = newSuspendedTransaction {
            val session = SessionTable.findByToken(token.value) ?: return@newSuspendedTransaction Result.failure(
                SessionUnavailingError()
            )

            Result.success(session)
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

        spinService.settle(session, extRoundId, command)

        return Result.success(Unit)
    }
}