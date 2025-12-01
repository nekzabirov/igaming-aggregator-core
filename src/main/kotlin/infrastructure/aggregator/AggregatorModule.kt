package infrastructure.aggregator

import infrastructure.aggregator.onegamehub.OneGameHubModule
import org.koin.dsl.module

internal val AggregatorModule = module {
    includes(OneGameHubModule)
}