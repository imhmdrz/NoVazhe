package mohaamadreza.saemipour.no.vazheh.services

/**
 * Local content storage service - سرویس میزبانی محتوا
 *
 * Replaces the external imagekit.io host: static image/audio files live under a local
 * directory ([contentDir]) and are served by Ktor at [PUBLIC_PATH]. [publicUrl] builds the
 * absolute URL that clients (Coil for images, the audio player for mp3) load.
 *
 * Because URLs are stored absolutely in the database (like the old imagekit links), the host
 * must be reachable from the client device. Override it with the `CONTENT_BASE_URL` env var:
 *   - Android emulator (default): http://10.0.2.2:8080
 *   - iOS simulator / desktop:    http://localhost:8080
 *   - Real device:                http://<your-LAN-ip>:8080
 *   - Production:                 https://your-domain
 */
object StorageService {

    /** URL path prefix the static files are served under. */
    const val PUBLIC_PATH = "/content"

    /** Filesystem directory (relative to the server's working dir) that holds the content. */
    val contentDir: String = System.getenv("CONTENT_DIR")?.takeIf { it.isNotBlank() } ?: "content"

    /** Public base URL of this server as seen by clients. */
    val baseUrl: String = (System.getenv("CONTENT_BASE_URL")?.takeIf { it.isNotBlank() }
        ?: "http://10.0.2.2:8080").trimEnd('/')

    /**
     * Build the absolute URL for a content file given its path relative to the content root,
     * e.g. `publicUrl("words/fruits/apple.png")` -> `http://host/content/words/fruits/apple.png`.
     */
    fun publicUrl(relativePath: String): String {
        val clean = relativePath.trim().trimStart('/')
        return "$baseUrl$PUBLIC_PATH/$clean"
    }

    /** Convenience builders mirroring the seed folder layout. */
    fun categoryImage(fileName: String): String = publicUrl(fileName)
    fun wordImage(category: String, fileName: String): String = publicUrl("words/$category/$fileName")
    fun wordAudio(category: String, fileName: String): String = publicUrl("audio/$category/$fileName")
}
