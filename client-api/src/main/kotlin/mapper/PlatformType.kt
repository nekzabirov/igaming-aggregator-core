package mapper

import com.nekzabirov.igambling.proto.dto.PlatformType
import core.value.Platform

fun Platform.toPlatformProto() = when(this) {
    Platform.DESKTOP -> PlatformType.PLATFORM_TYPE_DESKTOP
    Platform.MOBILE -> PlatformType.PLATFORM_TYPE_MOBILE
    Platform.DOWNLOAD -> PlatformType.PLATFORM_TYPE_DOWNLOAD
}