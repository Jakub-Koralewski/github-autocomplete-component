package com.example.githubautocomplete

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.githubautocomplete.data.RemoteGitHubSearchRepository
import com.example.githubautocomplete.data.api.GitHubApiModule
import com.example.githubautocomplete.ui.GithubAutocompleteViewModel
import com.example.githubautocomplete.ui.GithubAutocompleteScreen

class MainActivity : ComponentActivity() {

    private val viewModel: GithubAutocompleteViewModel by viewModels {
        GithubAutocompleteViewModel.factory(
            RemoteGitHubSearchRepository(GitHubApiModule.createSearchApi())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GithubAutocompleteScreen(viewModel = viewModel)
                }
            }
        }
    }
}
