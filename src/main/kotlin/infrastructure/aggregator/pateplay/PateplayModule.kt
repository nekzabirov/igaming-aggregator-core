package infrastructure.aggregator.pateplay

import infrastructure.aggregator.pateplay.adapter.PateplayCurrencyAdapter
import infrastructure.aggregator.pateplay.handler.PateplayHandler
import org.koin.dsl.module

internal val PateplayModule = module {
    single { PateplayCurrencyAdapter(get()) }

    factory { PateplayHandler(get(), get(), get(), get(), get()) }

    factory { PateplayAdapterFactory(get()) }
}
