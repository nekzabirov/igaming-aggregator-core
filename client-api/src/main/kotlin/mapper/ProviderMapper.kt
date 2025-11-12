package mapper

import com.djmhub.game.shared.domain.model.Provider

fun Provider.toProto(): com.djmhub.catalog.proto.dto.ProviderDto = com.djmhub.catalog.proto.dto.ProviderDto.newBuilder()
    .setId(this.id.toString())
    .setIdentity(this.identity)
    .setName(this.name)
    .setOrder(this.order)
    .let {
        if (this.partnerIdentity != null) it.setPartnerIdentity(this.partnerIdentity)
        else it
    }
    .let {
        if (this.partnerType != null) it.setPartnerType(this.partnerType!!)
        else it
    }
    .build()