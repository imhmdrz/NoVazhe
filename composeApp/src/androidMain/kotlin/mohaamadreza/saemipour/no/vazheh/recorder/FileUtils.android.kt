package mohaamadreza.saemipour.no.vazheh.recorder

import android.content.Context
import android.net.Uri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import androidx.core.net.toUri

private object AndroidFileUtils : KoinComponent {
    val context: Context by inject()
}

actual fun getCacheDirectory(): String {
    return AndroidFileUtils.context.cacheDir.absolutePath
}

actual fun readFileAsBytes(filePath: String): ByteArray? {
    return try {
        File(filePath).readBytes()
    } catch (e: Exception) {
        null
    }
}

actual fun readBytesFromUri(uri: String): ByteArray? {
    return try {
        val context = AndroidFileUtils.context
        val contentUri = uri.toUri()
        context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

actual fun deleteFile(filePath: String): Boolean {
    return try {
        File(filePath).delete()
    } catch (e: Exception) {
        false
    }
}

actual fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}

