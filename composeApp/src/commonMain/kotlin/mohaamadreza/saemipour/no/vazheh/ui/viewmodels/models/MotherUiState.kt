package mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models

import androidx.compose.runtime.Stable
import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.data.CustomWordDTO
import mohaamadreza.saemipour.no.vazheh.data.WordDTO

@Stable
data class MotherUiState(
    val username: String = "",
    val displayName: String = "",
    // Whether a parent account is logged in. Drives the locked Profile tab — guests
    // can use the dashboard/games but must log in to reach Profile/settings.
    val isLoggedIn: Boolean = false,
    val selectedTab: MotherTab = MotherTab.DASHBOARD,
    val settingsUnlocked: Boolean = false,
    val showSettingsPasswordPrompt: Boolean = false,
    val isVerifyingSettingsPassword: Boolean = false,
    val settingsPasswordError: String? = null,
    
    // Children states using sealed classes
    val childrenState: ChildrenState = ChildrenState.Idle,
    val createChildState: CreateChildState = CreateChildState.Idle,
    val updateChildState: UpdateChildState = UpdateChildState.Idle,
    val deleteChildState: DeleteChildState = DeleteChildState.Idle,
    
    // Custom words states using sealed classes
    val customWordsState: CustomWordsState = CustomWordsState.Idle,
    val createCustomWordState: CreateCustomWordState = CreateCustomWordState.Idle,
    val deleteCustomWordState: DeleteCustomWordState = DeleteCustomWordState.Idle,

    // Categories (used by the add-word flow to pick or create a category)
    val categoriesState: CategoriesState = CategoriesState.Idle,
    val createCategoryState: CreateCategoryState = CreateCategoryState.Idle,

    // Base/curated words for the currently opened category (in the add-word manager)
    val categoryWordsState: CategoryWordsState = CategoryWordsState.Idle,

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

    // ==================== Categories Helpers ====================

    val categories: List<CategoryDTO>
        get() = when (categoriesState) {
            is CategoriesState.Success -> categoriesState.categories
            else -> emptyList()
        }

    val isLoadingCategories: Boolean
        get() = categoriesState is CategoriesState.Loading

    val isCreatingCategory: Boolean
        get() = createCategoryState is CreateCategoryState.Loading

    val createCategoryErrorMessage: String?
        get() = (createCategoryState as? CreateCategoryState.Error)?.message

    // ==================== Category Words Helpers ====================

    val categoryWords: List<WordDTO>
        get() = (categoryWordsState as? CategoryWordsState.Success)?.words ?: emptyList()

    val isLoadingCategoryWords: Boolean
        get() = categoryWordsState is CategoryWordsState.Loading

    val categoryWordsErrorMessage: String?
        get() = (categoryWordsState as? CategoryWordsState.Error)?.message
}

@Stable
enum class MotherTab {
    DASHBOARD,
    PROFILE
}

// ==================== Children States ====================
@Stable
sealed class ChildrenState {
    data object Idle : ChildrenState()
    data object Loading : ChildrenState()
    data class Success(val children: List<ChildDTO>) : ChildrenState()
    data class Error(val message: String) : ChildrenState()
}
@Stable
sealed class CreateChildState {
    data object Idle : CreateChildState()
    data object Loading : CreateChildState()
    data class Success(val child: ChildDTO) : CreateChildState()
    data class Error(val message: String) : CreateChildState()
}
@Stable
sealed class UpdateChildState {
    data object Idle : UpdateChildState()
    data class Loading(val childId: Int) : UpdateChildState()
    data class Success(val child: ChildDTO) : UpdateChildState()
    data class Error(val message: String, val childId: Int) : UpdateChildState()
}
@Stable
sealed class DeleteChildState {
    data object Idle : DeleteChildState()
    data class Loading(val childId: Int) : DeleteChildState()
    data class Success(val deletedChildId: Int) : DeleteChildState()
    data class Error(val message: String, val childId: Int) : DeleteChildState()
}

// ==================== Custom Words States ====================
@Stable
sealed class CustomWordsState {
    data object Idle : CustomWordsState()
    data object Loading : CustomWordsState()
    data class Success(val words: List<CustomWordDTO>) : CustomWordsState()
    data class Error(val message: String) : CustomWordsState()
}
@Stable
sealed class CreateCustomWordState {
    data object Idle : CreateCustomWordState()
    data object Loading : CreateCustomWordState()
    data class Success(val word: CustomWordDTO) : CreateCustomWordState()
    data class Error(val message: String) : CreateCustomWordState()
}
@Stable
sealed class DeleteCustomWordState {
    data object Idle : DeleteCustomWordState()
    data class Loading(val wordId: Int) : DeleteCustomWordState()
    data class Success(val deletedWordId: Int) : DeleteCustomWordState()
    data class Error(val message: String, val wordId: Int) : DeleteCustomWordState()
}

// ==================== Categories States ====================
@Stable
sealed class CategoriesState {
    data object Idle : CategoriesState()
    data object Loading : CategoriesState()
    data class Success(val categories: List<CategoryDTO>) : CategoriesState()
    data class Error(val message: String) : CategoriesState()
}

@Stable
sealed class CreateCategoryState {
    data object Idle : CreateCategoryState()
    data object Loading : CreateCategoryState()
    data class Success(val category: CategoryDTO) : CreateCategoryState()
    data class Error(val message: String) : CreateCategoryState()
}

// ==================== Category Words States ====================
@Stable
sealed class CategoryWordsState {
    data object Idle : CategoryWordsState()
    data object Loading : CategoryWordsState()
    data class Success(val words: List<WordDTO>) : CategoryWordsState()
    data class Error(val message: String) : CategoryWordsState()
}
