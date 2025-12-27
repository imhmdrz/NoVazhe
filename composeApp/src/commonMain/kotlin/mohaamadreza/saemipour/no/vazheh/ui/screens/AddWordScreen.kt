package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mohaamadreza.saemipour.no.vazheh.permission.PermissionCallback
import mohaamadreza.saemipour.no.vazheh.permission.PermissionStatus
import mohaamadreza.saemipour.no.vazheh.permission.PermissionType
import mohaamadreza.saemipour.no.vazheh.permission.createPermissionsManager
import mohaamadreza.saemipour.no.vazheh.player.AudioPlayer
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.recorder.audioFileToDataUrl
import mohaamadreza.saemipour.no.vazheh.recorder.currentTimeMillis
import mohaamadreza.saemipour.no.vazheh.recorder.deleteFile
import mohaamadreza.saemipour.no.vazheh.recorder.getCacheDirectory
import mohaamadreza.saemipour.no.vazheh.recorder.rememberRecorderManager
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealLight
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.MotherViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CreateCustomWordState

@Composable
fun AddWordScreen(
    navController: NavController,
    viewModel: MotherViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Form state
    var wordFa by remember { mutableStateOf("") }
    var audioUrl by remember { mutableStateOf("") }

    // Recording state
    var isRecording by remember { mutableStateOf(false) }
    var hasRecording by remember { mutableStateOf(false) }
    var recordingFilePath by remember { mutableStateOf("") }
    var isProcessingAudio by remember { mutableStateOf(false) }
    var microphonePermissionGranted by remember { mutableStateOf(false) }
    var showPermissionDenied by remember { mutableStateOf(false) }

    // Permission action triggers (to call composable functions from click handlers)
    var shouldRequestPermission by remember { mutableStateOf(false) }
    var shouldOpenSettings by remember { mutableStateOf(false) }

    // Recorder manager
    val recorderManager = rememberRecorderManager()

    // Audio player state
    var playerState by remember { mutableStateOf(PlayerState()) }

    // Permission manager
    val permissionsManager = createPermissionsManager(
        callback = object : PermissionCallback {
            override fun onPermissionStatus(
                permissionType: PermissionType,
                status: PermissionStatus
            ) {
                when (status) {
                    PermissionStatus.GRANTED -> {
                        microphonePermissionGranted = true
                        showPermissionDenied = false
                        shouldRequestPermission = false
                    }

                    PermissionStatus.DENIED -> {
                        showPermissionDenied = true
                        microphonePermissionGranted = false
                        shouldRequestPermission = false
                    }

                    PermissionStatus.SHOW_RATIONALE -> {
                        showPermissionDenied = true
                        microphonePermissionGranted = false
                        shouldRequestPermission = false
                    }
                }
            }
        }
    )

    // Check initial permission status
    val isMicPermissionGranted = permissionsManager.isPermissionGranted(PermissionType.MICROPHONE)
    LaunchedEffect(isMicPermissionGranted) {
        microphonePermissionGranted = isMicPermissionGranted
    }

    // Handle permission request (composable context)
    if (shouldRequestPermission) {
        permissionsManager.askPermission(PermissionType.MICROPHONE)
    }

    // Handle open settings (composable context)
    if (shouldOpenSettings) {
        permissionsManager.launchSettings()
        shouldOpenSettings = false
    }

    // Show success message and navigate back
    LaunchedEffect(uiState.createCustomWordState) {
        when (uiState.createCustomWordState) {
            is CreateCustomWordState.Success -> {
                snackbarHostState.showSnackbar("کلمه با موفقیت اضافه شد")
                viewModel.clearCreateCustomWordError()
                // Clean up temp file
                if (recordingFilePath.isNotEmpty()) {
                    deleteFile(recordingFilePath)
                }
                navController.popBackStack()
            }

            else -> {}
        }
    }

    // Clear error when form changes
    LaunchedEffect(wordFa, audioUrl) {
        if (uiState.createCustomWordErrorMessage != null) {
            viewModel.clearCreateCustomWordError()
        }
    }

    // Audio updates callback
    val audioUpdates = remember {
        object : AudioUpdates {
            override fun onProgressUpdate(state: PlayerState) {
                playerState = state
            }

            override fun onReady() {
                // Audio is ready to play
            }

            override fun onError(exception: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("خطا در پخش صدا: ${exception.message}")
                }
                playerState = playerState.copy(isPlaying = false)
            }
        }
    }

    AudioProvider(audioUpdates = audioUpdates) { audioPlayer ->

        // Cleanup on dispose
        DisposableEffect(Unit) {
            onDispose {
                if (isRecording) {
                    recorderManager.stop()
                }
                audioPlayer.cleanUp()
                // Clean up temp file if not used
                if (recordingFilePath.isNotEmpty() && audioUrl.isEmpty()) {
                    deleteFile(recordingFilePath)
                }
            }
        }

        // Function to start recording
        fun startRecording() {
            if (!microphonePermissionGranted) {
                return
            }
            // Stop playback if playing
            if (playerState.isPlaying) {
                audioPlayer.pause()
            }
            val cacheDir = getCacheDirectory()
            val fileName = "recording_${currentTimeMillis()}.m4a"
            recordingFilePath = "$cacheDir/$fileName"
            recorderManager.start(recordingFilePath)
            isRecording = true
            hasRecording = false
            audioUrl = ""
        }

        // Function to stop recording
        fun stopRecording() {
            recorderManager.stop()
            isRecording = false
            hasRecording = true
            isProcessingAudio = true

            scope.launch {
                val dataUrl = withContext(Dispatchers.IO) {
                    // Small delay to ensure file is fully written
                    delay(200)
                    audioFileToDataUrl(recordingFilePath)
                }
                isProcessingAudio = false
                if (dataUrl != null) {
                    audioUrl = dataUrl
                } else {
                    snackbarHostState.showSnackbar("خطا در پردازش فایل صوتی")
                    hasRecording = false
                }
            }
        }

        // Function to play recording
        fun playRecording() {
            if (recordingFilePath.isNotEmpty()) {
                audioPlayer.play(recordingFilePath)
            }
        }

        // Function to stop/pause playback
        fun stopPlayback() {
            audioPlayer.pause()
        }

        // Function to delete recording
        fun deleteRecording() {
            // Stop playback first
            if (playerState.isPlaying) {
                audioPlayer.pause()
            }
            audioPlayer.cleanUp()
            if (recordingFilePath.isNotEmpty()) {
                deleteFile(recordingFilePath)
            }
            recordingFilePath = ""
            hasRecording = false
            audioUrl = ""
            playerState = PlayerState()
        }

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
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SoftGray)
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    AddWordHeader(
                        onBackClick = { navController.popBackStack() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Form content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Persian word input
                        InputSection(
                            title = "کلمه فارسی",
                            subtitle = "(الزامی)",
                            value = wordFa,
                            onValueChange = { wordFa = it },
                            placeholder = "مثال: سیب",
                            enabled = !uiState.isCreatingCustomWord,
                            isError = uiState.createCustomWordErrorMessage != null && wordFa.isBlank()
                        )

                        // Audio Recording Section
                        AudioRecordingSection(
                            isRecording = isRecording,
                            hasRecording = hasRecording,
                            isProcessingAudio = isProcessingAudio,
                            isPlaying = playerState.isPlaying,
                            microphonePermissionGranted = microphonePermissionGranted,
                            showPermissionDenied = showPermissionDenied,
                            onRequestPermission = { shouldRequestPermission = true },
                            onStartRecording = { startRecording() },
                            onStopRecording = { stopRecording() },
                            onDeleteRecording = { deleteRecording() },
                            onPlayRecording = { playRecording() },
                            onStopPlayback = { stopPlayback() },
                            onOpenSettings = { shouldOpenSettings = true },
                            enabled = !uiState.isCreatingCustomWord
                        )

                        // Error message
                        if (uiState.createCustomWordErrorMessage != null) {
                            ErrorCard(
                                message = uiState.createCustomWordErrorMessage!!,
                                onDismiss = { viewModel.clearCreateCustomWordError() }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Submit button
                        Button(
                            onClick = {
                                viewModel.createCustomWord(
                                    wordFa = wordFa.trim(),
                                    wordEn = null,
                                    audioUrl = audioUrl.trim(),
                                    imageUrl = null,
                                    categoryId = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealPurple
                            ),
                            enabled = wordFa.isNotBlank() && audioUrl.isNotBlank() && !uiState.isCreatingCustomWord && !isRecording && !isProcessingAudio
                        ) {
                            if (uiState.isCreatingCustomWord) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "افزودن کلمه",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddWordHeader(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(TealPurple, TealLight)
                )
            )
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBackClick)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "بازگشت",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "بازگشت",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "افزودن کلمه جدید",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "کلمه سفارشی خود را اضافه کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun InputSection(
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MutedText
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) CoralRed else TealPurple,
                unfocusedBorderColor = if (isError) CoralRed else SoftGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorBorderColor = CoralRed
            ),
            singleLine = true,
            enabled = enabled,
            isError = isError
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoralRed.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(CoralRed.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CoralRed
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = CoralRed,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "بستن",
                    tint = CoralRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioRecordingSection(
    isRecording: Boolean,
    hasRecording: Boolean,
    isProcessingAudio: Boolean,
    isPlaying: Boolean,
    microphonePermissionGranted: Boolean,
    showPermissionDenied: Boolean,
    onRequestPermission: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onDeleteRecording: () -> Unit,
    onPlayRecording: () -> Unit,
    onStopPlayback: () -> Unit,
    onOpenSettings: () -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ضبط صدا",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "(الزامی)",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    // Permission denied state
                    showPermissionDenied && !microphonePermissionGranted -> {
                        PermissionDeniedContent(
                            onOpenSettings = onOpenSettings
                        )
                    }
                    // Need permission
                    !microphonePermissionGranted -> {
                        RequestPermissionContent(
                            onRequestPermission = onRequestPermission
                        )
                    }
                    // Processing audio
                    isProcessingAudio -> {
                        ProcessingAudioContent()
                    }
                    // Has recording
                    hasRecording -> {
                        RecordingCompleteContent(
                            isPlaying = isPlaying,
                            onPlayRecording = onPlayRecording,
                            onStopPlayback = onStopPlayback,
                            onDeleteRecording = onDeleteRecording,
                            enabled = enabled
                        )
                    }
                    // Recording in progress
                    isRecording -> {
                        RecordingInProgressContent(
                            onStopRecording = onStopRecording
                        )
                    }
                    // Ready to record
                    else -> {
                        ReadyToRecordContent(
                            onStartRecording = onStartRecording,
                            enabled = enabled
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestPermissionContent(
    onRequestPermission: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(TealPurple.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎤",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "برای ضبط صدا، دسترسی به میکروفون نیاز است",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TealPurple
            )
        ) {
            Text(
                text = "اجازه دسترسی",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onOpenSettings: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(CoralRed.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🚫",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "دسترسی به میکروفون رد شده است",
            style = MaterialTheme.typography.bodyMedium,
            color = CoralRed
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "لطفاً از تنظیمات، دسترسی را فعال کنید",
            style = MaterialTheme.typography.bodySmall,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenSettings,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TealPurple
            )
        ) {
            Text(
                text = "باز کردن تنظیمات",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReadyToRecordContent(
    onStartRecording: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    if (enabled) TealPurple else SoftGray,
                    CircleShape
                )
                .clickable(enabled = enabled, onClick = onStartRecording),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎙️",
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "برای شروع ضبط، روی دکمه بالا بزنید",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText
        )
    }
}

@Composable
private fun RecordingInProgressContent(
    onStopRecording: () -> Unit
) {
    // Pulsating animation
    val scale by animateFloatAsState(
        targetValue = 1.1f,
        animationSpec = tween(500),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .background(CoralRed, CircleShape)
                .clickable(onClick = onStopRecording),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(CoralRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "در حال ضبط... برای توقف بزنید",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = CoralRed
            )
        }
    }
}

@Composable
private fun ProcessingAudioContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = TealPurple,
            strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "در حال پردازش صدا...",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText
        )
    }
}

@Composable
private fun RecordingCompleteContent(
    isPlaying: Boolean,
    onPlayRecording: () -> Unit,
    onStopPlayback: () -> Unit,
    onDeleteRecording: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Play/Stop button
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    if (isPlaying) CoralRed else TealPurple,
                    CircleShape
                )
                .clickable(
                    enabled = enabled,
                    onClick = { if (isPlaying) onStopPlayback() else onPlayRecording() }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) {
                // Stop icon (square)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                )
            } else {
                // Play icon (triangle using text)
                Text(
                    text = "▶",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isPlaying) "در حال پخش..." else "صدا با موفقیت ضبط شد",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isPlaying) CoralRed else TealPurple
        )

        if (!isPlaying) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "برای گوش دادن، روی دکمه بالا بزنید",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delete button
        Button(
            onClick = onDeleteRecording,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CoralRed.copy(alpha = 0.1f),
                contentColor = CoralRed
            ),
            enabled = enabled && !isPlaying
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "حذف و ضبط مجدد",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
