package com.nekgamebling.infrastructure.persistence.exposed.repository

import com.nekgamebling.domain.session.model.Spin
import com.nekgamebling.domain.session.repository.SpinRepository
import com.nekgamebling.infrastructure.persistence.exposed.mapper.toSpin
import com.nekgamebling.infrastructure.persistence.exposed.table.SpinTable
import com.nekgamebling.shared.value.SpinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

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
