package com.nekgamebling.infrastructure.wallet.dto

import kotlinx.serialization.Serializable

@Serializable
data class WalletResponse<T>(
    val data: T? = null
)
