package infrastructure.api.rest

import infrastructure.aggregator.onegamehub.handler.oneGameHubWebhookRoute
import infrastructure.aggregator.pateplay.handler.pateplayWebhookRoute
import io.ktor.server.routing.*

/**
 * Aggregator webhook routes for handling callbacks from game aggregators.
 */
fun Route.aggregatorRoute() = route("/{aggregatorIdentity}") {
    oneGameHubWebhookRoute()
    pateplayWebhookRoute()
}
