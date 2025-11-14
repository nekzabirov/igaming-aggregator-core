package infrastructure.aggregator.onegamehub

import core.value.Currency


internal object OneGameHubCurrencyAdapter {

    fun convertToAggregator(currency: Currency, value: Int): Float {
        return value.toFloat()
    }

}