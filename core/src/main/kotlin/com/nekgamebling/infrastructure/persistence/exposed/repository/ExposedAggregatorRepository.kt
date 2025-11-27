package com.nekgamebling.infrastructure.persistence.exposed.repository

import com.nekgamebling.domain.aggregator.model.AggregatorInfo
import com.nekgamebling.domain.aggregator.repository.AggregatorRepository
import com.nekgamebling.infrastructure.persistence.exposed.mapper.toAggregatorInfo
import com.nekgamebling.infrastructure.persistence.exposed.table.AggregatorInfoTable
import com.nekgamebling.shared.value.Aggregator
import com.nekgamebling.shared.value.Page
import com.nekgamebling.shared.value.Pageable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

/**
 * Exposed implementation of AggregatorRepository.
 */
class ExposedAggregatorRepository : AggregatorRepository {

    override suspend fun findById(id: UUID): AggregatorInfo? = newSuspendedTransaction {
        AggregatorInfoTable.selectAll()
            .where { AggregatorInfoTable.id eq id }
            .singleOrNull()
            ?.toAggregatorInfo()
    }

    override suspend fun findByIdentity(identity: String): AggregatorInfo? = newSuspendedTransaction {
        AggregatorInfoTable.selectAll()
            .where { AggregatorInfoTable.identity eq identity }
            .singleOrNull()
            ?.toAggregatorInfo()
    }

    override suspend fun findByAggregator(aggregator: Aggregator): AggregatorInfo? = newSuspendedTransaction {
        AggregatorInfoTable.selectAll()
            .where { AggregatorInfoTable.aggregator eq aggregator }
            .singleOrNull()
            ?.toAggregatorInfo()
    }

    override suspend fun save(aggregatorInfo: AggregatorInfo): AggregatorInfo = newSuspendedTransaction {
        val id = AggregatorInfoTable.insertAndGetId {
            it[identity] = aggregatorInfo.identity
            it[config] = aggregatorInfo.config
            it[aggregator] = aggregatorInfo.aggregator
            it[active] = aggregatorInfo.active
        }
        aggregatorInfo.copy(id = id.value)
    }

    override suspend fun update(aggregatorInfo: AggregatorInfo): AggregatorInfo = newSuspendedTransaction {
        AggregatorInfoTable.update({ AggregatorInfoTable.id eq aggregatorInfo.id }) {
            it[identity] = aggregatorInfo.identity
            it[config] = aggregatorInfo.config
            it[aggregator] = aggregatorInfo.aggregator
            it[active] = aggregatorInfo.active
        }
        aggregatorInfo
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        AggregatorInfoTable.deleteWhere { AggregatorInfoTable.id eq id } > 0
    }

    override suspend fun existsByIdentity(identity: String): Boolean = newSuspendedTransaction {
        AggregatorInfoTable.selectAll()
            .where { AggregatorInfoTable.identity eq identity }
            .count() > 0
    }

    override suspend fun findAll(pageable: Pageable): Page<AggregatorInfo> = newSuspendedTransaction {
        val totalCount = AggregatorInfoTable.selectAll().count()
        val items = AggregatorInfoTable.selectAll()
            .limit(pageable.sizeReal)
            .offset(pageable.offset)
            .map { it.toAggregatorInfo() }

        Page(
            items = items,
            totalPages = pageable.getTotalPages(totalCount),
            totalItems = totalCount,
            currentPage = pageable.pageReal
        )
    }

    override suspend fun findAllActive(): List<AggregatorInfo> = newSuspendedTransaction {
        AggregatorInfoTable.selectAll()
            .where { AggregatorInfoTable.active eq true }
            .map { it.toAggregatorInfo() }
    }
}
