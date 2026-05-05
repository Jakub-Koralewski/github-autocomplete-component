package com.example.githubautocomplete

import com.example.githubautocomplete.data.GitHubSearchRepository
import com.example.githubautocomplete.data.model.GithubListItem
import com.example.githubautocomplete.ui.AutocompleteUiState
import com.example.githubautocomplete.ui.GithubAutocompleteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GithubAutocompleteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `short query after debounce stays on min chars hint`() = runTest(testDispatcher) {
        val repo = TrackingRepo()
        val vm = GithubAutocompleteViewModel(repo)

        val states = mutableListOf<AutocompleteUiState>()
        backgroundScope.launch {
            vm.uiState.collect { states.add(it) }
        }
        testScheduler.runCurrent()
        advanceTimeBy(400)
        testScheduler.runCurrent()
        states.clear()

        vm.onQueryChange("ab")
        advanceTimeBy(350)
        testScheduler.runCurrent()

        assertTrue(states.any { it is AutocompleteUiState.MinCharsHint })
        assertEquals(0, repo.searchCallCount)
    }

    @Test
    fun `valid query emits loading then success`() = runTest(testDispatcher) {
        val repo = object : GitHubSearchRepository {
            override suspend fun searchCombined(query: String) = listOf(
                GithubListItem.UserLogin("found")
            )
        }
        val vm = GithubAutocompleteViewModel(repo)
        val states = mutableListOf<AutocompleteUiState>()
        backgroundScope.launch {
            vm.uiState.collect { states.add(it) }
        }
        testScheduler.runCurrent()
        advanceTimeBy(400)
        testScheduler.runCurrent()
        states.clear()

        vm.onQueryChange("abc")
        advanceTimeBy(350)
        testScheduler.runCurrent()

        assertTrue(states.any { it is AutocompleteUiState.Loading })
        assertEquals(
            AutocompleteUiState.Success(listOf(GithubListItem.UserLogin("found"))),
            states.last()
        )
    }

    @Test
    fun `repository failure becomes error state`() = runTest(testDispatcher) {
        val repo = object : GitHubSearchRepository {
            override suspend fun searchCombined(query: String): List<GithubListItem> {
                throw RuntimeException("network down")
            }
        }
        val vm = GithubAutocompleteViewModel(repo)
        val states = mutableListOf<AutocompleteUiState>()
        backgroundScope.launch {
            vm.uiState.collect { states.add(it) }
        }
        testScheduler.runCurrent()
        advanceTimeBy(400)
        testScheduler.runCurrent()
        states.clear()

        vm.onQueryChange("abc")
        advanceTimeBy(350)
        testScheduler.runCurrent()

        assertEquals(
            AutocompleteUiState.Error("network down"),
            states.last()
        )
    }

    private class TrackingRepo : GitHubSearchRepository {
        var searchCallCount = 0
        override suspend fun searchCombined(query: String): List<GithubListItem> {
            searchCallCount++
            return emptyList()
        }
    }
}
