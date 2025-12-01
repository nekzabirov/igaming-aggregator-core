package com.nekgamebling.domain.game.model

import com.nekgamebling.shared.value.ImageMap
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Core game entity representing a game in the system.
 */
@Serializable
data class Game(
    @Serializable(with = com.nekgamebling.shared.serializer.UUIDSerializer::class)
    val id: UUID,
    val identity: String,
    val name: String,
    @Serializable(with = com.nekgamebling.shared.serializer.UUIDSerializer::class)
    val providerId: UUID,
    val images: ImageMap = ImageMap.EMPTY,
    val bonusBetEnable: Boolean = true,
    val bonusWageringEnable: Boolean = true,
    val tags: List<String> = emptyList(),
    val active: Boolean = true
) {
    init {
        require(identity.isNotBlank()) { "Game identity cannot be blank" }
        require(name.isNotBlank()) { "Game name cannot be blank" }
    }

    fun isPlayable(): Boolean = active
    fun hasTag(tag: String): Boolean = tags.contains(tag)
    fun withTag(tag: String): Game = copy(tags = tags + tag)
    fun withoutTag(tag: String): Game = copy(tags = tags - tag)
    fun activate(): Game = copy(active = true)
    fun deactivate(): Game = copy(active = false)
}
