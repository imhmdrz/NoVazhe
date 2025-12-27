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
 * Read bytes from a content URI (e.g., content://media/...)
 * On Android, this uses ContentResolver
 * On iOS, this reads from file path
 */
expect fun readBytesFromUri(uri: String): ByteArray?

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

/**
 * Convert image URI to data URL format
 */
@OptIn(ExperimentalEncodingApi::class)
fun imageUriToDataUrl(uri: String, mimeType: String = "image/jpeg"): Pair<String, ByteArray>? {
    val bytes = readBytesFromUri(uri) ?: return null
    val base64Content = Base64.encode(bytes)
    return "data:$mimeType;base64,$base64Content" to bytes
}

