package com.nekgamebling.domain.provider.model

import com.nekgamebling.shared.serializer.UUIDSerializer
import com.nekgamebling.shared.value.ImageMap
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Game provider entity.
 */
@Serializable
data class Provider(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val identity: String,
    val name: String,
    val images: ImageMap = ImageMap.EMPTY,
    val order: Int = 100,
    @Serializable(with = UUIDSerializer::class)
    val aggregatorId: UUID? = null,
    val active: Boolean = true
) {
    init {
        require(identity.isNotBlank()) { "Provider identity cannot be blank" }
        require(name.isNotBlank()) { "Provider name cannot be blank" }
    }

    fun isLinkedToAggregator(): Boolean = aggregatorId != null
    fun activate(): Provider = copy(active = true)
    fun deactivate(): Provider = copy(active = false)
}
