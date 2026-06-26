package mohaamadreza.saemipour.no.vazheh

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import mohaamadreza.saemipour.no.vazheh.ui.animation.LocalNavAnimatedVisibilityScope
import mohaamadreza.saemipour.no.vazheh.ui.animation.LocalSharedTransitionScope
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

@OptIn(ExperimentalSharedTransitionApi::class)
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
        // The app always opens on the (guest-capable) dashboard; logging in is only
        // required to reach the Profile tab.
        val startDestination = remember { "mother" }

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
                        // Token is invalid/expired - clear it and drop back to the guest
                        // dashboard (not a login wall). توکن نامعتبر - بازگشت به داشبورد مهمان
                        AppLogger.d("App", "Unauthorized - returning to guest dashboard")
                        authViewModel.forceLogout()
                        motherViewModel.retry()
                        navController.navigate("mother") {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                    is AuthEvent.LoggedOut -> {
                        // Logout keeps the user in the app as a guest; just refresh so the
                        // Profile tab re-locks. خروج: ماندن در برنامه به‌صورت مهمان
                        motherViewModel.retry()
                    }

                    is AuthEvent.LoggedIn -> {
                        // Refresh so the Profile tab unlocks; AuthScreen handles navigation.
                        motherViewModel.retry()
                    }
                }
            }
        }

        val navEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navEntry?.destination?.route

        // Default screen-to-screen transition: a directional slide + fade that respects the
        // back stack (forward pushes from the end, Back pops toward the start).
        val slideDuration = 350
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        modifier =
                            Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .safeDrawingPadding(),
                        navController = navController,
                        startDestination = startDestination,
                        enterTransition = {
                            slideIntoContainer(SlideDirection.Start, tween(slideDuration)) + fadeIn(
                                tween(slideDuration)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                SlideDirection.Start,
                                tween(slideDuration)
                            ) + fadeOut(tween(slideDuration))
                        },
                        popEnterTransition = {
                            slideIntoContainer(SlideDirection.End, tween(slideDuration)) + fadeIn(
                                tween(slideDuration)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(SlideDirection.End, tween(slideDuration)) + fadeOut(
                                tween(slideDuration)
                            )
                        }
                    ) {
                        // Games launched from the dashboard "zoom in" toward the child rather than slide.
                        val zoomEnter =
                            scaleIn(tween(slideDuration), initialScale = 0.82f) + fadeIn(
                                tween(slideDuration)
                            )
                        val zoomPopExit =
                            scaleOut(tween(slideDuration), targetScale = 0.82f) + fadeOut(
                                tween(slideDuration)
                            )
                        composable(
                            "face-game",
                            enterTransition = { zoomEnter },
                            popExitTransition = { zoomPopExit }
                        ) {
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
                        composable(
                            "game",
                            // Fade only: the category image shared-element does the visual heavy lifting here.
                            enterTransition = { fadeIn(tween(slideDuration)) },
                            exitTransition = { fadeOut(tween(slideDuration)) },
                            popEnterTransition = { fadeIn(tween(slideDuration)) },
                            popExitTransition = { fadeOut(tween(slideDuration)) }
                        ) {
                            CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                                OrientationWrapper(Orientation.Vertical) {
                                    GameScreen(
                                        navController = navController,
                                        viewModel = childViewModel,
                                        quizViewModel = quizViewModel
                                    )
                                }
                            }
                        }
                        composable("mother") {
                            LaunchedEffect(Unit) {
                                motherViewModel.retry()
                            }
                            CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                                OrientationWrapper(Orientation.Vertical) {
                                    MotherScreen(
                                        navController = navController,
                                        viewModel = motherViewModel,
                                        childViewModel = childViewModel,
                                        quizViewModel = quizViewModel
                                    )
                                }
                            }
                        }
                        composable("add-word") {
                            OrientationWrapper(Orientation.Vertical) {
                                AddWordScreen(
                                    navController = navController,
                                    viewModel = motherViewModel
                                )
                            }
                        }
                        composable(
                            "quiz",
                            enterTransition = { zoomEnter },
                            popExitTransition = { zoomPopExit }
                        ) {
                            OrientationWrapper(Orientation.Vertical) {
                                QuizScreen(navController = navController, viewModel = quizViewModel)
                            }
                        }
                        composable(
                            route = "memory-game/{categoryId}",
                            arguments = listOf(navArgument("categoryId") {
                                type = NavType.IntType
                            }),
                            enterTransition = { zoomEnter },
                            popExitTransition = { zoomPopExit }
                        ) { backStackEntry ->
                            val categoryId =
                                backStackEntry.arguments?.read { getInt("categoryId") } ?: 0
                            val viewModel: MemoryGameViewModel = koinViewModel()
                            OrientationWrapper(Orientation.Vertical) {
                                MemoryGameScreen(
                                    navController = navController,
                                    viewModel = viewModel,
                                    categoryId = categoryId
                                )
                            }
                        }
                        composable(
                            "color-sorting",
                            enterTransition = { zoomEnter },
                            popExitTransition = { zoomPopExit }
                        ) {
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
                            arguments = listOf(navArgument("categoryId") {
                                type = NavType.IntType
                            }),
                            enterTransition = { zoomEnter },
                            popExitTransition = { zoomPopExit }
                        ) { backStackEntry ->
                            val categoryId =
                                backStackEntry.arguments?.read { getInt("categoryId") } ?: 0
                            val viewModel: ShadowMatchViewModel = koinViewModel()
                            OrientationWrapper(Orientation.Vertical) {
                                ShadowMatchScreen(
                                    navController = navController,
                                    viewModel = viewModel,
                                    categoryId = categoryId
                                )
                            }
                        }
                        composable(
                            "odd-one-out",
                            enterTransition = { zoomEnter },
                            popExitTransition = { zoomPopExit }
                        ) {
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
    }
}

@Composable
expect inline fun OrientationWrapper(nextOrientation: Orientation, content: @Composable () -> Unit)
