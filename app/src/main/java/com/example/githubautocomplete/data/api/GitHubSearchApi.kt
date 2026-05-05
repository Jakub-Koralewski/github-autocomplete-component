package com.example.githubautocomplete.data.api

import com.example.githubautocomplete.data.model.SearchReposResponse
import com.example.githubautocomplete.data.model.SearchUsersResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubSearchApi {
    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("per_page") perPage: Int
    ): SearchUsersResponse

    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("per_page") perPage: Int
    ): SearchReposResponse
}
