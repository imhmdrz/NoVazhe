package mohaamadreza.saemipour.no.vazheh.recorder

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

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

