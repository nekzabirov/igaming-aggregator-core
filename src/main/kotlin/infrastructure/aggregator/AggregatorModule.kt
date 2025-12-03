package infrastructure.aggregator

import application.port.outbound.AggregatorAdapterRegistry
import infrastructure.aggregator.onegamehub.OneGameHubModule
import infrastructure.aggregator.pateplay.PateplayModule
import infrastructure.aggregator.pragmatic.PragmaticModule
import org.koin.dsl.module

internal val AggregatorModule = module {
    includes(OneGameHubModule)
    includes(PragmaticModule)
    includes(PateplayModule)

    factory<AggregatorAdapterRegistry> {
        AggregatorAdapterRegistryImpl(get(), get(), get())
    }
}