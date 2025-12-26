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
import mohaamadreza.saemipour.no.vazheh.data.CreateChildRequest
import mohaamadreza.saemipour.no.vazheh.data.Gender
import mohaamadreza.saemipour.no.vazheh.data.UpdateChildRequest

data class MotherUiState(
    val username: String = "",
    val displayName: String = "",
    val selectedTab: MotherTab = MotherTab.DASHBOARD,
    // Children state
    val children: List<ChildDTO> = emptyList(),
    val selectedChild: ChildDTO? = null,
    val isLoadingChildren: Boolean = false,
    val isCreatingChild: Boolean = false,
    val isUpdatingChild: Boolean = false,
    val isDeletingChild: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Add child dialog state
    val showAddChildDialog: Boolean = false
)

enum class MotherTab {
    DASHBOARD,
    PROFILE
}

class MotherViewModel(
    private val authRepository: AuthRepository,
    private val childRepository: ChildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotherUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserInfo()
        loadChildren()
    }

    /**
     * Load user information from storage
     * بارگذاری اطلاعات کاربر
     */
    private fun loadUserInfo() {
        val username = authRepository.getUsername() ?: ""
        val displayName = authRepository.getDisplayName() ?: ""
        _uiState.update {
            it.copy(
                username = username,
                displayName = displayName
            )
        }
    }

    /**
     * Select a tab
     * انتخاب تب
     */
    fun onTabSelected(tab: MotherTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * Logout user
     * خروج کاربر
     */
    fun logout() {
        authRepository.logout()
    }

    // ==================== Children Operations - عملیات فرزندان ====================

    /**
     * Load all children
     * بارگذاری لیست فرزندان
     */
    fun loadChildren() {
        _uiState.update { it.copy(isLoadingChildren = true, errorMessage = null) }

        viewModelScope.launch {
            childRepository.getChildren().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            it.copy(
                                children = response.data,
                                isLoadingChildren = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingChildren = false,
                                errorMessage = "خطا در بارگذاری فرزندان"
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error loading children: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isLoadingChildren = false,
                            errorMessage = "خطا در اتصال: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Create a new child
     * ایجاد فرزند جدید (حداکثر ۲ فرزند)
     */
    fun createChild(name: String, age: Int, gender: Gender, avatarUrl: String? = null) {
        // Validation
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "نام فرزند الزامی است") }
            return
        }
        if (age !in 1..18) {
            _uiState.update { it.copy(errorMessage = "سن باید بین ۱ تا ۱۸ سال باشد") }
            return
        }
        if (_uiState.value.children.size >= 2) {
            _uiState.update { it.copy(errorMessage = "حداکثر ۲ فرزند می‌توانید اضافه کنید") }
            return
        }

        AppLogger.d("mhmdrz" , "here")
        _uiState.update { it.copy(isCreatingChild = true, errorMessage = null) }

        viewModelScope.launch {
            val request = CreateChildRequest(name = name, age = age, gender = gender, avatarUrl = avatarUrl)
            childRepository.createChild(request).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _uiState.update {
                            it.copy(
                                children = (it.children + response.data).filterNotNull(),
                                isCreatingChild = false,
                                showAddChildDialog = false,
                                successMessage = "فرزند با موفقیت اضافه شد"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isCreatingChild = false,
                                errorMessage = response.message
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error creating child: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isCreatingChild = false,
                            errorMessage = "خطا در ایجاد فرزند: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Update child
     * ویرایش فرزند
     */
    fun updateChild(childId: Int, name: String? = null, age: Int? = null, gender: Gender? = null, avatarUrl: String? = null) {
        // Validation
        if (name != null && name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "نام فرزند نمی‌تواند خالی باشد") }
            return
        }
        if (age != null && (age < 1 || age > 18)) {
            _uiState.update { it.copy(errorMessage = "سن باید بین ۱ تا ۱۸ سال باشد") }
            return
        }

        _uiState.update { it.copy(isUpdatingChild = true, errorMessage = null) }

        viewModelScope.launch {
            val request = UpdateChildRequest(name = name, age = age, gender = gender, avatarUrl = avatarUrl)
            childRepository.updateChild(childId, request).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _uiState.update {
                            it.copy(
                                children = it.children.mapNotNull { child ->
                                    if (child.id == childId) response.data else child
                                },
                                selectedChild = if (it.selectedChild?.id == childId) response.data else it.selectedChild,
                                isUpdatingChild = false,
                                successMessage = "فرزند با موفقیت ویرایش شد"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isUpdatingChild = false,
                                errorMessage = response.message
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error updating child: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isUpdatingChild = false,
                            errorMessage = "خطا در ویرایش فرزند: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Delete child
     * حذف فرزند
     */
    fun deleteChild(childId: Int) {
        _uiState.update { it.copy(isDeletingChild = true, errorMessage = null) }

        viewModelScope.launch {
            childRepository.deleteChild(childId).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            it.copy(
                                children = it.children.filter { child -> child.id != childId },
                                selectedChild = if (it.selectedChild?.id == childId) null else it.selectedChild,
                                isDeletingChild = false,
                                successMessage = "فرزند با موفقیت حذف شد"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isDeletingChild = false,
                                errorMessage = response.message
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("MotherViewModel", "Error deleting child: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isDeletingChild = false,
                            errorMessage = "خطا در حذف فرزند: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Select a child
     * انتخاب فرزند
     */
    fun selectChild(child: ChildDTO?) {
        _uiState.update { it.copy(selectedChild = child) }
    }

    /**
     * Clear error message
     * پاک کردن پیام خطا
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Clear success message
     * پاک کردن پیام موفقیت
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /**
     * Retry loading children
     * تلاش مجدد برای بارگذاری فرزندان
     */
    fun retry() {
        loadChildren()
    }

    /**
     * Check if can add more children (max 2)
     * آیا می‌توان فرزند دیگری اضافه کرد
     */
    fun canAddChild(): Boolean {
        return _uiState.value.children.size < 2
    }

    /**
     * Show add child dialog
     * نمایش دیالوگ افزودن فرزند
     */
    fun showAddChildDialog() {
        _uiState.update { it.copy(showAddChildDialog = true) }
    }

    /**
     * Hide add child dialog
     * بستن دیالوگ افزودن فرزند
     */
    fun hideAddChildDialog() {
        _uiState.update { it.copy(showAddChildDialog = false) }
    }
}
