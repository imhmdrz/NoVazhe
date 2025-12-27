package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.isAndroidPlatform
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.components.AddChildDialog
import mohaamadreza.saemipour.no.vazheh.ui.components.ChildrenListContent
import mohaamadreza.saemipour.no.vazheh.ui.components.ErrorContent
import mohaamadreza.saemipour.no.vazheh.ui.components.KidsModeGuideDialog
import mohaamadreza.saemipour.no.vazheh.ui.components.LoadingContent
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealLight
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.MotherViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.ChildrenState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CustomWordsState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.MotherTab

@Composable
fun MotherScreen(
    navController: NavController,
    viewModel: MotherViewModel,
    childViewModel: ChildViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val childUiState by childViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentPlayingAudioUrl by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var wasPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    // Audio updates callback
    val audioUpdates = remember {
        object : AudioUpdates {
            override fun onProgressUpdate(playerState: PlayerState) {
                isAudioPlaying = playerState.isPlaying
                // Only reset when audio was playing and now stopped (playback finished)
                if (wasPlaying && !playerState.isPlaying) {
                    currentPlayingAudioUrl = null
                }
                wasPlaying = playerState.isPlaying
            }

            override fun onReady() {
                // Audio is ready to play
            }

            override fun onError(exception: Exception) {
                currentPlayingAudioUrl = null
                isAudioPlaying = false
                wasPlaying = false
            }
        }
    }

    AudioProvider(audioUpdates = audioUpdates) { audioPlayer ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = TealPurple,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                bottomBar = {
                    MotherBottomNavigation(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = viewModel::onTabSelected
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(SoftGray)
                ) {
                    when (uiState.selectedTab) {
                        MotherTab.DASHBOARD -> DashboardContent(
                            childrenState = uiState.childrenState,
                            customWordsState = uiState.customWordsState,
                            onChildClick = { child ->
                                viewModel.selectChild(child)
                                childViewModel.setSelectedChild(child)
                                navController.navigate("child")
                            },
                            onAddChildClick = {
                                viewModel.showAddChildDialog()
                            },
                            onRetry = { viewModel.retry() },
                            canAddChild = uiState.canAddChild,
                            onAddWordClick = {
                                navController.navigate("add-word")
                            },
                            currentPlayingAudioUrl = if (isAudioPlaying) currentPlayingAudioUrl else null,
                            onPlayAudio = { audioUrl ->
                                // Stop previous playback if any
                                if (currentPlayingAudioUrl != null && currentPlayingAudioUrl != audioUrl) {
                                    audioPlayer.pause()
                                }
                                currentPlayingAudioUrl = audioUrl
                                wasPlaying = false
                                audioPlayer.play(audioUrl)
                            },
                            onStopAudio = {
                                audioPlayer.pause()
                                currentPlayingAudioUrl = null
                                isAudioPlaying = false
                                wasPlaying = false
                            },
                            onDeleteCustomWord = { wordId ->
                                viewModel.deleteCustomWord(wordId)
                            }
                        )
                        MotherTab.PROFILE -> ProfileContent(
                            username = uiState.username,
                            displayName = uiState.displayName,
                            timerDurationMinutes = childUiState.timerDurationMinutes,
                            onTimerDurationChange = { minutes ->
                                childViewModel.setTimerDuration(minutes)
                            },
                            onLogout = {
                                viewModel.logout()
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }

            if (uiState.showAddChildDialog) {
                AddChildDialog(
                    isLoading = uiState.isCreatingChild,
                    errorMessage = uiState.createChildErrorMessage,
                    onDismiss = { viewModel.hideAddChildDialog() },
                    onAddChild = { name, age, gender ->
                        viewModel.createChild(name = name, age = age, gender = gender)
                    },
                    onClearError = { viewModel.clearCreateChildError() }
                )
            }
        }
    }
}

@Composable
private fun MotherBottomNavigation(
    selectedTab: MotherTab,
    onTabSelected: (MotherTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected = selectedTab == MotherTab.DASHBOARD,
            onClick = { onTabSelected(MotherTab.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MotherTab.DASHBOARD) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "داشبورد",
                    modifier = Modifier.size(28.dp)
                )
            },
            label = {
                Text(
                    text = "داشبورد",
                    fontWeight = if (selectedTab == MotherTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPurple,
                selectedTextColor = TealPurple,
                unselectedIconColor = MutedText,
                unselectedTextColor = MutedText,
                indicatorColor = TealPurple.copy(alpha = 0.08f)
            )
        )

        NavigationBarItem(
            selected = selectedTab == MotherTab.PROFILE,
            onClick = { onTabSelected(MotherTab.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MotherTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "پروفایل",
                    modifier = Modifier.size(28.dp)
                )
            },
            label = {
                Text(
                    text = "پروفایل",
                    fontWeight = if (selectedTab == MotherTab.PROFILE) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPurple,
                selectedTextColor = TealPurple,
                unselectedIconColor = MutedText,
                unselectedTextColor = MutedText,
                indicatorColor = TealPurple.copy(alpha = 0.08f)
            )
        )
    }
}

@Composable
private fun ProfileContent(
    username: String,
    displayName: String,
    timerDurationMinutes: Int,
    onTimerDurationChange: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var showKidsModeGuide by remember { mutableStateOf(false) }
    
    // Kids Mode Guide Dialog
    if (showKidsModeGuide) {
        KidsModeGuideDialog(
            onDismiss = { showKidsModeGuide = false },
            isAndroid = isAndroidPlatform()
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with gradient background
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(TealPurple, TealLight)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "سلام، ${displayName.ifEmpty { "کاربر" }}!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // User Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UserInfoRow(
                    label = "نام کاربری",
                    value = username.ifEmpty { "تنظیم نشده" }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timer Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "⏱️ تنظیم زمان بازی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Text(
                    text = "مدت زمان مجاز بازی فرزند را تنظیم کنید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText
                )

                // Timer duration selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimerOptionButton(
                        minutes = 1,
                        isSelected = timerDurationMinutes == 1,
                        onClick = { onTimerDurationChange(1) }
                    )
                    TimerOptionButton(
                        minutes = 10,
                        isSelected = timerDurationMinutes == 10,
                        onClick = { onTimerDurationChange(10) }
                    )
                    TimerOptionButton(
                        minutes = 15,
                        isSelected = timerDurationMinutes == 15,
                        onClick = { onTimerDurationChange(15) }
                    )
                    TimerOptionButton(
                        minutes = 30,
                        isSelected = timerDurationMinutes == 30,
                        onClick = { onTimerDurationChange(30) }
                    )
                }

                // Current selection display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = TealPurple.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "زمان انتخاب شده: $timerDurationMinutes دقیقه",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TealPurple
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Kids Mode Guide Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showKidsModeGuide = true },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.size(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "حالت کودک",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "راهنمای قفل صفحه برای جلوگیری از خروج کودک",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
                
                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MutedText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CoralRed
            )
        ) {
            Text(
                text = "خروج از حساب کاربری",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TimerOptionButton(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(
                color = if (isSelected) TealPurple else SoftGray,
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$minutes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else DarkText
        )
    }
}

@Composable
private fun UserInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MutedText
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = DarkText
        )
    }
}

@Composable
private fun DashboardContent(
    childrenState: ChildrenState,
    customWordsState: CustomWordsState,
    onChildClick: (ChildDTO) -> Unit,
    onAddChildClick: () -> Unit,
    onAddWordClick: () -> Unit,
    onRetry: () -> Unit,
    canAddChild: Boolean,
    currentPlayingAudioUrl: String? = null,
    onPlayAudio: (audioUrl: String) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onDeleteCustomWord: (wordId: Int) -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        when (childrenState) {
            is ChildrenState.Idle -> {
                // Initial state - show nothing or loading
            }
            
            is ChildrenState.Loading -> {
                // Loading state
                LoadingContent()
            }
            
            is ChildrenState.Error -> {
                // Error state
                ErrorContent(
                    message = childrenState.message,
                    onRetry = onRetry
                )
            }
            
            is ChildrenState.Success -> {
                // Success state - show children list
                ChildrenListContent(
                    children = childrenState.children,
                    customWordsState = customWordsState,
                    onChildClick = onChildClick,
                    onAddChildClick = onAddChildClick,
                    onAddWordClick = onAddWordClick,
                    canAddChild = canAddChild,
                    onRetry = onRetry,
                    currentPlayingAudioUrl = currentPlayingAudioUrl,
                    onPlayAudio = onPlayAudio,
                    onStopAudio = onStopAudio,
                    onDeleteCustomWord = onDeleteCustomWord
                )
            }
        }
    }
}