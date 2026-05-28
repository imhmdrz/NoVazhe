package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
import mohaamadreza.saemipour.no.vazheh.ui.components.AddWordHeader
import mohaamadreza.saemipour.no.vazheh.ui.components.AudioRecordingSection
import mohaamadreza.saemipour.no.vazheh.ui.components.ImageSelectionSection
import mohaamadreza.saemipour.no.vazheh.ui.components.InputSection
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.MotherViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CreateCategoryState
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

    // Category selection state
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    // Make sure categories are loaded when this screen opens so the picker is populated
    LaunchedEffect(Unit) {
        if (uiState.categories.isEmpty() && !uiState.isLoadingCategories) {
            viewModel.loadCategories()
        }
    }

    // When a new category is created successfully, pre-select it and dismiss the dialog
    LaunchedEffect(uiState.createCategoryState) {
        when (val state = uiState.createCategoryState) {
            is CreateCategoryState.Success -> {
                selectedCategoryId = state.category.id
                showCreateCategoryDialog = false
                viewModel.clearCreateCategoryError()
            }
            else -> {}
        }
    }

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
                                    categoryId = selectedCategoryId
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

                        Spacer(modifier = Modifier.height(24.dp))

                        // Category picker
                        CategoryPickerSection(
                            categories = uiState.categories,
                            isLoading = uiState.isLoadingCategories,
                            selectedCategoryId = selectedCategoryId,
                            enabled = !uiState.isCreatingCustomWord,
                            onCategorySelected = { id ->
                                selectedCategoryId = if (selectedCategoryId == id) null else id
                            },
                            onAddCategoryClick = { showCreateCategoryDialog = true }
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

            if (showCreateCategoryDialog) {
                CreateCategoryDialog(
                    isLoading = uiState.isCreatingCategory,
                    errorMessage = uiState.createCategoryErrorMessage,
                    onDismiss = {
                        showCreateCategoryDialog = false
                        viewModel.clearCreateCategoryError()
                    },
                    onCreate = { nameFa, nameEn ->
                        viewModel.createCategory(
                            nameFa = nameFa,
                            nameEn = nameEn.ifBlank { null }
                        )
                    }
                )
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

/**
 * Horizontal category picker shown above the image/audio sections. Lets the
 * mother either select an existing category or open a dialog to create a new
 * one and add the word to it immediately.
 * انتخاب دسته‌بندی برای کلمه جدید (انتخاب از موجود یا ساخت دسته‌بندی جدید)
 */
@Composable
private fun CategoryPickerSection(
    categories: List<CategoryDTO>,
    isLoading: Boolean,
    selectedCategoryId: Int?,
    enabled: Boolean,
    onCategorySelected: (Int) -> Unit,
    onAddCategoryClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "دسته‌بندی",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "(اختیاری)",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "یک دسته‌بندی انتخاب کن یا دسته‌بندی جدید بساز",
            style = MaterialTheme.typography.bodySmall,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                AddCategoryChip(enabled = enabled, onClick = onAddCategoryClick)
            }

            if (isLoading && categories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .size(width = 110.dp, height = 44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = TealPurple
                        )
                    }
                }
            }

            items(categories.size) { index ->
                val category = categories[index]
                CategoryChip(
                    category = category,
                    selected = selectedCategoryId == category.id,
                    enabled = enabled,
                    onClick = { onCategorySelected(category.id) }
                )
            }
        }
    }
}

@Composable
private fun AddCategoryChip(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(44.dp)
            .background(
                color = TealPurple.copy(alpha = 0.08f),
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                width = 1.5.dp,
                color = TealPurple,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = TealPurple,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "دسته‌بندی جدید",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TealPurple
        )
    }
}

@Composable
private fun CategoryChip(
    category: CategoryDTO,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) TealPurple else Color.White
    val contentColor = if (selected) Color.White else DarkText
    val borderColor = if (selected) TealPurple else SoftGray

    Row(
        modifier = Modifier
            .height(44.dp)
            .background(color = bg, shape = RoundedCornerShape(22.dp))
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(22.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = category.nameFa,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

/**
 * Inline dialog for creating a new category from the add-word screen.
 * Once the category is created it is auto-selected as the destination for the
 * word currently being added.
 * دیالوگ ساخت دسته‌بندی جدید
 */
@Composable
private fun CreateCategoryDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreate: (nameFa: String, nameEn: String) -> Unit
) {
    var nameFa by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "دسته‌بندی جدید",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "یک نام فارسی برای دسته‌بندی جدید وارد کن",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nameFa,
                    onValueChange = { nameFa = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "مثال: میوه‌ها", color = MutedText) },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    enabled = !isLoading,
                    isError = errorMessage != null && nameFa.isBlank(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPurple,
                        unfocusedBorderColor = SoftGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        errorBorderColor = CoralRed
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "نام انگلیسی (اختیاری)", color = MutedText) },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPurple,
                        unfocusedBorderColor = SoftGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = CoralRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = CoralRed.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralRed
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(nameFa.trim(), nameEn.trim()) },
                enabled = !isLoading && nameFa.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPurple)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "افزودن",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(text = "انصراف", color = MutedText)
            }
        }
    )
}