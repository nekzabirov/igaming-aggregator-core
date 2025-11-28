package com.nekgamebling.infrastructure.persistence.exposed.table

import com.nekgamebling.shared.value.SpinType

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
