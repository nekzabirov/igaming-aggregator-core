package com.nekgamebling.infrastructure.persistence.exposed.repository

import com.nekgamebling.domain.session.model.Round
import com.nekgamebling.domain.session.model.Session
import com.nekgamebling.domain.session.model.Spin
import com.nekgamebling.domain.session.repository.RoundRepository
import com.nekgamebling.domain.session.repository.SessionRepository
import com.nekgamebling.domain.session.repository.SpinRepository
import com.nekgamebling.infrastructure.persistence.exposed.mapper.toRound
import com.nekgamebling.infrastructure.persistence.exposed.mapper.toSession
import com.nekgamebling.infrastructure.persistence.exposed.mapper.toSpin
import com.nekgamebling.infrastructure.persistence.exposed.table.RoundTable
import com.nekgamebling.infrastructure.persistence.exposed.table.SessionTable
import com.nekgamebling.infrastructure.persistence.exposed.table.SpinTable
import com.nekgamebling.shared.value.SpinType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

/**
 * Exposed implementation of SessionRepository.
 */
class ExposedSessionRepository : SessionRepository {

    override suspend fun findById(id: UUID): Session? = newSuspendedTransaction {
        SessionTable.selectAll()
            .where { SessionTable.id eq id }
            .singleOrNull()
            ?.toSession()
    }

    override suspend fun findByToken(token: String): Session? = newSuspendedTransaction {
        SessionTable.selectAll()
            .where { SessionTable.token eq token }
            .singleOrNull()
            ?.toSession()
    }

    override suspend fun save(session: Session): Session = newSuspendedTransaction {
        val id = SessionTable.insertAndGetId {
            it[gameId] = session.gameId
            it[aggregatorId] = session.aggregatorId
            it[playerId] = session.playerId
            it[token] = session.token
            it[externalToken] = session.externalToken
            it[currency] = session.currency.value
            it[locale] = session.locale.value
            it[platform] = session.platform
        }
        session.copy(id = id.value)
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        SessionTable.deleteWhere { SessionTable.id eq id } > 0
    }
}

/**
 * Exposed implementation of RoundRepository.
 */
class ExposedRoundRepository : RoundRepository {

    override suspend fun findById(id: UUID): Round? = newSuspendedTransaction {
        RoundTable.selectAll()
            .where { RoundTable.id eq id }
            .singleOrNull()
            ?.toRound()
    }

    override suspend fun findByExtId(sessionId: UUID, extId: String): Round? = newSuspendedTransaction {
        RoundTable.selectAll()
            .where { (RoundTable.sessionId eq sessionId) and (RoundTable.extId eq extId) }
            .singleOrNull()
            ?.toRound()
    }

    override suspend fun findBySessionId(sessionId: UUID): List<Round> = newSuspendedTransaction {
        RoundTable.selectAll()
            .where { RoundTable.sessionId eq sessionId }
            .map { it.toRound() }
    }

    override suspend fun save(round: Round): Round = newSuspendedTransaction {
        val id = RoundTable.insertAndGetId {
            it[sessionId] = round.sessionId
            it[gameId] = round.gameId
            it[extId] = round.extId
            it[finished] = round.finished
        }
        round.copy(id = id.value)
    }

    override suspend fun update(round: Round): Round = newSuspendedTransaction {
        RoundTable.update({ RoundTable.id eq round.id }) {
            it[finished] = round.finished
        }
        round
    }

    override suspend fun finish(id: UUID): Boolean = newSuspendedTransaction {
        RoundTable.update({ RoundTable.id eq id }) {
            it[finished] = true
        } > 0
    }

    override suspend fun findOrCreate(sessionId: UUID, gameId: UUID, extId: String): Round = newSuspendedTransaction {
        val existing = RoundTable.selectAll()
            .where { (RoundTable.sessionId eq sessionId) and (RoundTable.extId eq extId) }
            .singleOrNull()
            ?.toRound()

        if (existing != null) return@newSuspendedTransaction existing

        val id = RoundTable.insertAndGetId {
            it[RoundTable.sessionId] = sessionId
            it[RoundTable.gameId] = gameId
            it[RoundTable.extId] = extId
            it[finished] = false
        }

        Round(
            id = id.value,
            sessionId = sessionId,
            gameId = gameId,
            extId = extId,
            finished = false
        )
    }
}

/**
 * Exposed implementation of SpinRepository.
 */
class ExposedSpinRepository : SpinRepository {

    override suspend fun findById(id: UUID): Spin? = newSuspendedTransaction {
        SpinTable.selectAll()
            .where { SpinTable.id eq id }
            .singleOrNull()
            ?.toSpin()
    }

    override suspend fun findByRoundId(roundId: UUID): List<Spin> = newSuspendedTransaction {
        SpinTable.selectAll()
            .where { SpinTable.roundId eq roundId }
            .map { it.toSpin() }
    }

    override suspend fun findByRoundIdAndType(roundId: UUID, type: SpinType): Spin? = newSuspendedTransaction {
        SpinTable.selectAll()
            .where { (SpinTable.roundId eq roundId) and (SpinTable.type eq type) }
            .singleOrNull()
            ?.toSpin()
    }

    override suspend fun findByExtId(extId: String): Spin? = newSuspendedTransaction {
        SpinTable.selectAll()
            .where { SpinTable.extId eq extId }
            .singleOrNull()
            ?.toSpin()
    }

    override suspend fun save(spin: Spin): Spin = newSuspendedTransaction {
        val id = SpinTable.insertAndGetId {
            it[roundId] = spin.roundId
            it[type] = spin.type
            it[amount] = spin.amount
            it[realAmount] = spin.realAmount
            it[bonusAmount] = spin.bonusAmount
            it[extId] = spin.extId
            it[referenceId] = spin.referenceId
            it[freeSpinId] = spin.freeSpinId
        }
        spin.copy(id = id.value)
    }

    override suspend fun findPlaceSpinByRoundId(roundId: UUID): Spin? = newSuspendedTransaction {
        SpinTable.selectAll()
            .where { (SpinTable.roundId eq roundId) and (SpinTable.type eq SpinType.PLACE) }
            .singleOrNull()
            ?.toSpin()
    }
}
