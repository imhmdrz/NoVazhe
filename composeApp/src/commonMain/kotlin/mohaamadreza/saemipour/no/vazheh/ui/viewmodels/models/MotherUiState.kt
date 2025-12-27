package mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models

import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.data.CustomWordDTO

data class MotherUiState(
    val username: String = "",
    val displayName: String = "",
    val selectedTab: MotherTab = MotherTab.DASHBOARD,
    
    // Children states using sealed classes
    val childrenState: ChildrenState = ChildrenState.Idle,
    val createChildState: CreateChildState = CreateChildState.Idle,
    val updateChildState: UpdateChildState = UpdateChildState.Idle,
    val deleteChildState: DeleteChildState = DeleteChildState.Idle,
    
    // Custom words states using sealed classes
    val customWordsState: CustomWordsState = CustomWordsState.Idle,
    val createCustomWordState: CreateCustomWordState = CreateCustomWordState.Idle,
    val deleteCustomWordState: DeleteCustomWordState = DeleteCustomWordState.Idle,
    
    // Selected child
    val selectedChild: ChildDTO? = null,
    
    // Dialog visibility
    val showAddChildDialog: Boolean = false,
    
    // Global success message (for snackbar/toast)
    val successMessage: String? = null
) {
    // ==================== Children Helpers ====================
    
    val children: List<ChildDTO>
        get() = when (childrenState) {
            is ChildrenState.Success -> childrenState.children
            else -> emptyList()
        }
    
    val isLoadingChildren: Boolean
        get() = childrenState is ChildrenState.Loading
    
    val isCreatingChild: Boolean
        get() = createChildState is CreateChildState.Loading
    
    val canAddChild: Boolean
        get() = children.size < 2
    
    val childrenErrorMessage: String?
        get() = (childrenState as? ChildrenState.Error)?.message
    
    val createChildErrorMessage: String?
        get() = (createChildState as? CreateChildState.Error)?.message
    
    // ==================== Custom Words Helpers ====================
    
    val customWords: List<CustomWordDTO>
        get() = when (customWordsState) {
            is CustomWordsState.Success -> customWordsState.words
            else -> emptyList()
        }
    
    val isLoadingCustomWords: Boolean
        get() = customWordsState is CustomWordsState.Loading
    
    val isCreatingCustomWord: Boolean
        get() = createCustomWordState is CreateCustomWordState.Loading
    
    val isDeletingCustomWord: Boolean
        get() = deleteCustomWordState is DeleteCustomWordState.Loading
    
    val customWordsErrorMessage: String?
        get() = (customWordsState as? CustomWordsState.Error)?.message
    
    val createCustomWordErrorMessage: String?
        get() = (createCustomWordState as? CreateCustomWordState.Error)?.message
    
    val deleteCustomWordErrorMessage: String?
        get() = (deleteCustomWordState as? DeleteCustomWordState.Error)?.message
}

enum class MotherTab {
    DASHBOARD,
    PROFILE
}

// ==================== Children States ====================

sealed class ChildrenState {
    data object Idle : ChildrenState()
    data object Loading : ChildrenState()
    data class Success(val children: List<ChildDTO>) : ChildrenState()
    data class Error(val message: String) : ChildrenState()
}

sealed class CreateChildState {
    data object Idle : CreateChildState()
    data object Loading : CreateChildState()
    data class Success(val child: ChildDTO) : CreateChildState()
    data class Error(val message: String) : CreateChildState()
}

sealed class UpdateChildState {
    data object Idle : UpdateChildState()
    data class Loading(val childId: Int) : UpdateChildState()
    data class Success(val child: ChildDTO) : UpdateChildState()
    data class Error(val message: String, val childId: Int) : UpdateChildState()
}

sealed class DeleteChildState {
    data object Idle : DeleteChildState()
    data class Loading(val childId: Int) : DeleteChildState()
    data class Success(val deletedChildId: Int) : DeleteChildState()
    data class Error(val message: String, val childId: Int) : DeleteChildState()
}

// ==================== Custom Words States ====================

sealed class CustomWordsState {
    data object Idle : CustomWordsState()
    data object Loading : CustomWordsState()
    data class Success(val words: List<CustomWordDTO>) : CustomWordsState()
    data class Error(val message: String) : CustomWordsState()
}

sealed class CreateCustomWordState {
    data object Idle : CreateCustomWordState()
    data object Loading : CreateCustomWordState()
    data class Success(val word: CustomWordDTO) : CreateCustomWordState()
    data class Error(val message: String) : CreateCustomWordState()
}

sealed class DeleteCustomWordState {
    data object Idle : DeleteCustomWordState()
    data class Loading(val wordId: Int) : DeleteCustomWordState()
    data class Success(val deletedWordId: Int) : DeleteCustomWordState()
    data class Error(val message: String, val wordId: Int) : DeleteCustomWordState()
}
