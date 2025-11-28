package com.nekgamebling.infrastructure.persistence.exposed.repository

import com.nekgamebling.domain.session.model.Round
import com.nekgamebling.domain.session.repository.RoundRepository
import com.nekgamebling.infrastructure.persistence.exposed.mapper.toRound
import com.nekgamebling.infrastructure.persistence.exposed.table.RoundTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

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
