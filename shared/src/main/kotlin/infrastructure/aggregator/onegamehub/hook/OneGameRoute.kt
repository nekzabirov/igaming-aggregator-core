package infrastructure.aggregator.onegamehub.hook

import app.service.SpinService
import core.model.Balance
import core.value.Currency
import core.value.SessionToken
import infrastructure.aggregator.onegamehub.adapter.OneGameHubCurrencyAdapter
import infrastructure.aggregator.onegamehub.hook.error.OneGameHubError
import infrastructure.aggregator.onegamehub.hook.error.OneGameHubInvalidateRequest
import infrastructure.aggregator.onegamehub.hook.error.OneGameHubTokenExpired
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val Parameters.amount get() = this["amount"]!!.toInt()
private val Parameters.gameSymbol get() = this["game_id"]!!
private val Parameters.transactionId get() = this["transaction_id"]!!
private val Parameters.roundId get() = this["round_id"]!!
private val Parameters.freespinId get() = this["freerounds_id"]
private val Parameters.isRoundEnd get() = this["ext_round_finished"] == "1"

internal fun Route.oneGameHubRoute() = post("/onegamehub") {
    val action = call.queryParameters["action"]
    val sessionToken = call.queryParameters["extra"]

    if (action == null || sessionToken == null) {
        call.respondFail(OneGameHubInvalidateRequest())
        return@post
    }

    when (action) {
        "balance" -> {
            balance(SessionToken(sessionToken))
        }

        "bet" -> {
            bet(SessionToken(sessionToken))
        }

        "win" -> {
            win(SessionToken(sessionToken))
        }

        else -> {
            call.respondFail(OneGameHubInvalidateRequest())
        }
    }
}

private suspend fun RoutingContext.balance(token: SessionToken) {
    val balance = SpinService.findBalance(token).getOrElse {
        call.respondFail(OneGameHubTokenExpired())
        return
    }

    call.respondSuccess(balance)
}

private suspend fun RoutingContext.bet(token: SessionToken) {
    val balance = SpinService.place(
        token = token,
        gameSymbol = call.queryParameters.gameSymbol,
        extRoundId = call.queryParameters.roundId,
        transactionId = call.queryParameters.transactionId,
        amount = call.queryParameters.amount
    ).getOrElse {
        call.respond(OneGameHubError.transform(it))
        return
    }

    call.respondSuccess(balance)
}

private suspend fun RoutingContext.win(token: SessionToken) {
    val balance = SpinService.settle(
        token = token,
        extRoundId = call.queryParameters.roundId,
        transactionId = call.queryParameters.transactionId,
        amount = call.queryParameters.amount
    ).getOrElse {
        call.respond(OneGameHubError.transform(it))
        return
    }

    call.respondSuccess(balance)
}

private suspend fun ApplicationCall.respondSuccess(balance: Balance) {
    val totalAmount = OneGameHubCurrencyAdapter.convertToAggregator(balance.currency, balance.totalAmount)

    respondSuccess(totalAmount, balance.currency)
}

private suspend fun ApplicationCall.respondFail(error: OneGameHubError) {
    respond(HttpStatusCode.BadRequest, error.body)
}

private suspend fun ApplicationCall.respondSuccess(balance: Int, currency: Currency) =
    respond(
        HttpStatusCode.OK, mapOf(
            "status" to 200,
            "balance" to balance,
            "currency" to currency.value
        )
    )