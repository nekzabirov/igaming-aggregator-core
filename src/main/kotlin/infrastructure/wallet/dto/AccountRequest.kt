package com.nekgamebling.infrastructure.wallet.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccountRequest(
    val playerId: String,

    val status: Int = 1
)
