package com.nekgamebling.config

import com.nekgamebling.infrastructure.persistence.exposed.table.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Database configuration.
 */
object DatabaseConfig {
    private val defaultUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    private val defaultDriver = "org.h2.Driver"

    /**
     * Initialize database connection.
     */
    fun init(
        url: String = System.getenv("DATABASE_URL") ?: defaultUrl,
        driver: String = System.getenv("DATABASE_DRIVER") ?: defaultDriver,
        user: String = System.getenv("DATABASE_USER") ?: "",
        password: String = System.getenv("DATABASE_PASSWORD") ?: ""
    ) {
        Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )

        createTables()
    }

    /**
     * Create all database tables.
     */
    private fun createTables() {
        transaction {
            SchemaUtils.create(
                // Aggregator tables
                AggregatorInfoTable,

                // Provider tables
                ProviderTable,

                // Game tables
                GameTable,
                GameVariantTable,
                GameFavouriteTable,
                GameWonTable,

                // Collection tables
                CollectionTable,
                CollectionGameTable,

                // Session tables
                SessionTable,
                RoundTable,
                SpinTable
            )
        }
    }

    /**
     * Drop all tables (use with caution - for testing only).
     */
    fun dropTables() {
        transaction {
            SchemaUtils.drop(
                SpinTable,
                RoundTable,
                SessionTable,
                CollectionGameTable,
                CollectionTable,
                GameWonTable,
                GameFavouriteTable,
                GameVariantTable,
                GameTable,
                ProviderTable,
                AggregatorInfoTable
            )
        }
    }
}
