package com.example.githubautocomplete.data.model

import com.squareup.moshi.Json

data class SearchUsersResponse(
    val items: List<UserJson> = emptyList()
)

data class UserJson(
    val login: String
)

data class SearchReposResponse(
    val items: List<RepoJson> = emptyList()
)

data class RepoJson(
    @Json(name = "full_name") val fullName: String
)
