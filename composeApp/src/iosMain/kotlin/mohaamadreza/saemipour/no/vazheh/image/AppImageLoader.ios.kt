package mohaamadreza.saemipour.no.vazheh.image

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal actual fun imageCacheDir(context: PlatformContext): Path {
    val base = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    ).firstOrNull() as? String ?: ""
    return "$base/image_cache".toPath()
}
