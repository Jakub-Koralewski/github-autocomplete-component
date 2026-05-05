# GitHub users & repositories autocomplete (Android, Kotlin)

Native autocomplete (no autocomplete UI library): searches GitHub **users** and **repositories** via the public REST API, merges results, sorts alphabetically by login / full name, and shows loading, empty, error, and “type at least 3 characters” states. Input is debounced (300 ms) and each new query cancels the previous request (`flatMapLatest`).

## Run

1. Open the `github-autocomplete` folder in **Android Studio** (Giraffe+ recommended).
2. Let Gradle sync (Studio downloads the wrapper if needed).
3. Run the app on a device/emulator, or run unit tests: **Gradle** → `app` → `Tasks` → `verification` → `testDebugUnitTest`.

## Testing snippet

Meaningful coverage lives in:

- `GitHubSearchRepositoryTest` — [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver) dispatches `/search/users` and `/search/repositories` in parallel; asserts merged sort order and the 50-item cap.
- `GithubAutocompleteViewModelTest` — debounce + min-length guard + loading/success/error using fake `GitHubSearchRepository` implementations.

Example command (with Gradle wrapper present):

```bash
./gradlew :app:testDebugUnitTest
```

## Requirements checklist

| Requirement             | Implementation                                                                         |
|-------------------------|----------------------------------------------------------------------------------------|
| No autocomplete library | Custom `OutlinedTextField` + `LazyColumn`; networking via Retrofit/OkHttp only         |
| Min 3 characters        | `GithubAutocompleteViewModel.MIN_QUERY_LENGTH` + `MinCharsHint` state                  |
| Users + repos           | `GitHubSearchApi` calls `search/users` and `search/repositories`                       |
| Combined + alphabetical | `RemoteGitHubSearchRepository.searchCombined` merges then `sortedBy { it.sortKey }`    |
| Max 50 items            | `take(50)` after sort; `per_page=25` per endpoint (50 raw rows max before trim)        |
| Loading / empty / error | `AutocompleteUiState` + composable branches                                            |
| Rapid typing            | 300 ms debounce + `flatMapLatest`                                                      |
| Reusable                | `GithubAutocompleteContent(onQueryChange, state)` is stateless; inject any `ViewModel` |

