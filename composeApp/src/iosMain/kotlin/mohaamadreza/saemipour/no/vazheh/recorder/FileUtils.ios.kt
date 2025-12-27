package mohaamadreza.saemipour.no.vazheh.recorder

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.timeIntervalSince1970
import platform.posix.memcpy

actual fun getCacheDirectory(): String {
    val paths = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    )
    return paths.firstOrNull() as? String ?: ""
}

@OptIn(ExperimentalForeignApi::class)
actual fun readFileAsBytes(filePath: String): ByteArray? {
    val data = NSData.dataWithContentsOfFile(filePath) ?: return null
    val bytes = ByteArray(data.length.toInt())
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
actual fun readBytesFromUri(uri: String): ByteArray? {
    // On iOS, the URI is typically a file path or file URL
    val data = if (uri.startsWith("file://")) {
        val nsUrl = NSURL.URLWithString(uri) ?: return null
        NSData.dataWithContentsOfURL(nsUrl)
    } else {
        if (uri.startsWith("/")) {
            // It's a file path
            NSData.dataWithContentsOfFile(uri)
        } else {
            // Try as URL
            val nsUrl = NSURL.URLWithString(uri) ?: return null
            NSData.dataWithContentsOfURL(nsUrl)
        }
    } ?: return null

    val bytes = ByteArray(data.length.toInt())
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
actual fun deleteFile(filePath: String): Boolean {
    return NSFileManager.defaultManager.removeItemAtPath(filePath, null)
}

actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

