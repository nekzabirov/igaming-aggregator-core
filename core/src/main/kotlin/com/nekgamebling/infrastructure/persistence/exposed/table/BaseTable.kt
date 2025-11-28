package com.nekgamebling.infrastructure.persistence.exposed.table

import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.time.LocalDateTime

/**
 * Base table with common fields (id, createdAt, updatedAt).
 */
abstract class BaseTable(name: String) : UUIDTable(name) {
    val createdAt = datetime("created_at").default(LocalDateTime.now().toKotlinLocalDateTime())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now().toKotlinLocalDateTime())
}
