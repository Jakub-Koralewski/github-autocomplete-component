package com.example.githubautocomplete.data

import com.example.githubautocomplete.data.api.GitHubSearchApi
import com.example.githubautocomplete.data.model.GithubListItem
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface GitHubSearchRepository {
    suspend fun searchCombined(query: String): List<GithubListItem>
}

class RemoteGitHubSearchRepository(
    private val api: GitHubSearchApi
) : GitHubSearchRepository {

    /**
     * Fetches up to [PER_SOURCE_LIMIT] users and repositories each, merges and sorts
     * alphabetically by login / full name, then returns at most [MAX_COMBINED_RESULTS] items.
     */
    override suspend fun searchCombined(query: String): List<GithubListItem> = coroutineScope {
        val trimmed = query.trim()
        require(trimmed.isNotEmpty())

        val usersDeferred = async { api.searchUsers(trimmed, PER_SOURCE_LIMIT) }
        val reposDeferred = async { api.searchRepositories(trimmed, PER_SOURCE_LIMIT) }

        val users = usersDeferred.await().items
        val repos = reposDeferred.await().items

        val merged = buildList {
            users.forEach { add(GithubListItem.UserLogin(it.login)) }
            repos.forEach { add(GithubListItem.Repository(it.fullName)) }
        }
        merged
            .sortedBy { it.sortKey }
            .take(MAX_COMBINED_RESULTS)
    }

    companion object {
        const val PER_SOURCE_LIMIT = 25
        const val MAX_COMBINED_RESULTS = 50
    }
}
