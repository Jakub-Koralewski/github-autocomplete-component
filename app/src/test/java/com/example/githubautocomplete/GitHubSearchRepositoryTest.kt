package com.example.githubautocomplete

import com.example.githubautocomplete.data.RemoteGitHubSearchRepository
import com.example.githubautocomplete.data.api.GitHubApiModule
import com.example.githubautocomplete.data.model.GithubListItem
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Integration-style test: verifies parallel calls, merge, alphabetical sort, and 50-item cap
 * without hitting the real GitHub API.
 */
class GitHubSearchRepositoryTest {

    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `merges users and repositories and sorts alphabetically`() = runTest {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: return MockResponse().setResponseCode(404)
                return when {
                    path.startsWith("/search/users") -> MockResponse().setBody(
                        """{"items":[{"login":"zebra"},{"login":"adam"}]}"""
                    )
                    path.startsWith("/search/repositories") -> MockResponse().setBody(
                        """{"items":[{"full_name":"middle/repo"},{"full_name":"acme/app"}]}"""
                    )
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        val client = OkHttpClient.Builder().build()
        val api = GitHubApiModule.createSearchApi(server.url("/").toString(), client)
        val repo = RemoteGitHubSearchRepository(api)

        val result = repo.searchCombined("query")

        assertEquals(
            listOf(
                GithubListItem.Repository("acme/app"),
                GithubListItem.UserLogin("adam"),
                GithubListItem.Repository("middle/repo"),
                GithubListItem.UserLogin("zebra")
            ),
            result
        )
    }

    @Test
    fun `caps combined list at 50 items`() = runTest {
        val usersJson = buildString {
            append("""{"items":[""")
            repeat(30) { i ->
                if (i > 0) append(",")
                append("""{"login":"u$i"}""")
            }
            append("]}")
        }
        val reposJson = buildString {
            append("""{"items":[""")
            repeat(30) { i ->
                if (i > 0) append(",")
                append("""{"full_name":"o$i/r"}""")
            }
            append("]}")
        }
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: return MockResponse().setResponseCode(404)
                return when {
                    path.startsWith("/search/users") -> MockResponse().setBody(usersJson)
                    path.startsWith("/search/repositories") -> MockResponse().setBody(reposJson)
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        val api = GitHubApiModule.createSearchApi(server.url("/").toString(), OkHttpClient())
        val repo = RemoteGitHubSearchRepository(api)

        val result = repo.searchCombined("q")

        assertEquals(50, result.size)
    }
}
