package infrastructure.persistence.exposed.table

import infrastructure.persistence.exposed.repository.IdentityTable
import shared.value.ImageMap
import shared.value.LocaleName
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.json.jsonb

object CollectionTable : BaseTable("collections"), IdentityTable {
    override val identity = varchar("identity", 100).uniqueIndex()
    val name = jsonb<LocaleName>("name", Json.Default)
    val images = jsonb<ImageMap>("images", Json.Default).default(ImageMap.EMPTY)
    val active = bool("active").default(true)
    val order = integer("order").default(100)
}
