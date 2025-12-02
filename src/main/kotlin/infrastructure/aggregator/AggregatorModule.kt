package infrastructure.aggregator

import application.port.outbound.AggregatorAdapterRegistry
import infrastructure.aggregator.onegamehub.OneGameHubModule
import org.koin.dsl.module

internal val AggregatorModule = module {
    includes(OneGameHubModule)

    factory<AggregatorAdapterRegistry> {
        AggregatorAdapterRegistryImpl(get())
    }
}