package mohaamadreza.saemipour.no.vazheh

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mohaamadreza.saemipour.no.vazheh.ui.screens.AuthScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.ChildScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.GameScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.MotherScreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.AppTheme
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.AuthViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.MotherViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    AppTheme {
        val navController = rememberNavController()

        val authViewModel: AuthViewModel = koinViewModel()
        val startDestination = remember { if (authViewModel.isLoggedIn()) "mother" else "auth" }

        // Shared ChildViewModel across child and game screens
        val childViewModel: ChildViewModel = koinViewModel()
        val motherViewModel: MotherViewModel = koinViewModel()

        NavHost(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding(),
            navController = navController,
            startDestination = startDestination
        ) {
            composable("auth") {
                OrientationWrapper(Orientation.Vertical) {
                    AuthScreen(navController = navController, viewModel = authViewModel)
                }
            }
            composable("child") {
                OrientationWrapper(Orientation.Vertical) {
                    ChildScreen(navController = navController, viewModel = childViewModel)
                }
            }
            composable("game") {
                OrientationWrapper(Orientation.Horizontal) {
                    GameScreen(navController = navController, viewModel = childViewModel)
                }
            }
            composable("mother") {
                OrientationWrapper(Orientation.Vertical) {
                    MotherScreen(navController = navController, viewModel = motherViewModel)
                }
            }
        }
    }
}

@Composable
expect inline fun OrientationWrapper(nextOrientation: Orientation, content: @Composable () -> Unit)
