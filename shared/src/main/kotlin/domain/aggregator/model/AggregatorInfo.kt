package domain.aggregator.model

import domain.aggregator.model.Aggregator
import java.util.UUID

data class AggregatorInfo(
    val id: UUID,

    val identity: String,

    val config: Map<String, String>,

    val aggregator: Aggregator,

    val active: Boolean = true,
)
