package infrastructure.aggregator.onegamehub

import infrastructure.aggregator.onegamehub.handler.OneGameHubHandler
import org.koin.dsl.module

internal val OneGameHubModule = module {
    factory { OneGameHubHandler(get(), get(), get(), get()) }
}