package mohaamadreza.saemipour.no.vazheh.pinning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberAppPinner(): AppPinner = remember { IosAppPinner }

/**
 * iOS has no public API for entering Guided Access programmatically — the user
 * must triple-click the side / home button and enter their passcode. We expose
 * a stub so the shared dialog can render a graceful "not supported" message.
 */
private object IosAppPinner : AppPinner {
    override fun pin(): AppPinningResult = AppPinningResult.Unsupported
    override suspend fun pinAndAwait(timeoutMs: Long): AppPinningResult = AppPinningResult.Unsupported
    override fun unpin(): Boolean = false
    override fun isPinned(): Boolean = false
}
