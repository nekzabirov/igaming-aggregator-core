package com.nekgamebling.infrastructure.persistence.exposed.adapter

import com.nekgamebling.application.port.inbound.GamePort
import com.nekgamebling.domain.aggregator.model.AggregatorInfo
import com.nekgamebling.domain.game.model.GameVariant
import com.nekgamebling.domain.game.model.GameVariantWithDetail
import com.nekgamebling.infrastructure.persistence.exposed.mapper.toGame
import com.nekgamebling.infrastructure.persistence.exposed.mapper.toGameVariant
import com.nekgamebling.infrastructure.persistence.exposed.mapper.toProvider
import com.nekgamebling.infrastructure.persistence.exposed.table.GameTable
import com.nekgamebling.infrastructure.persistence.exposed.table.GameVariantTable
import com.nekgamebling.infrastructure.persistence.exposed.table.ProviderTable
import com.nekgamebling.shared.extension.toUrlSlug
import com.nekgamebling.shared.value.Aggregator
import com.nekgamebling.shared.value.Page
import com.nekgamebling.shared.value.Pageable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsertReturning

class ExposedGamePort : GamePort {
    override suspend fun syncGame(variants: List<GameVariant>, aggregatorInfo: AggregatorInfo) =
        newSuspendedTransaction {
            for (gameVariant in variants) {
                if (gameVariant.gameId != null) continue

                val providerIdentity = gameVariant.providerName.toUrlSlug()

                val provider = ProviderTable.upsertReturning(
                    keys = arrayOf(ProviderTable.identity),
                    onUpdateExclude = listOf(
                        ProviderTable.id,
                        ProviderTable.name,
                        ProviderTable.createdAt,
                        ProviderTable.name
                    ),
                ) {
                    it[identity] = providerIdentity
                    it[name] = gameVariant.providerName
                }.single().toProvider()

                val gameIdentity = "${provider.identity}-${gameVariant.name}".toUrlSlug()

                var gameId = GameTable.select(GameTable.id)
                    .where { ((GameTable.identity eq gameIdentity) or (GameTable.name eq gameVariant.name)) and (GameTable.providerId eq provider.id) }
                    .singleOrNull()?.get(GameTable.id)?.value

                if (gameId == null) {
                    gameId = GameTable.insertAndGetId {
                        it[identity] = gameIdentity
                        it[name] = gameVariant.name
                        it[providerId] = provider.id
                    }.value
                }

                GameVariantTable.update(where = { GameVariantTable.id eq gameVariant.id }) {
                    it[GameVariantTable.gameId] = gameId
                }
            }
        }

    override suspend fun findVariantsAll(
        query: String,
        aggregator: Aggregator?,
        gameIdentity: String?,
        pageable: Pageable
    ): Page<GameVariantWithDetail> = newSuspendedTransaction {
        val query = GameVariantTable
            .leftJoin(GameTable, onColumn = { GameVariantTable.gameId }, otherColumn = { GameTable.id })
            .leftJoin(ProviderTable, onColumn = { ProviderTable.id }, otherColumn = { GameTable.providerId })
            .selectAll()
            .apply {
                andWhere {
                    val queryS = "%${query}%"
                    (GameVariantTable.name like queryS) or
                            (GameVariantTable.providerName like queryS) or
                            (GameVariantTable.symbol like queryS)
                }

                aggregator?.also { providerConfigType ->
                    andWhere { GameVariantTable.aggregator eq providerConfigType }
                }

                gameIdentity?.also { gameIdentity ->
                    andWhere { GameTable.identity eq gameIdentity }
                }
            }

        val totalCount = query.count()

        val items = query
            .limit(pageable.sizeReal)
            .offset(pageable.offset)
            .map {
                GameVariantWithDetail(
                    variant = it.toGameVariant(),
                    game = if (it[GameTable.id] != null) it.toGame() else null,
                    provider = if (it[ProviderTable.id] != null) it.toProvider() else null
                )
            }

        Page(
            items = items,
            totalPages = pageable.getTotalPages(totalCount),
            totalItems = totalCount,
            currentPage = pageable.pageReal
        )
    }
}