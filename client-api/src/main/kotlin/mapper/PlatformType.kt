package mapper

import com.djmhub.game.shared.domain.core.value.PlatformType

fun PlatformType.toProto() = when(this) {
    PlatformType.DESKTOP -> com.djmhub.catalog.proto.dto.PlatformType.PLATFORM_TYPE_DESKTOP
    PlatformType.MOBILE -> com.djmhub.catalog.proto.dto.PlatformType.PLATFORM_TYPE_MOBILE
    PlatformType.DOWNLOAD -> com.djmhub.catalog.proto.dto.PlatformType.PLATFORM_TYPE_DOWNLOAD
}