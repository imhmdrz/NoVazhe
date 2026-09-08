package mohaamadreza.saemipour.no.vazheh.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.AppLogger
import mohaamadreza.saemipour.no.vazheh.data.AuthRepository
import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.data.ChildRepository
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.CreateCategoryRequest
import mohaamadreza.saemipour.no.vazheh.data.CreateChildRequest
import mohaamadreza.saemipour.no.vazheh.data.CreateCustomWordRequest
import mohaamadreza.saemipour.no.vazheh.data.Gender
import mohaamadreza.saemipour.no.vazheh.data.UpdateChildRequest
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CategoriesState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CategoryWordsState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.ChildrenState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CreateCategoryState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CreateChildState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CreateCustomWordState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CustomWordsState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.DeleteChildState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.DeleteCustomWordState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.MotherTab
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.MotherUiState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.UpdateChildState

class MotherViewModel(
    private val authRepository: AuthRepository,
    private val childRepository: ChildRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotherUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    /**
     * Re-read the login state and (re)load data accordingly. Categories are public
     * (needed for the games), but children & custom words require a logged-in parent,
     * so they're only loaded when authenticated. Called on init, on re-entering the
     * screen, and after login/logout so the Profile tab locks/unlocks correctly.
     */
    fun refresh() {
        val loggedIn = authRepository.isLoggedIn()
        _uiState.update {
            it.copy(
                isLoggedIn = loggedIn,
                settingsUnlocked = if (loggedIn) it.settingsUnlocked else false,
                showSettingsPasswordPrompt = if (loggedIn) it.showSettingsPasswordPrompt else false,
                isVerifyingSettingsPassword =
                    if (loggedIn) it.isVerifyingSettingsPassword else false,
                settingsPasswordError = if (loggedIn) it.settingsPasswordError else null
            )
        }
        loadUserInfo()
        loadCategories()
        if (loggedIn) {
            loadChildren()
            loadCustomWords()
        } else {
            // Drop any parent-only data so a guest never sees a previous session's children/words.
            _uiState.update {
                it.copy(
                    childrenState = ChildrenState.Idle,
                    customWordsState = CustomWordsState.Idle,
                    selectedChild = null
                )
            }
        }
    }

    fun loadUserInfo() {
        val username = authRepository.getUsername() ?: ""
        val displayName = authRepository.getDisplayName() ?: ""
        _uiState.update {
            it.copy(
                username = username,
                displayName = displayName
            )
        }
    }

    fun onTabSelected(tab: MotherTab) {
        val currentState = _uiState.value

        if (tab == MotherTab.PROFILE && currentState.isLoggedIn && !currentState.settingsUnlocked) {
            requestSettingsAccess()
            return
        }

        _uiState.update {
            it.copy(
                selectedTab = tab,
                settingsUnlocked =
                    if (it.selectedTab == MotherTab.PROFILE && tab != MotherTab.PROFILE) false
                    else it.settingsUnlocked
            )
        }
    }

    fun requestSettingsAccess() {
        if (!_uiState.value.isLoggedIn) return

        _uiState.update {
            it.copy(
                showSettingsPasswordPrompt = true,
                settingsPasswordError = null,
                isVerifyingSettingsPassword = false
            )
        }
    }

    fun verifySettingsPassword(password: String) {
        _uiState.update {
            it.copy(
                isVerifyingSettingsPassword = true,
                settingsPasswordError = null
            )
        }

        viewModelScope.launch {
            authRepository.verifyPassword(password).fold(
                onSuccess = { verified ->
                    if (verified) {
                        _uiState.update {
                            it.copy(
                                settingsUnlocked = true,
                                showSettingsPasswordPrompt = false,
                                isVerifyingSettingsPassword = false,
                                settingsPasswordError = null,
                                selectedTab = MotherTab.PROFILE
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                settingsUnlocked = false,
                                showSettingsPasswordPrompt = true,
                                isVerifyingSettingsPassword = false,
                                settingsPasswordError = "رمز عبور اشتباه است"
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d(
                        "MotherViewModel",
                        "Error verifying settings password: ${exception.message}"
                    )
                    _uiState.update {
                        it.copy(
                            settingsUnlocked = false,
                            showSettingsPasswordPrompt = true,
                            isVerifyingSettingsPassword = false,
                            settingsPasswordError =
                                "خطا در اتصال: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
        }
    }

    fun dismissSettingsPasswordPrompt() {
        _uiState.update {
            it.copy(
                showSettingsPasswordPrompt = false,
                settingsPasswordError = null,
                isVerifyingSettingsPassword = false
            )
        }
    }

    fun logout() {
        authRepository.logout()
        // Become a guest immediately: lock Profile, clear parent data, return to dashboard.
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                selectedTab = MotherTab.DASHBOARD,
                username = "",
                displayName = "",
                settingsUnlocked = false,
                showSettingsPasswordPrompt = false,
                isVerifyingSettingsPassword = false,
                settingsPasswordError = null,
                childrenState = ChildrenState.Idle,
                customWordsState = CustomWordsState.Idle,
                selectedChild = null
            )
        }
    }

    // ==================== Children Operations - عملیات فرزندان ====================

    fun loadChildren() {
        _uiState.update { it.copy(childrenState = ChildrenState.Loading) }

        viewModelScope.launch {
            childRepository.getChildren().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            // Auto-select the first child if none is selected
                            val newSelected = it.selectedChild
                                ?.let { current -> response.data.firstOrNull { c -> c.id == current.id } }
                                ?: response.data.firstOrNull()
                            it.copy(
                                childrenState = ChildrenState.Success(response.data),
                                selectedChild = newSelected
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(childrenState = ChildrenState.Error("خطا در بارگذاری فرزندان"))
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error loading children: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            childrenState = ChildrenState.Error(
                                "خطا در اتصال: ${exception.message ?: "خطای نامشخص"}"
                            )
                        )
                    }
                }
            )
        }
    }

    fun createChild(name: String, age: Int, gender: Gender, avatarUrl: String? = null) {
        // Validation
        if (name.isBlank()) {
            _uiState.update { 
                it.copy(createChildState = CreateChildState.Error("نام فرزند الزامی است"))
            }
            return
        }
        if (age !in 1..18) {
            _uiState.update { 
                it.copy(createChildState = CreateChildState.Error("سن باید بین ۱ تا ۱۸ سال باشد")) 
            }
            return
        }
        AppLogger.d("mhmdrz", "Creating child...")
        _uiState.update { it.copy(createChildState = CreateChildState.Loading) }

        viewModelScope.launch {
            val request = CreateChildRequest(name = name, age = age, gender = gender, avatarUrl = avatarUrl)
            childRepository.createChild(request).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val newChild = response.data ?: return@fold
                        _uiState.update {
                            // Update children list
                            val currentChildren = it.children
                            val updatedChildren = currentChildren + newChild

                            it.copy(
                                childrenState = ChildrenState.Success(updatedChildren),
                                createChildState = CreateChildState.Success(newChild),
                                showAddChildDialog = false,
                                // Auto-select first added child
                                selectedChild = it.selectedChild ?: newChild,
                                successMessage = "فرزند با موفقیت اضافه شد"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                createChildState = CreateChildState.Error(
                                    response.message ?: "خطا در ایجاد فرزند"
                                )
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error creating child: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            createChildState = CreateChildState.Error(
                                "خطا در ایجاد فرزند: ${exception.message ?: "خطای نامشخص"}"
                            )
                        )
                    }
                }
            )
        }
    }

    fun updateChild(childId: Int, name: String? = null, age: Int? = null, gender: Gender? = null, avatarUrl: String? = null) {
        // Validation
        if (name != null && name.isBlank()) {
            _uiState.update { 
                it.copy(updateChildState = UpdateChildState.Error("نام فرزند نمی‌تواند خالی باشد", childId))
            }
            return
        }
        if (age != null && (age !in 1..18)) {
            _uiState.update { 
                it.copy(updateChildState = UpdateChildState.Error("سن باید بین ۱ تا ۱۸ سال باشد", childId)) 
            }
            return
        }

        _uiState.update { it.copy(updateChildState = UpdateChildState.Loading(childId)) }

        viewModelScope.launch {
            val request = UpdateChildRequest(name = name, age = age, gender = gender, avatarUrl = avatarUrl)
            childRepository.updateChild(childId, request).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val updatedChild = response.data ?: return@launch
                        _uiState.update {
                            // Update children list
                            val updatedChildren = it.children.map { child ->
                                if (child.id == childId) updatedChild else child
                            }
                            
                            it.copy(
                                childrenState = ChildrenState.Success(updatedChildren),
                                updateChildState = UpdateChildState.Success(updatedChild),
                                selectedChild = if (it.selectedChild?.id == childId) updatedChild else it.selectedChild,
                                successMessage = "فرزند با موفقیت ویرایش شد"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                updateChildState = UpdateChildState.Error(
                                    response.message ?: "خطا در ویرایش فرزند",
                                    childId
                                )
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error updating child: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            updateChildState = UpdateChildState.Error(
                                "خطا در ویرایش فرزند: ${exception.message ?: "خطای نامشخص"}",
                                childId
                            )
                        )
                    }
                }
            )
        }
    }

    fun deleteChild(childId: Int) {
        _uiState.update { it.copy(deleteChildState = DeleteChildState.Loading(childId)) }

        viewModelScope.launch {
            childRepository.deleteChild(childId).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            // Update children list
                            val updatedChildren = it.children.filter { child -> child.id != childId }
                            
                            it.copy(
                                childrenState = ChildrenState.Success(updatedChildren),
                                deleteChildState = DeleteChildState.Success(childId),
                                selectedChild = if (it.selectedChild?.id == childId) null else it.selectedChild,
                                successMessage = "فرزند با موفقیت حذف شد"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                deleteChildState = DeleteChildState.Error(
                                    response.message ?: "خطا در حذف فرزند",
                                    childId
                                )
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error deleting child: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            deleteChildState = DeleteChildState.Error(
                                "خطا در حذف فرزند: ${exception.message ?: "خطای نامشخص"}",
                                childId
                            )
                        )
                    }
                }
            )
        }
    }

    fun selectChild(child: ChildDTO?) {
        _uiState.update { it.copy(selectedChild = child) }
    }

    fun clearCreateChildError() {
        _uiState.update { it.copy(createChildState = CreateChildState.Idle) }
    }

    fun clearUpdateChildError() {
        _uiState.update { it.copy(updateChildState = UpdateChildState.Idle) }
    }

    fun clearDeleteChildError() {
        _uiState.update { it.copy(deleteChildState = DeleteChildState.Idle) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun retry() {
        refresh()
    }

    fun showAddChildDialog() {
        // Reset create child state when opening dialog
        _uiState.update { 
            it.copy(
                showAddChildDialog = true,
                createChildState = CreateChildState.Idle
            ) 
        }
    }

    fun hideAddChildDialog() {
        _uiState.update { 
            it.copy(
                showAddChildDialog = false,
                createChildState = CreateChildState.Idle
            ) 
        }
    }

    // ==================== Custom Words Operations - عملیات کلمات سفارشی ====================

    /**
     * Load all custom words created by parent
     * بارگذاری کلمات سفارشی مادر
     */
    fun loadCustomWords() {
        _uiState.update { it.copy(customWordsState = CustomWordsState.Loading) }

        viewModelScope.launch {
            contentRepository.getCustomWords().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            it.copy(customWordsState = CustomWordsState.Success(response.data))
                        }
                    } else {
                        AppLogger.d("MotherViewModel", "Error creating custom word: ${response.message}")
                        _uiState.update {
                            it.copy(customWordsState = CustomWordsState.Error("خطا در بارگذاری کلمات سفارشی"))
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error loading custom words: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            customWordsState = CustomWordsState.Error(
                                "خطا در اتصال: ${exception.message ?: "خطای نامشخص"}"
                            )
                        )
                    }
                }
            )
        }
    }

    /**
     * Create a new custom word
     * ایجاد کلمه سفارشی جدید
     */
    fun createCustomWord(
        wordFa: String,
        wordEn: String? = null,
        imageUrl: String? = null,
        audioUrl: String,
        categoryId: Int? = null
    ) {
        // Validation
        if (wordFa.isBlank()) {
            _uiState.update {
                it.copy(createCustomWordState = CreateCustomWordState.Error("کلمه فارسی الزامی است"))
            }
            return
        }
        if (audioUrl.isBlank()) {
            _uiState.update {
                it.copy(createCustomWordState = CreateCustomWordState.Error("فایل صوتی الزامی است"))
            }
            return
        }
        if (imageUrl.isNullOrBlank()) {
            _uiState.update {
                it.copy(createCustomWordState = CreateCustomWordState.Error("تصویر کلمه الزامی است"))
            }
            return
        }

        AppLogger.d("MotherViewModel", "Creating custom word: $wordFa")
        _uiState.update { it.copy(createCustomWordState = CreateCustomWordState.Loading) }

        viewModelScope.launch {
            val request = CreateCustomWordRequest(
                wordFa = wordFa,
                wordEn = wordEn,
                imageUrl = imageUrl,
                audioUrl = audioUrl,
                categoryId = categoryId
            )
            contentRepository.createCustomWord(request).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val newWord = response.data ?: return@launch
                        AppLogger.d("MotherViewModel", "Custom word created successfully: ${newWord.wordFa}")
                        _uiState.update {
                            // Update custom words list
                            val currentWords = it.customWords
                            val updatedWords = currentWords + newWord

                            it.copy(
                                customWordsState = CustomWordsState.Success(updatedWords),
                                createCustomWordState = CreateCustomWordState.Success(newWord),
                                successMessage = "کلمه با موفقیت اضافه شد"
                            )
                        }
                    } else {
                        AppLogger.d("MotherViewModel", "Error creating custom word: ${response.message}")
                        _uiState.update {
                            it.copy(
                                createCustomWordState = CreateCustomWordState.Error(
                                    response.message
                                )
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error creating custom word: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            createCustomWordState = CreateCustomWordState.Error(
                                "خطا در ایجاد کلمه: ${exception.message ?: "خطای نامشخص"}"
                            )
                        )
                    }
                }
            )
        }
    }

    /**
     * Delete a custom word
     * حذف کلمه سفارشی
     */
    fun deleteCustomWord(wordId: Int) {
        _uiState.update { it.copy(deleteCustomWordState = DeleteCustomWordState.Loading(wordId)) }

        viewModelScope.launch {
            contentRepository.deleteCustomWord(wordId).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            // Update custom words list
                            val updatedWords = it.customWords.filter { word -> word.id != wordId }

                            it.copy(
                                customWordsState = CustomWordsState.Success(updatedWords),
                                deleteCustomWordState = DeleteCustomWordState.Success(wordId),
                                successMessage = "کلمه با موفقیت حذف شد"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                deleteCustomWordState = DeleteCustomWordState.Error(
                                    response.message,
                                    wordId
                                )
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error deleting custom word: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            deleteCustomWordState = DeleteCustomWordState.Error(
                                "خطا در حذف کلمه: ${exception.message ?: "خطای نامشخص"}",
                                wordId
                            )
                        )
                    }
                }
            )
        }
    }

    /**
     * Clear create custom word error
     * پاک کردن خطای ایجاد کلمه سفارشی
     */
    fun clearCreateCustomWordError() {
        _uiState.update { it.copy(createCustomWordState = CreateCustomWordState.Idle) }
    }

    /**
     * Clear delete custom word error
     * پاک کردن خطای حذف کلمه سفارشی
     */
    fun clearDeleteCustomWordError() {
        _uiState.update { it.copy(deleteCustomWordState = DeleteCustomWordState.Idle) }
    }

    /**
     * Retry loading custom words
     * تلاش مجدد برای بارگذاری کلمات سفارشی
     */
    fun retryCustomWords() {
        loadCustomWords()
    }

    // ==================== Categories Operations - عملیات دسته‌بندی‌ها ====================

    /**
     * Load all available categories (used by the add-word picker).
     * بارگذاری همه دسته‌بندی‌های موجود برای انتخاب در افزودن کلمه
     */
    fun loadCategories() {
        _uiState.update { it.copy(categoriesState = CategoriesState.Loading) }

        viewModelScope.launch {
            contentRepository.getAllCategories().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            it.copy(categoriesState = CategoriesState.Success(response.data))
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                categoriesState = CategoriesState.Error("خطا در بارگذاری دسته‌بندی‌ها")
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error loading categories: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            categoriesState = CategoriesState.Error(
                                "خطا در اتصال: ${exception.message ?: "خطای نامشخص"}"
                            )
                        )
                    }
                }
            )
        }
    }

    /**
     * Create a new category. On success the new category is appended to the
     * cached list so the add-word screen can immediately pick it.
     * ایجاد دسته‌بندی جدید توسط مادر
     */
    fun createCategory(nameFa: String, nameEn: String? = null, iconUrl: String? = null) {
        val trimmedFa = nameFa.trim()
        if (trimmedFa.isBlank()) {
            _uiState.update {
                it.copy(createCategoryState = CreateCategoryState.Error("نام دسته‌بندی الزامی است"))
            }
            return
        }

        _uiState.update { it.copy(createCategoryState = CreateCategoryState.Loading) }

        viewModelScope.launch {
            val request = CreateCategoryRequest(
                nameFa = trimmedFa,
                nameEn = nameEn?.trim().orEmpty(),
                iconUrl = iconUrl
            )
            contentRepository.createCategory(request).fold(
                onSuccess = { response ->
                    val data = response.data
                    if (response.success && data != null) {
                        _uiState.update {
                            // Merge the new category if it isn't already cached
                            val existing = it.categories
                            val merged = if (existing.any { c -> c.id == data.id }) {
                                existing
                            } else {
                                existing + data
                            }
                            it.copy(
                                categoriesState = CategoriesState.Success(merged),
                                createCategoryState = CreateCategoryState.Success(data),
                                successMessage = "دسته‌بندی با موفقیت اضافه شد"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                createCategoryState = CreateCategoryState.Error(
                                    response.message.ifBlank { "خطا در ایجاد دسته‌بندی" }
                                )
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error creating category: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            createCategoryState = CreateCategoryState.Error(
                                "خطا در ایجاد دسته‌بندی: ${exception.message ?: "خطای نامشخص"}"
                            )
                        )
                    }
                }
            )
        }
    }

    fun clearCreateCategoryError() {
        _uiState.update { it.copy(createCategoryState = CreateCategoryState.Idle) }
    }

    // ==================== Category Words - کلمات یک دسته‌بندی ====================

    /**
     * Load the base/curated words for a single category (public endpoint), shown in the
     * add-word manager when a category is opened. Parent-created custom words for the same
     * category are merged in the UI from [MotherUiState.customWords].
     * بارگذاری کلمات یک دسته‌بندی
     */
    fun loadCategoryWords(categoryId: Int) {
        _uiState.update { it.copy(categoryWordsState = CategoryWordsState.Loading) }

        viewModelScope.launch {
            contentRepository.getWordsByCategory(categoryId).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            it.copy(categoryWordsState = CategoryWordsState.Success(response.data))
                        }
                    } else {
                        _uiState.update {
                            it.copy(categoryWordsState = CategoryWordsState.Error("خطا در بارگذاری کلمات"))
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error loading category words: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            categoryWordsState = CategoryWordsState.Error(
                                "خطا در اتصال: ${exception.message ?: "خطای نامشخص"}"
                            )
                        )
                    }
                }
            )
        }
    }

    fun clearCategoryWords() {
        _uiState.update { it.copy(categoryWordsState = CategoryWordsState.Idle) }
    }
}
