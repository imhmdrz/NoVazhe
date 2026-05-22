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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import mohaamadreza.saemipour.no.vazheh.ui.components.ChildContent
import mohaamadreza.saemipour.no.vazheh.ui.components.CustomWordContent
import mohaamadreza.saemipour.no.vazheh.ui.components.EmptyChildrenContent
import mohaamadreza.saemipour.no.vazheh.ui.components.ErrorContent
import mohaamadreza.saemipour.no.vazheh.ui.components.KidsModeGuideDialog
import mohaamadreza.saemipour.no.vazheh.ui.components.LoadingContent
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.MotherViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.QuizViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.ChildrenState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CustomWordsState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.MotherTab

@Composable
fun MotherScreen(
    navController: NavController,
    viewModel: MotherViewModel,
    childViewModel: ChildViewModel,
    quizViewModel: QuizViewModel
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

    // Sync auto-selected child from MotherViewModel to ChildViewModel
    LaunchedEffect(uiState.selectedChild?.id) {
        uiState.selectedChild?.let { childViewModel.setSelectedChild(it) }
    }

    // Auto-prompt to add a child if user has no children yet
    LaunchedEffect(uiState.childrenState) {
        val state = uiState.childrenState
        if (state is ChildrenState.Success && state.children.isEmpty() &&
            !uiState.showAddChildDialog
        ) {
            viewModel.showAddChildDialog()
        }
    }

    // Audio updates callback
    val audioUpdates = remember {
        object : AudioUpdates {
            override fun onProgressUpdate(playerState: PlayerState) {
                isAudioPlaying = playerState.isPlaying
                if (wasPlaying && !playerState.isPlaying) {
                    currentPlayingAudioUrl = null
                }
                wasPlaying = playerState.isPlaying
            }

            override fun onReady() {}

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
                        MotherTab.DASHBOARD -> ChildScreen(
                            navController = navController,
                            viewModel = childViewModel,
                            quizViewModel = quizViewModel
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
                            },
                            childrenState = uiState.childrenState,
                            customWordsState = uiState.customWordsState,
                            onChildClick = { child ->
                                viewModel.selectChild(child)
                                childViewModel.setSelectedChild(child)
                                viewModel.onTabSelected(MotherTab.DASHBOARD)
                            },
                            onAddChildClick = { viewModel.showAddChildDialog() },
                            onRetry = { viewModel.retry() },
                            canAddChild = uiState.canAddChild,
                            onAddWordClick = { navController.navigate("add-word") },
                            currentPlayingAudioUrl =
                                if (isAudioPlaying) currentPlayingAudioUrl else null,
                            onPlayAudio = { audioUrl ->
                                if (currentPlayingAudioUrl != null &&
                                    currentPlayingAudioUrl != audioUrl
                                ) {
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
                    contentDescription = "تنظیمات",
                    modifier = Modifier.size(28.dp)
                )
            },
            label = {
                Text(
                    text = "تنظیمات",
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

// ==================== PROFILE ====================

@Composable
private fun ProfileContent(
    username: String,
    displayName: String,
    timerDurationMinutes: Int,
    onTimerDurationChange: (Int) -> Unit,
    onLogout: () -> Unit,
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
    var showKidsModeGuide by remember { mutableStateOf(false) }

    if (showKidsModeGuide) {
        KidsModeGuideDialog(
            onDismiss = { showKidsModeGuide = false },
            isAndroid = isAndroidPlatform()
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Children & Custom Words Section
        when (childrenState) {
            is ChildrenState.Loading -> item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) { LoadingContent() }
            }

            is ChildrenState.Error -> item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorContent(
                        message = childrenState.message,
                        onRetry = onRetry
                    )
                }
            }

            is ChildrenState.Success -> {
                // Children section header
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 8.dp, bottom = 12.dp)
                    ) {
                        Text(
                            "فرزندان شما",
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkText
                        )

                        if (canAddChild) {
                            Text(
                                "+  افزودن فرزند",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TealPurple,
                                modifier = Modifier.clickable(onClick = onAddChildClick)
                            )
                        }
                    }
                }

                if (childrenState.children.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                            EmptyChildrenContent(
                                onAddChildClick = onAddChildClick,
                                canAddChild = canAddChild
                            )
                        }
                    }
                } else {
                    items(childrenState.children.size) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 6.dp)
                        ) {
                            ChildContent(
                                child = childrenState.children[index],
                                onClick = { onChildClick(childrenState.children[index]) }
                            )
                        }
                    }
                }

                // Custom words section header
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 20.dp, bottom = 12.dp)
                    ) {
                        Text(
                            "مدیریت کلمات",
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkText
                        )

                        Text(
                            "+  افزودن کلمه",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TealPurple,
                            modifier = Modifier.clickable(onClick = onAddWordClick)
                        )
                    }
                }

                when (customWordsState) {
                    is CustomWordsState.Error -> item {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                            ErrorContent(
                                message = customWordsState.message,
                                onRetry = onRetry
                            )
                        }
                    }

                    is CustomWordsState.Success -> {
                        items(customWordsState.words.size) { index ->
                            val word = customWordsState.words[index]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 6.dp)
                            ) {
                                CustomWordContent(
                                    word = word,
                                    isPlaying = currentPlayingAudioUrl == word.audioUrl,
                                    onPlayClick = { audioUrl -> onPlayAudio(audioUrl) },
                                    onStopClick = onStopAudio,
                                    onDeleteClick = { wordId -> onDeleteCustomWord(wordId) },
                                    onClick = {}
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }

            else -> {}
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // User Info Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
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
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Timer Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
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
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Kids Mode Guide Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
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
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Logout Button
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
            ) {
                Text(
                    text = "خروج از حساب کاربری",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
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