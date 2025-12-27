package mohaamadreza.saemipour.no.vazheh.recorder

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Get the cache directory path for storing temporary audio files
 */
expect fun getCacheDirectory(): String

/**
 * Read file as byte array
 */
expect fun readFileAsBytes(filePath: String): ByteArray?

/**
 * Delete a file
 */
expect fun deleteFile(filePath: String): Boolean

/**
 * Get current time in milliseconds
 */
expect fun currentTimeMillis(): Long

/**
 * Convert audio file to data URL format
 */
@OptIn(ExperimentalEncodingApi::class)
fun audioFileToDataUrl(filePath: String, mimeType: String = "audio/mp4"): String? {
    val bytes = readFileAsBytes(filePath) ?: return null
    val base64Content = Base64.encode(bytes)
    return "data:$mimeType;base64,$base64Content"
}

