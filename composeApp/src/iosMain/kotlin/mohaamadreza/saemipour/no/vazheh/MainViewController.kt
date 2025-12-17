package mohaamadreza.saemipour.no.vazheh

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIInterfaceOrientationMaskLandscape
import platform.UIKit.UIInterfaceOrientationMaskPortrait
import platform.UIKit.UIWindowScene
import platform.UIKit.UIWindowSceneGeometryPreferencesIOS

fun MainViewController() = ComposeUIViewController { App() }

@Composable
actual inline fun OrientationWrapper(
    nextOrientation: Orientation,
    content: @Composable () -> Unit
) {
    LaunchedEffect(Unit) {
        val windowScene =
            UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
        println("window orientation: ${windowScene ?: "null"}")
        val nextOrientationPreference = UIWindowSceneGeometryPreferencesIOS(
            interfaceOrientations =
                when (nextOrientation) {
                    Orientation.Vertical -> UIInterfaceOrientationMaskPortrait
                    Orientation.Horizontal -> UIInterfaceOrientationMaskLandscape
                }

        )
        windowScene?.requestGeometryUpdateWithPreferences(
            geometryPreferences = nextOrientationPreference,
            errorHandler = {
                println("Orientation changing Error")
            }
        )
    }
    content()
}