package com.nekgamebling.infrastructure.persistence.exposed.table

import com.nekgamebling.shared.value.Aggregator
import com.nekgamebling.shared.value.ImageMap
import com.nekgamebling.shared.value.LocaleName
import com.nekgamebling.shared.value.Platform
import com.nekgamebling.shared.value.SpinType
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.json.jsonb
import java.time.LocalDateTime

/**
 * Base table with common fields (id, createdAt, updatedAt).
 */
abstract class BaseTable(name: String) : UUIDTable(name) {
    val createdAt = datetime("created_at").default(LocalDateTime.now().toKotlinLocalDateTime())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now().toKotlinLocalDateTime())
}

// ============================================
// Game Tables
// ============================================

object GameTable : BaseTable("games") {
    val identity = varchar("identity", 100).uniqueIndex()
    val name = varchar("name", 100)
    val providerId = reference("provider_id", ProviderTable.id)
    val images = jsonb<ImageMap>("images", Json.Default).default(ImageMap.EMPTY)
    val bonusBetEnable = bool("bonus_bet_enable").default(true)
    val bonusWageringEnable = bool("bonus_wagering_enable").default(true)
    val tags = array<String>("tags").default(emptyList())
    val active = bool("active").default(true)
}

object GameVariantTable : BaseTable("game_variants") {
    val gameId = reference("game_id", GameTable.id).nullable()
    val symbol = varchar("symbol", 100)
    val name = varchar("name", 255)
    val providerName = varchar("provider_name", 60)
    val aggregator = enumeration<Aggregator>("aggregator")
    val playLines = integer("play_lines")
    val freeSpinEnable = bool("free_spin_enable")
    val freeChipEnable = bool("free_chip_enable")
    val jackpotEnable = bool("jackpot_enable")
    val demoEnable = bool("demo_enable")
    val bonusBuyEnable = bool("bonus_buy_enable")
    val locales = array<String>("locales")
    val platforms = array<String>("platforms")

    init {
        uniqueIndex(symbol, aggregator)
    }
}

object GameFavouriteTable : UUIDTable("game_favourites") {
    val playerId = varchar("player_id", 100)
    val gameId = reference("game_id", GameTable.id)

    init {
        uniqueIndex(playerId, gameId)
    }
}

object GameWonTable : BaseTable("game_wons") {
    val gameId = reference("game_id", GameTable.id)
    val playerId = varchar("player_id", 100)
    val amount = integer("amount")
    val currency = varchar("currency", 3)
}

// ============================================
// Provider Tables
// ============================================

object ProviderTable : BaseTable("providers") {
    val identity = varchar("identity", 100).uniqueIndex()
    val name = varchar("name", 100)
    val images = jsonb<ImageMap>("images", Json.Default).default(ImageMap.EMPTY)
    val order = integer("order").default(100)
    val aggregatorId = reference("aggregator_id", AggregatorInfoTable.id).nullable()
    val active = bool("active").default(true)
}

// ============================================
// Aggregator Tables
// ============================================

object AggregatorInfoTable : BaseTable("aggregator_infos") {
    val identity = varchar("identity", 100).uniqueIndex()
    val config = jsonb<Map<String, String>>("config", Json.Default)
    val aggregator = enumeration<Aggregator>("aggregator")
    val active = bool("active").default(true)
}

// ============================================
// Collection Tables
// ============================================

object CollectionTable : BaseTable("collections") {
    val identity = varchar("identity", 100).uniqueIndex()
    val name = jsonb<LocaleName>("name", Json.Default)
    val images = jsonb<ImageMap>("images", Json.Default).default(ImageMap.EMPTY)
    val active = bool("active").default(true)
    val order = integer("order").default(100)
}

object CollectionGameTable : UUIDTable("collection_games") {
    val categoryId = reference("category_id", CollectionTable.id)
    val gameId = reference("game_id", GameTable.id)
    val order = integer("order").default(0)

    init {
        uniqueIndex(categoryId, gameId)
    }
}

// ============================================
// Session Tables
// ============================================

object SessionTable : BaseTable("sessions") {
    val gameId = reference("game_id", GameTable.id)
    val aggregatorId = reference("aggregator_id", AggregatorInfoTable.id)
    val playerId = varchar("player_id", 100)
    val token = varchar("token", 255)
    val externalToken = varchar("external_token", 255).nullable().default(null)
    val currency = varchar("currency", 3)
    val locale = varchar("locale", 10)
    val platform = enumeration<Platform>("platform")
}

object RoundTable : BaseTable("rounds") {
    val sessionId = reference("session_id", SessionTable.id)
    val gameId = reference("game_id", GameTable.id)
    val extId = varchar("ext_id", 255)
    val finished = bool("finished").default(false)

    init {
        uniqueIndex(sessionId, extId)
    }
}

object SpinTable : BaseTable("spins") {
    val roundId = reference("round_id", RoundTable.id).nullable()
    val type = enumeration<SpinType>("type")
    val amount = integer("amount").nullable()
    val realAmount = integer("real_amount").nullable()
    val bonusAmount = integer("bonus_amount").nullable()
    val extId = varchar("ext_id", 255)
    val referenceId = reference("reference_id", SpinTable.id).nullable()
    val freeSpinId = varchar("free_spin_id", 255).nullable()
}
