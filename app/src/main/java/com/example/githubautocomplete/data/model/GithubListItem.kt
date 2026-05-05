package com.example.githubautocomplete.data.model

/**
 * Combined list row for users and repositories. Sorting uses [sortKey] (login or full name).
 */
sealed class GithubListItem {
    abstract val sortKey: String

    data class UserLogin(val login: String) : GithubListItem() {
        override val sortKey: String get() = login.lowercase()
    }

    data class Repository(val fullName: String) : GithubListItem() {
        override val sortKey: String get() = fullName.lowercase()
    }
}
