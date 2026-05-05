package com.example.githubautocomplete.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object GitHubApiModule {

    private const val BASE_URL = "https://api.github.com/"
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    fun createSearchApi(
        userAgent: String = "GitHubAutocompleteDemo/1.0"
    ): GitHubSearchApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor(userAgent))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GitHubSearchApi::class.java)
    }

    /**
     * For tests: same stack against a [OkHttpClient] (e.g. MockWebServer).
     */
    fun createSearchApi(baseUrl: String, client: OkHttpClient): GitHubSearchApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GitHubSearchApi::class.java)
    }
}
