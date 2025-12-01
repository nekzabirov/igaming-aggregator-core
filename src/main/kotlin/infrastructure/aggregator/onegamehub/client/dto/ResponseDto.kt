package com.nekgamebling.infrastructure.aggregator.onegamehub.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResponseDto<T>(
    val status: Int,

    val response: T? = null
) {
    val success: Boolean = status == 200
}