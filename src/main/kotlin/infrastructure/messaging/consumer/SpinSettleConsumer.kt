package com.nekgamebling.infrastructure.messaging.consumer

import application.event.SpinSettledEvent
import application.port.inbound.SpinSettleEventHandler
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.*
import io.ktor.server.application.*
import org.koin.ktor.ext.getKoin

private const val QUEUE_NAME = "spin.events"
private const val ROUTING_KEY = "spin.settled"

fun Application.consumeSpinSettle(exchange: String) = rabbitmq {
    val handlers = getKoin().get<List<SpinSettleEventHandler>>()

    queueBind {
        queue = QUEUE_NAME
        this.exchange = exchange
        routingKey = ROUTING_KEY

        exchangeDeclare {
            this.exchange = exchange
            type = "topic"
            durable = true
        }

        queueDeclare {
            queue = QUEUE_NAME
            durable = true
        }
    }

    basicConsume {
        queue = QUEUE_NAME
        autoAck = true

        deliverCallback<SpinSettledEvent> { msg ->
            val body = msg.body
            log.info("Spin event received: $body")

            handlers.forEach { it.handle(body) }
        }
    }
}