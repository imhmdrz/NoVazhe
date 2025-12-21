package mohaamadreza.saemipour.no.vazheh.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChildViewModel : ViewModel() {

    // Example state logic, to be expanded based on specific sharing needs
    // For now, moving the logic implied by "shared":

    // Maybe keep track of selected category?
    private val _selectedCategory = MutableStateFlow<Int?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    fun onCategorySelected(index: Int) {
        _selectedCategory.value = index
    }
}
