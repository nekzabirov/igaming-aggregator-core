package mapper

import com.djmhub.catalog.proto.dto.GameVariantDto
import com.djmhub.game.shared.domain.model.GameVariant

fun GameVariant.toProto(): GameVariantDto = GameVariantDto.newBuilder()
    .setId(this.id.toString())
    .setGameId(this.gameId.toString())
    .setSymbol(this.symbol)
    .setName(this.name)
    .setProviderName(this.providerName)
    .setPartner(this.partner)
    .setPlayLines(this.playLines)
    .setFreeChipEnable(this.freeChipEnable)
    .setFreeChipEnable(this.freeSpinEnable)
    .setJackpotEnable(this.jackpotEnable)
    .setDemoEnable(this.demoEnable)
    .setBonusBuyEnable(this.bonusBuyEnable)
    .addAllLocales(this.locales)
    .addAllPlatforms(this.platforms.map { it.toProto() })
    .build()