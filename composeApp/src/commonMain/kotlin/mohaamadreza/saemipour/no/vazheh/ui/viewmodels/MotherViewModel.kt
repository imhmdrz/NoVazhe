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
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.ChildrenState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CreateChildState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.DeleteChildState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.MotherTab
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.MotherUiState
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.UpdateChildState

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

    fun onTabSelected(tab: MotherTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun logout() {
        authRepository.logout()
    }

    // ==================== Children Operations - عملیات فرزندان ====================

    fun loadChildren() {
        _uiState.update { it.copy(childrenState = ChildrenState.Loading) }

        viewModelScope.launch {
            childRepository.getChildren().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            it.copy(childrenState = ChildrenState.Success(response.data))
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
        if (!_uiState.value.canAddChild) {
            _uiState.update { 
                it.copy(createChildState = CreateChildState.Error("حداکثر ۲ فرزند می‌توانید اضافه کنید")) 
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
        if (age != null && (age < 1 || age > 18)) {
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
        loadChildren()
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
}
