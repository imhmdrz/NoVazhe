package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.ismoy.imagepickerkmp.domain.models.GalleryPhotoResult
import io.github.ismoy.imagepickerkmp.presentation.ui.components.GalleryPickerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mohaamadreza.saemipour.no.vazheh.permission.PermissionCallback
import mohaamadreza.saemipour.no.vazheh.permission.PermissionStatus
import mohaamadreza.saemipour.no.vazheh.permission.PermissionType
import mohaamadreza.saemipour.no.vazheh.permission.createPermissionsManager
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.recorder.audioFileToDataUrl
import mohaamadreza.saemipour.no.vazheh.recorder.currentTimeMillis
import mohaamadreza.saemipour.no.vazheh.recorder.deleteFile
import mohaamadreza.saemipour.no.vazheh.recorder.getCacheDirectory
import mohaamadreza.saemipour.no.vazheh.recorder.imageUriToDataUrl
import mohaamadreza.saemipour.no.vazheh.recorder.rememberRecorderManager
import mohaamadreza.saemipour.no.vazheh.ui.components.AddWordHeader
import mohaamadreza.saemipour.no.vazheh.ui.components.AudioRecordingSection
import mohaamadreza.saemipour.no.vazheh.ui.components.ImageSelectionSection
import mohaamadreza.saemipour.no.vazheh.ui.components.InputSection
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
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
    var imageUrl by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<GalleryPhotoResult?>(null) }
    var showGallery by remember { mutableStateOf(false) }

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
            // Delete the file
            if (recordingFilePath.isNotEmpty()) {
                deleteFile(recordingFilePath)
            }
            // Reset state
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
                },
                topBar = {
                    AddWordHeader { navController.popBackStack() }
                },
                bottomBar = {
                    // Fixed bottom button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.createCustomWord(
                                    wordFa = wordFa.trim(),
                                    wordEn = null,
                                    audioUrl = audioUrl.trim(),
                                    imageUrl = imageUrl.ifBlank { null },
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
                                Text(
                                    text = "+ افزودن کلمه",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Form content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    ) {
                        // Persian word input
                        InputSection(
                            value = wordFa,
                            onValueChange = { wordFa = it },
                            enabled = !uiState.isCreatingCustomWord,
                            isError = uiState.createCustomWordErrorMessage != null && wordFa.isBlank()
                        )

                        // Gallery Picker Launcher
                        if (showGallery) {
                            GalleryPickerLauncher(
                                onPhotosSelected = { photos ->
                                    if (photos.isNotEmpty()) {
                                        selectedImage = photos.first()
                                        val uri = photos.first().uri
                                        val mime = photos.first().mimeType
                                        // Convert URI to data URL in background
                                        scope.launch {
                                            val (dataUrl, dataByte) = withContext(Dispatchers.IO) {
                                                imageUriToDataUrl(uri, mime ?: "image/jpeg") ?: (null to null)
                                            }
                                            if (dataUrl != null) {
                                                imageUrl = dataUrl
                                            } else {
                                                snackbarHostState.showSnackbar("خطا در پردازش تصویر")
                                                selectedImage = null
                                            }
                                        }
                                    }
                                    showGallery = false
                                },
                                onError = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("خطا در انتخاب تصویر")
                                    }
                                    showGallery = false
                                },
                                onDismiss = { showGallery = false },
                                allowMultiple = false
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Image Selection Section
                        ImageSelectionSection(
                            selectedImage = selectedImage,
                            onSelectImage = { showGallery = true },
                            onDeleteImage = {
                                selectedImage = null
                                imageUrl = ""
                            },
                            enabled = !uiState.isCreatingCustomWord
                        )

                        Spacer(modifier = Modifier.height(24.dp))

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

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
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