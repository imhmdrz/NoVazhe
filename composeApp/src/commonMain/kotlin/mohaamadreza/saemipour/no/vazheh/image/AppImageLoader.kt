package mohaamadreza.saemipour.no.vazheh.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import okio.Path

/**
 * Builds a singleton [ImageLoader] with a persistent on-disk cache and an
 * in-memory cache so every `AsyncImage` in the app reuses downloaded photos
 * across recompositions, navigations, and app restarts.
 *
 * Disk cache: ~512 MB under the platform cache directory.
 * Memory cache: 25% of the app's available memory.
 */
fun newImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(imageCacheDir(context))
                .maxSizeBytes(IMAGE_DISK_CACHE_MAX_BYTES)
                .build()
        }
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)
        .build()

private const val IMAGE_DISK_CACHE_MAX_BYTES: Long = 512L * 1024L * 1024L

/** Platform-specific directory used for the Coil disk cache. */
internal expect fun imageCacheDir(context: PlatformContext): Path
