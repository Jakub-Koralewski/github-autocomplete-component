package com.example.githubautocomplete.data.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * GitHub requires a valid User-Agent for API requests.
 */
class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(request)
    }
}
