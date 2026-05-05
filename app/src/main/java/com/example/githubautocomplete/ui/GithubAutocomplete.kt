package com.example.githubautocomplete.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.githubautocomplete.data.model.GithubListItem

/**
 * Reusable entry: inject [viewModel] from the host screen/activity.
 */
@Composable
fun GithubAutocompleteScreen(
    modifier: Modifier = Modifier,
    viewModel: GithubAutocompleteViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val queryText by viewModel.queryText.collectAsStateWithLifecycle()
    GithubAutocompleteContent(
        queryText = queryText,
        onQueryChange = viewModel::onQueryChange,
        state = state,
        modifier = modifier
    )
}

/**
 * Stateless body: easy to preview, test, and embed in other screens.
 */
@Composable
fun GithubAutocompleteContent(
    queryText: String,
    onQueryChange: (String) -> Unit,
    state: AutocompleteUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = queryText,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search GitHub users & repos") },
            singleLine = true
        )
        when (state) {
            is AutocompleteUiState.MinCharsHint -> {
                HintText(
                    text = "Enter at least ${GithubAutocompleteViewModel.MIN_QUERY_LENGTH} characters to search."
                )
            }
            is AutocompleteUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .semantics { contentDescription = "Loading indicator" },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is AutocompleteUiState.Error -> {
                HintText(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is AutocompleteUiState.Success -> {
                if (state.items.isEmpty()) {
                    HintText(text = "No users or repositories match this query.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.items, key = { item ->
                            when (item) {
                                is GithubListItem.UserLogin -> "u:${item.login}"
                                is GithubListItem.Repository -> "r:${item.fullName}"
                            }
                        }) { item ->
                            GithubResultRow(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HintText(
    text: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    )
}

@Composable
private fun GithubResultRow(item: GithubListItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (item) {
            is GithubListItem.UserLogin -> {
                Icon(Icons.Default.Person, contentDescription = null)
                Column {
                    Text("User", style = MaterialTheme.typography.labelSmall)
                    Text(item.login, style = MaterialTheme.typography.bodyLarge)
                }
            }
            is GithubListItem.Repository -> {
                Icon(Icons.Default.Folder, contentDescription = null)
                Column {
                    Text("Repository", style = MaterialTheme.typography.labelSmall)
                    Text(item.fullName, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GithubAutocompleteContentPreview() {
    MaterialTheme {
        GithubAutocompleteContent(
            queryText = "ali",
            onQueryChange = {},
            state = AutocompleteUiState.Success(
                listOf(
                    GithubListItem.UserLogin("alice"),
                    GithubListItem.Repository("bob/demo"),
                    GithubListItem.UserLogin("zebra")
                )
            )
        )
    }
}
