package com.nekgamebling.application.event

import com.nekgamebling.shared.value.Currency
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Base interface for all domain events.
 */
@Serializable
sealed interface DomainEvent {
    /**
     * Routing key for message queue.
     */
    val routingKey: String

    /**
     * Timestamp when the event occurred.
     */
    val timestamp: Long
        get() = System.currentTimeMillis()
}

// ============================================
// Spin Events
// ============================================

/**
 * Base interface for spin-related events.
 */
@Serializable
sealed interface SpinEvent : DomainEvent {
    val gameId: String
    val gameIdentity: String
    val amount: Int
    val currency: Currency
    val playerId: String
    val freeSpinId: String?
}

/**
 * Event emitted when a spin is placed (bet made).
 */
@Serializable
data class SpinPlacedEvent(
    override val gameId: String,
    override val gameIdentity: String,
    override val amount: Int,
    override val currency: Currency,
    override val playerId: String,
    override val freeSpinId: String? = null
) : SpinEvent {
    override val routingKey: String = "spin.placed"
}

/**
 * Event emitted when a spin is settled (win/loss determined).
 */
@Serializable
data class SpinSettledEvent(
    override val gameId: String,
    override val gameIdentity: String,
    override val amount: Int,
    override val currency: Currency,
    override val playerId: String,
    override val freeSpinId: String? = null,
    val winAmount: Int = 0
) : SpinEvent {
    override val routingKey: String = "spin.settled"
}

/**
 * Event emitted when a spin is rolled back.
 */
@Serializable
data class SpinRolledBackEvent(
    override val gameId: String,
    override val gameIdentity: String,
    override val amount: Int,
    override val currency: Currency,
    override val playerId: String,
    override val freeSpinId: String? = null,
    val reason: String? = null
) : SpinEvent {
    override val routingKey: String = "spin.rolled_back"
}

// ============================================
// Session Events
// ============================================

/**
 * Event emitted when a new session is opened.
 */
@Serializable
data class SessionOpenedEvent(
    val sessionId: String,
    val gameId: String,
    val gameIdentity: String,
    val playerId: String,
    val currency: Currency,
    val platform: String
) : DomainEvent {
    override val routingKey: String = "session.opened"
}

/**
 * Event emitted when a session is closed.
 */
@Serializable
data class SessionClosedEvent(
    val sessionId: String,
    val playerId: String,
    val reason: String? = null
) : DomainEvent {
    override val routingKey: String = "session.closed"
}

// ============================================
// Game Events
// ============================================

/**
 * Event emitted when a player wins a game.
 */
@Serializable
data class GameWonEvent(
    val gameId: String,
    val gameIdentity: String,
    val playerId: String,
    val amount: Int,
    val currency: Currency
) : DomainEvent {
    override val routingKey: String = "game.won"
}

/**
 * Event emitted when a player adds a game to favorites.
 */
@Serializable
data class GameFavouriteAddedEvent(
    val gameId: String,
    val gameIdentity: String,
    val playerId: String
) : DomainEvent {
    override val routingKey: String = "game.favourite.added"
}

/**
 * Event emitted when a player removes a game from favorites.
 */
@Serializable
data class GameFavouriteRemovedEvent(
    val gameId: String,
    val gameIdentity: String,
    val playerId: String
) : DomainEvent {
    override val routingKey: String = "game.favourite.removed"
}
