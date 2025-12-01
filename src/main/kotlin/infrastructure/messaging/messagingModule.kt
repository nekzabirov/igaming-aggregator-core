package com.nekgamebling.infrastructure.messaging

import application.port.outbound.EventPublisherAdapter
import com.nekgamebling.infrastructure.messaging.consumer.consumeSpinSettle
import infrastructure.messaging.RabbitMqEventPublisher
import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module

fun messagingModule(application: Application) = module {
    val url = System.getenv("RABBITMQ_URL")
    val exchange = System.getenv("RABBITMQ_EXCHANGE")

    application.install(RabbitMQ) {
        uri = url
    }

    application.consumeSpinSettle(exchange)

    single<EventPublisherAdapter> { RabbitMqEventPublisher(application, exchange) }
}