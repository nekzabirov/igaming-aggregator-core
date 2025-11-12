package app.usecase

import domain.value.Aggregator
import domain.model.AggregatorInfo
import domain.table.AggregatorInfoTable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class AddAggregatorUsecase {
    suspend operator fun invoke(
        identity: String,
        type: domain.value.Aggregator,
        config: Map<String, String>
    ): Result<domain.model.AggregatorInfo> = newSuspendedTransaction {
        val id = AggregatorInfoTable.insertAndGetId {
            it[AggregatorInfoTable.identity] = identity
            it[aggregator] = _root_ide_package_.domain.value.Aggregator.ONEGAMEHUB
            it[AggregatorInfoTable.config] = config
        }.value

        Result.success(
            _root_ide_package_.domain.model.AggregatorInfo(
                id = id,
                identity = identity,
                config = config,
                aggregator = type,
                active = true,
            )
        )
    }
}