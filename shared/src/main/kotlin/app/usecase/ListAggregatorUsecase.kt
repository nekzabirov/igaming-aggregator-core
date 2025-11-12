package app.usecase

import domain.mapper.toAggregatorModel
import domain.model.AggregatorInfo
import domain.table.AggregatorInfoTable
import domain.table.base.paging
import domain.value.Aggregator
import domain.value.Page
import domain.value.Pageable
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ListAggregatorUsecase {
    suspend operator fun invoke(pageable: Pageable, filterBuilder: (Filter) -> Unit): Page<AggregatorInfo> =
        newSuspendedTransaction {
            val filter = Filter().apply(filterBuilder)

            AggregatorInfoTable.selectAll()
                .apply {
                    if (filter.query.isNotBlank())
                        andWhere { AggregatorInfoTable.identity like "%${filter.query}%" }

                    filter.type?.let {
                        andWhere { AggregatorInfoTable.aggregator eq it }
                    }

                    filter.active?.let {
                        andWhere { AggregatorInfoTable.active eq it }
                    }
                }
                .paging(pageable)
                .map { it.toAggregatorModel() }
        }

    class Filter {
        var query: String = ""
            private set

        var active: Boolean? = null
            private set

        var type: Aggregator? = null
            private set

        fun withQuery(query: String) = apply { this.query = query }

        fun withActive(active: Boolean) = apply { this.active = active }

        fun withType(type: Aggregator) = apply { this.type = type }
    }
}