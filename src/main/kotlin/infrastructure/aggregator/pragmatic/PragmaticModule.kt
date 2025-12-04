package infrastructure.aggregator.pragmatic

import com.nekgamebling.infrastructure.aggregator.pragmatic.handler.PragmaticHandler
import infrastructure.aggregator.pragmatic.adapter.PragmaticCurrencyAdapter
import org.koin.dsl.module

internal val PragmaticModule = module {
    single { PragmaticCurrencyAdapter(get()) }

    factory { PragmaticHandler(get(), get(), get(), get(), get(), get(), get(), get()) }

    factory { PragmaticAdapterFactory(get()) }
}
