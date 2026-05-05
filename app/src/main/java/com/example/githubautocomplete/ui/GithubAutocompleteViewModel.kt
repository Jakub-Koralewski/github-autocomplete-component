package com.example.githubautocomplete.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.githubautocomplete.data.GitHubSearchRepository
import com.example.githubautocomplete.data.model.GithubListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

sealed interface AutocompleteUiState {
    data object MinCharsHint : AutocompleteUiState
    data object Loading : AutocompleteUiState
    data class Success(val items: List<GithubListItem>) : AutocompleteUiState
    data class Error(val message: String) : AutocompleteUiState
}

class GithubAutocompleteViewModel(
    private val repository: GitHubSearchRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    val queryText: StateFlow<String> = query

    fun onQueryChange(text: String) {
        query.value = text
    }

    val uiState = query
        .debounce(300L)
        .flatMapLatest { raw ->
            flow {
                val trimmed = raw.trim()
                if (trimmed.length < MIN_QUERY_LENGTH) {
                    emit(AutocompleteUiState.MinCharsHint)
                    return@flow
                }
                emit(AutocompleteUiState.Loading)
                try {
                    val items = repository.searchCombined(trimmed)
                    emit(AutocompleteUiState.Success(items))
                } catch (e: Exception) {
                    emit(AutocompleteUiState.Error(e.message ?: "Request failed"))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AutocompleteUiState.MinCharsHint
        )

    companion object {
        const val MIN_QUERY_LENGTH = 3

        fun factory(repository: GitHubSearchRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(GithubAutocompleteViewModel::class.java))
                    return GithubAutocompleteViewModel(repository) as T
                }
            }
    }
}
