package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.ismoy.imagepickerkmp.domain.models.GalleryPhotoResult
import io.github.ismoy.imagepickerkmp.presentation.ui.components.GalleryPickerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
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
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.MotherViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CreateCategoryState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CreateCustomWordState

/** Which step of the add-word flow is currently shown. */
private enum class AddWordStep { Categories, Words, NewWord }

/**
 * A row in the word list, unifying a category's base/curated words with the
 * parent's own added (custom) words. Only custom words can be deleted.
 */
private data class WordRowModel(
    val id: Int,
    val wordFa: String,
    val imageUrl: String?,
    val isCustom: Boolean
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddWordScreen(
    navController: NavController,
    viewModel: MotherViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ===== In-screen navigation: categories -> words -> new word =====
    var selectedCategory by remember { mutableStateOf<CategoryDTO?>(null) }
    var showNewWordForm by remember { mutableStateOf(false) }
    val step = when {
        selectedCategory == null -> AddWordStep.Categories
        showNewWordForm -> AddWordStep.NewWord
        else -> AddWordStep.Words
    }

    // ===== New-word form state =====
    var wordFa by remember { mutableStateOf("") }
    var audioUrl by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<GalleryPhotoResult?>(null) }
    var showWordGallery by remember { mutableStateOf(false) }

    // ===== Create-category dialog state =====
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var categoryImageUrl by remember { mutableStateOf("") }
    var showCategoryGallery by remember { mutableStateOf(false) }

    // Make sure categories are loaded when this screen opens so the list is populated
    LaunchedEffect(Unit) {
        if (uiState.categories.isEmpty() && !uiState.isLoadingCategories) {
            viewModel.loadCategories()
        }
    }

    // Load the opened category's words whenever the selection changes
    LaunchedEffect(selectedCategory?.id) {
        selectedCategory?.let { viewModel.loadCategoryWords(it.id) }
    }

    // When a new category is created, close the dialog and reset its image
    LaunchedEffect(uiState.createCategoryState) {
        if (uiState.createCategoryState is CreateCategoryState.Success) {
            showCreateCategoryDialog = false
            categoryImageUrl = ""
            viewModel.clearCreateCategoryError()
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

            override fun onReady() {}

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
                if (recordingFilePath.isNotEmpty() && audioUrl.isEmpty()) {
                    deleteFile(recordingFilePath)
                }
            }
        }

        fun startRecording() {
            if (!microphonePermissionGranted) return
            if (playerState.isPlaying) audioPlayer.pause()
            val cacheDir = getCacheDirectory()
            val fileName = "recording_${currentTimeMillis()}.m4a"
            recordingFilePath = "$cacheDir/$fileName"
            recorderManager.start(recordingFilePath)
            isRecording = true
            hasRecording = false
            audioUrl = ""
        }

        fun stopRecording() {
            recorderManager.stop()
            isRecording = false
            hasRecording = true
            isProcessingAudio = true

            scope.launch {
                val dataUrl = withContext(Dispatchers.IO) {
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

        fun playRecording() {
            if (recordingFilePath.isNotEmpty()) audioPlayer.play(recordingFilePath)
        }

        fun stopPlayback() {
            audioPlayer.pause()
        }

        fun deleteRecording() {
            if (playerState.isPlaying) audioPlayer.pause()
            if (recordingFilePath.isNotEmpty()) deleteFile(recordingFilePath)
            recordingFilePath = ""
            hasRecording = false
            audioUrl = ""
            playerState = PlayerState()
        }

        // Reset the whole new-word form back to empty (used after success / on leaving it)
        fun resetWordForm() {
            if (playerState.isPlaying) audioPlayer.pause()
            if (recordingFilePath.isNotEmpty() && audioUrl.isEmpty()) deleteFile(recordingFilePath)
            wordFa = ""
            audioUrl = ""
            imageUrl = ""
            selectedImage = null
            isRecording = false
            hasRecording = false
            recordingFilePath = ""
            isProcessingAudio = false
            playerState = PlayerState()
        }

        // After a word is added: notify, reset, return to the category's word list, reload
        LaunchedEffect(uiState.createCustomWordState) {
            if (uiState.createCustomWordState is CreateCustomWordState.Success) {
                snackbarHostState.showSnackbar("کلمه با موفقیت اضافه شد")
                viewModel.clearCreateCustomWordError()
                resetWordForm()
                showNewWordForm = false
                selectedCategory?.let { viewModel.loadCategoryWords(it.id) }
                viewModel.loadCustomWords()
            }
        }

        fun handleBack() {
            when (step) {
                AddWordStep.NewWord -> {
                    resetWordForm()
                    showNewWordForm = false
                }
                AddWordStep.Words -> {
                    selectedCategory = null
                    viewModel.clearCategoryWords()
                }
                AddWordStep.Categories -> navController.popBackStack()
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            BackHandler(enabled = step != AddWordStep.Categories) { handleBack() }

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
                    AddWordHeader { handleBack() }
                },
                bottomBar = {
                    // Submit button only on the new-word form
                    if (step == AddWordStep.NewWord) {
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
                                        categoryId = selectedCategory?.id
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPurple),
                                enabled = wordFa.isNotBlank() && audioUrl.isNotBlank() &&
                                    !uiState.isCreatingCustomWord && !isRecording && !isProcessingAudio
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
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(paddingValues)
                ) {
                    when (step) {
                        AddWordStep.Categories -> CategoryListContent(
                            categories = uiState.categories,
                            isLoading = uiState.isLoadingCategories,
                            onAddCategory = { showCreateCategoryDialog = true },
                            onCategoryClick = { selectedCategory = it }
                        )

                        AddWordStep.Words -> {
                            val category = selectedCategory
                            if (category != null) {
                                val customForCategory = uiState.customWords
                                    .filter { it.categoryId == category.id }
                                    .map { WordRowModel(it.id, it.wordFa, it.imageUrl, isCustom = true) }
                                val baseWords = uiState.categoryWords
                                    .map { WordRowModel(it.id, it.wordFa, it.imageUrl, isCustom = false) }

                                WordListContent(
                                    category = category,
                                    words = baseWords + customForCategory,
                                    isLoading = uiState.isLoadingCategoryWords,
                                    onAddWord = { showNewWordForm = true },
                                    onDeleteCustomWord = { viewModel.deleteCustomWord(it) }
                                )
                            }
                        }

                        AddWordStep.NewWord -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 24.dp)
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "افزودن کلمه به «${selectedCategory?.nameFa ?: ""}»",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                InputSection(
                                    value = wordFa,
                                    onValueChange = { wordFa = it },
                                    enabled = !uiState.isCreatingCustomWord,
                                    isError = uiState.createCustomWordErrorMessage != null && wordFa.isBlank()
                                )

                                if (showWordGallery) {
                                    GalleryPickerLauncher(
                                        onPhotosSelected = { photos ->
                                            if (photos.isNotEmpty()) {
                                                selectedImage = photos.first()
                                                val uri = photos.first().uri
                                                val mime = photos.first().mimeType
                                                scope.launch {
                                                    val (dataUrl, _) = withContext(Dispatchers.IO) {
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
                                            showWordGallery = false
                                        },
                                        onError = {
                                            scope.launch { snackbarHostState.showSnackbar("خطا در انتخاب تصویر") }
                                            showWordGallery = false
                                        },
                                        onDismiss = { showWordGallery = false },
                                        allowMultiple = false
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                ImageSelectionSection(
                                    selectedImage = selectedImage,
                                    onSelectImage = { showWordGallery = true },
                                    onDeleteImage = {
                                        selectedImage = null
                                        imageUrl = ""
                                    },
                                    enabled = !uiState.isCreatingCustomWord
                                )

                                Spacer(modifier = Modifier.height(24.dp))

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

                                if (uiState.createCustomWordErrorMessage != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
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

            // Gallery launcher for the category photo (used by the create-category dialog)
            if (showCategoryGallery) {
                GalleryPickerLauncher(
                    onPhotosSelected = { photos ->
                        if (photos.isNotEmpty()) {
                            val uri = photos.first().uri
                            val mime = photos.first().mimeType
                            scope.launch {
                                val (dataUrl, _) = withContext(Dispatchers.IO) {
                                    imageUriToDataUrl(uri, mime ?: "image/jpeg") ?: (null to null)
                                }
                                if (dataUrl != null) {
                                    categoryImageUrl = dataUrl
                                } else {
                                    snackbarHostState.showSnackbar("خطا در پردازش تصویر")
                                }
                            }
                        }
                        showCategoryGallery = false
                    },
                    onError = {
                        scope.launch { snackbarHostState.showSnackbar("خطا در انتخاب تصویر") }
                        showCategoryGallery = false
                    },
                    onDismiss = { showCategoryGallery = false },
                    allowMultiple = false
                )
            }

            if (showCreateCategoryDialog) {
                CreateCategoryDialog(
                    isLoading = uiState.isCreatingCategory,
                    errorMessage = uiState.createCategoryErrorMessage,
                    imageUrl = categoryImageUrl,
                    onPickImage = { showCategoryGallery = true },
                    onClearImage = { categoryImageUrl = "" },
                    onDismiss = {
                        showCreateCategoryDialog = false
                        categoryImageUrl = ""
                        viewModel.clearCreateCategoryError()
                    },
                    onCreate = { nameFa, nameEn ->
                        viewModel.createCategory(
                            nameFa = nameFa,
                            nameEn = nameEn.ifBlank { null },
                            iconUrl = categoryImageUrl.ifBlank { null }
                        )
                    }
                )
            }
        }
    }
}

// ==================== Category list (step 1) ====================

@Composable
private fun CategoryListContent(
    categories: List<CategoryDTO>,
    isLoading: Boolean,
    onAddCategory: () -> Unit,
    onCategoryClick: (CategoryDTO) -> Unit
) {
    if (isLoading && categories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TealPurple)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AddTile(label = "دسته‌بندی جدید", onClick = onAddCategory)
        }
        gridItems(categories) { category ->
            CategoryTile(category = category, onClick = { onCategoryClick(category) })
        }
    }
}

@Composable
private fun AddTile(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TealPurple.copy(alpha = 0.08f))
            .border(
                width = 1.5.dp,
                color = TealPurple,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = TealPurple,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TealPurple
            )
        }
    }
}

@Composable
private fun CategoryTile(
    category: CategoryDTO,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SoftGray)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(category.iconUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Scrim for label legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
        )

        Text(
            text = category.nameFa,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
    }
}

// ==================== Word list (step 2) ====================

@Composable
private fun WordListContent(
    category: CategoryDTO,
    words: List<WordRowModel>,
    isLoading: Boolean,
    onAddWord: () -> Unit,
    onDeleteCustomWord: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = category.nameFa,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            AddWordRow(onClick = onAddWord)
        }

        if (isLoading && words.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TealPurple)
                }
            }
        } else if (words.isEmpty()) {
            item {
                Text(
                    text = "هنوز کلمه‌ای در این دسته‌بندی نیست",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            listItems(words, key = { "${it.isCustom}-${it.id}" }) { word ->
                WordRow(
                    word = word,
                    onDelete = if (word.isCustom) ({ onDeleteCustomWord(word.id) }) else null
                )
            }
        }
    }
}

@Composable
private fun AddWordRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(TealPurple.copy(alpha = 0.08f))
            .border(width = 1.5.dp, color = TealPurple, shape = RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = TealPurple,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "افزودن کلمه جدید",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TealPurple
        )
    }
}

@Composable
private fun WordRow(
    word: WordRowModel,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SoftGray),
                contentAlignment = Alignment.Center
            ) {
                if (!word.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(word.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = "🔤")
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = word.wordFa,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = DarkText,
                modifier = Modifier.weight(1f)
            )

            if (word.isCustom) {
                Text(
                    text = "افزوده‌ی شما",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealPurple
                )
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = CoralRed
                    )
                }
            }
        }
    }
}

// ==================== Shared bits ====================

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
 * Inline dialog for creating a new category with an optional photo.
 * دیالوگ ساخت دسته‌بندی جدید با عکس اختیاری
 */
@Composable
private fun CreateCategoryDialog(
    isLoading: Boolean,
    errorMessage: String?,
    imageUrl: String,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
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
                    text = "یک نام فارسی و (در صورت تمایل) یک عکس برای دسته‌بندی انتخاب کن",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category photo picker (optional)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftGray)
                        .border(1.5.dp, SoftGray, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isLoading, onClick = onPickImage),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Small clear button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable(enabled = !isLoading, onClick = onClearImage),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "حذف عکس",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = TealPurple,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "عکس",
                                style = MaterialTheme.typography.labelSmall,
                                color = TealPurple
                            )
                        }
                    }
                }

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
