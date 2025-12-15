package com.nekgamebling.infrastructure.wallet.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val realBalance: Long,
    val bonusBalance: Long,
    val lockedBalance: Long,
    val currency: String,
    val status: Int
)