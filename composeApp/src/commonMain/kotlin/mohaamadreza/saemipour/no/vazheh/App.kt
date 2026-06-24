package mohaamadreza.saemipour.no.vazheh

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import mohaamadreza.saemipour.no.vazheh.data.AuthEvent
import mohaamadreza.saemipour.no.vazheh.data.AuthStateManager
import mohaamadreza.saemipour.no.vazheh.image.newImageLoader
import mohaamadreza.saemipour.no.vazheh.ui.components.AppPinToggle
import mohaamadreza.saemipour.no.vazheh.ui.screens.AddWordScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.AuthScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.ColorSortingScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.ColorSortingViewModel
import mohaamadreza.saemipour.no.vazheh.ui.screens.FaceGameScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.FaceGameViewModel
import mohaamadreza.saemipour.no.vazheh.ui.screens.GameScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.MemoryGameScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.MemoryGameViewModel
import mohaamadreza.saemipour.no.vazheh.ui.screens.MotherScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.OddOneOutScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.OddOneOutViewModel
import mohaamadreza.saemipour.no.vazheh.ui.screens.QuizScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.ShadowMatchScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.ShadowMatchViewModel
import mohaamadreza.saemipour.no.vazheh.ui.theme.AppTheme
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.AuthViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.MotherViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.QuizViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    AppTheme {
        // Configure the global Coil ImageLoader once so every AsyncImage in the
        // app benefits from a persistent disk cache + in-memory cache.
        val platformContext = LocalPlatformContext.current
        remember(platformContext) {
            SingletonImageLoader.setSafe { newImageLoader(platformContext) }
        }

        val navController = rememberNavController()

        val authViewModel: AuthViewModel = koinViewModel()
        val startDestination = remember { if (authViewModel.isLoggedIn()) "mother" else "auth" }

        // Shared ChildViewModel across child and game screens
        val childViewModel: ChildViewModel = koinViewModel()
        
        // QuizViewModel for quiz screens
        val quizViewModel: QuizViewModel = koinViewModel()

        val motherViewModel: MotherViewModel = koinViewModel()

        // Listen for auth events (401 unauthorized, logout, etc.)
        LaunchedEffect(Unit) {
            AuthStateManager.authEvents.collect { event ->
                when (event) {
                    is AuthEvent.Unauthorized -> {
                        // Token is invalid - clear storage and navigate to login
                        // توکن نامعتبر است - پاک کردن و رفتن به صفحه ورود
                        AppLogger.d("App", "Unauthorized - navigating to auth screen")
                        authViewModel.forceLogout()
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    is AuthEvent.LoggedOut -> {
                        // User logged out - navigate to login
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    is AuthEvent.LoggedIn -> {
                        // User logged in - handled by individual screens
                    }
                }
            }
        }

        val navEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navEntry?.destination?.route

        Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding(),
            navController = navController,
            startDestination = startDestination
        ) {
            composable("face-game") {
                val viewModel: FaceGameViewModel = koinViewModel()
                OrientationWrapper(Orientation.Vertical) {
                    FaceGameScreen(navController = navController, viewModel = viewModel)
                }
            }
            composable("auth") {
                OrientationWrapper(Orientation.Vertical) {
                    AuthScreen(navController = navController, viewModel = authViewModel)
                }
            }
            composable("game") {
                OrientationWrapper(Orientation.Vertical) {
                    GameScreen(navController = navController, viewModel = childViewModel)
                }
            }
            composable("mother") {
                LaunchedEffect(Unit) {
                    motherViewModel.retry()
                }
                OrientationWrapper(Orientation.Vertical) {
                    MotherScreen(
                        navController = navController,
                        viewModel = motherViewModel,
                        childViewModel = childViewModel,
                        quizViewModel = quizViewModel
                    )
                }
            }
            composable("add-word") {
                OrientationWrapper(Orientation.Vertical) {
                    AddWordScreen(navController = navController, viewModel = motherViewModel)
                }
            }
            composable("quiz") {
                OrientationWrapper(Orientation.Vertical) {
                    QuizScreen(navController = navController, viewModel = quizViewModel)
                }
            }
            composable(
                route = "memory-game/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.read { getInt("categoryId") } ?: 0
                val viewModel: MemoryGameViewModel = koinViewModel()
                OrientationWrapper(Orientation.Vertical) {
                    MemoryGameScreen(
                        navController = navController,
                        viewModel = viewModel,
                        categoryId = categoryId
                    )
                }
            }
            composable("color-sorting") {
                val viewModel: ColorSortingViewModel = koinViewModel()
                OrientationWrapper(Orientation.Vertical) {
                    ColorSortingScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
            composable(
                route = "shadow-match/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.read { getInt("categoryId") } ?: 0
                val viewModel: ShadowMatchViewModel = koinViewModel()
                OrientationWrapper(Orientation.Vertical) {
                    ShadowMatchScreen(
                        navController = navController,
                        viewModel = viewModel,
                        categoryId = categoryId
                    )
                }
            }
            composable("odd-one-out") {
                val viewModel: OddOneOutViewModel = koinViewModel()
                OrientationWrapper(Orientation.Vertical) {
                    OddOneOutScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }

            // Global lock/unlock padlock — shown everywhere except the auth screen.
            if (currentRoute != null && currentRoute != "auth") {
                AppPinToggle(
                    refreshKey = currentRoute,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
expect inline fun OrientationWrapper(nextOrientation: Orientation, content: @Composable () -> Unit)
