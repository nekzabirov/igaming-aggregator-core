package infrastructure.messaging

import application.event.DomainEvent
import application.port.outbound.EventPublisherAdapter
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.basicPublish
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.rabbitmq
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * RabbitMQ implementation of EventPublisherAdapter.
 */
class RabbitMqEventPublisher(
    private val application: Application,
    private val exchangeName: String = System.getenv("RABBITMQ_EXCHANGE") ?: "game.event"
) : EventPublisherAdapter {
    private val logger = LoggerFactory.getLogger(RabbitMqEventPublisher::class.java)
    private val json = Json { encodeDefaults = true }

    override suspend fun publish(event: DomainEvent) {
        logger.info("Publishing event [${event.routingKey}]: $event")

        application.rabbitmq {
            basicPublish {
                exchange = exchangeName

                routingKey = event.routingKey

                message(event)
            }
        }
    }
}
