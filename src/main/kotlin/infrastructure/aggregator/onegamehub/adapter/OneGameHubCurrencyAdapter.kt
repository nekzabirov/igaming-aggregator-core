package com.nekgamebling.infrastructure.aggregator.onegamehub.adapter

import application.port.outbound.CurrencyAdapter
import shared.value.Currency
import java.math.BigDecimal
import java.math.BigInteger

class OneGameHubCurrencyAdapter(private val currencyAdapter: CurrencyAdapter) {
    suspend fun convertSystemToProvider(amount: BigInteger, currency: Currency): BigInteger {
        val original = currencyAdapter.convertFromSystem(amount, currency)

        return (original * BigDecimal("100")).toBigInteger()
    }

    suspend fun convertProviderToSystem(amount: BigInteger, currency: Currency): BigInteger {
        val original = amount.toBigDecimal() / BigDecimal("100")

        return currencyAdapter.convertToSystem(original, currency)
    }
}