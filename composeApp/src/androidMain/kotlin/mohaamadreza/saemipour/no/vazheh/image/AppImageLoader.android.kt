package mohaamadreza.saemipour.no.vazheh.image

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toOkioPath

internal actual fun imageCacheDir(context: PlatformContext): Path =
    context.cacheDir.resolve("image_cache").toOkioPath()
