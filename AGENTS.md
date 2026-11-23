# OrpheumUploader — Agent Guide

## Build & Run

```sh
./gradlew :shared:run                  # run desktop app
./gradlew :shared:test                  # run tests
./gradlew :android:assembleDebug        # build Android APK
```

Gradle 9.1, Kotlin 2.3.21, Compose Multiplatform 1.11.0. AGP 8.7.3.

## Modules

- **`:shared`** — KMP library (JVM + Android). All cross-platform code in `commonMain`. Desktop-only in `jvmMain`. Android-only in `androidMain`.
- **`:android`** — Android app module. Depends on `:shared`, adds `activity-compose` + `koin-android`.

## Source Sets

| Source Set | Purpose |
|---|---|
| `commonMain` | DI (`sharedModule`), domain models, data layer, most UI (Auth, Feed, Search, Playback, AlbumDetail), expect declarations |
| `jvmMain` | Desktop entrypoint (`main.kt`), platform UI actuals (`PlatformUploadView`, `AlbumDetailView`, `PlatformKeyEvents`), JavaFX audio, CIO HttpClient |
| `androidMain` | Android bindings (`AndroidTokenStorage`, `AndroidAudioPlayer`, `AndroidMediaSessionController`), OkHttp HttpClient, platform UI actuals |

### Platform-Split Pattern

Platform-only behavior is extracted as `expect`/`actual`:
- `PlatformUploadView()` — Android uses `ActivityResultContracts.GetContent` (audio + cover launchers), desktop uses AWT `FileDialog` + drag-and-drop; both delegate to `AdaptiveUploadView`
- `AlbumDetailView()` — both platforms delegate to shared `AdaptiveAlbumDetailView`; JVM adds AWT drag-and-drop + `FileDialog`, Android adds an `image/*` launcher
- `PlatformKeyEvents` (`platform/PlatformKeyEvents.kt`) — desktop keyboard shortcuts (Cmd/Ctrl+digit = tab switch, Escape = pop detail) via AWT `KeyEventDispatcher`; Android actual is a no-op
- `readAudioDurationSeconds(file)` (`platform/AudioMetadata.kt`) — JVM uses JavaFX `Media`, Android uses `MediaMetadataRetriever`; fallback `null`

## DI

- `commonMain/di/AppModules.kt` — `sharedModule` (repos, viewmodels) + `expect val platformModule`
- `jvmMain/di/JvmPlatformModule.kt` — `actual val platformModule` (CIO, DesktopTokenStorage, DesktopAudioPlayer)
- `androidMain/di/AndroidPlatformModule.kt` — `actual val platformModule` (OkHttp, AndroidTokenStorage, AndroidAudioPlayer)
- Desktop: `initializeKoin()` called in `main.kt`
- Android: `OrpheumApp` extends `Application`, init via `startKoin { androidContext(this@OrpheumApp); modules(...) }`

## Navigation

Navigation 3 (`org.jetbrains.androidx.navigation3:navigation3-ui`) mimicking the NeostoreAdmin flow:

- `App.kt` owns the token gate + `NavBackStack<Screen>(Screen.Upload)` + `NavDisplay`. No token → `AuthView` early-return (Auth is *outside* `NavDisplay`); logout clears the token and returns to Auth.
- Tabs switch via `backStack.clear() + add(target)` (never stack). `Screen.AlbumDetail(albumId)` and `Screen.PlaylistDetail(slug)` are pushed (`add`) and popped (`removeLastOrNull`).
- `MainView.kt` is the tab shell only: adaptive `BoxWithConstraints` (600.dp breakpoint) → `AppNavigationBar` (compact) / `AppNavigationRail` (expanded), static top bar, `MainContent` dispatch. `PlaybackOverlay` (MiniPlayerBar via `koinInject`) floats bottom-center on the shell, `Screen.AlbumDetail` and `Screen.PlaylistDetail`.
- Screens are `@Serializable` `sealed interface Screen : NavKey` (`Screen.kt`): `Upload`, `Search`, `Feed`, `Albums`, `AlbumDetail(albumId)`, `Playlists`, `PlaylistDetail(slug)`, `Account`. No `SavedStateConfiguration` — state save/restore not wired.
- Desktop shortcuts (JVM only): Cmd/Ctrl+1..6 → tabs, Escape → pop `AlbumDetail` / `PlaylistDetail`.

## CI/CD

- `.github/workflows/release.yml` — triggers on `v*` tags. Matrix: macOS `:shared:packageDmg`, Windows `:shared:packageMsi`, Ubuntu `:shared:packageDeb` + `:android:assembleDebug`. Creates a GitHub Release via `softprops/action-gh-release`.
- Requires repo secrets `BASE_URL` and `BASE_URL_BUCKET_PUBLIC`; use semver tags (`v1.2.0`).
- App version derives from `GITHUB_REF_NAME` (fallback `1.1.0`) in `shared/build.gradle.kts` (`packageVersion`) and `android/build.gradle.kts` (`versionName`).

## Required local config

`local.properties` (gitignored):

```
BASE_URL="http://dev.neotica.id"
BASE_URL_BUCKET_PUBLIC="http://storage.neotica.id/buckets"
```

Compiled into `id.neotica.orpheum.uploader.admin.config.BuildConfig` by `com.github.gmazzo.buildconfig`. Also settable via env vars.

## Code conventions

- Kotlin style: `official` (no wildcard imports)
- `kotlinx.serialization` with `ignoreUnknownKeys = true`, `isLenient = true`, `prettyPrint = true`
- ViewModels use `StateFlow`/`collectAsState()`

## Playback Architecture

- `PlaybackViewModel` (`ui/feature/playback/PlaybackViewModel.kt`) — shared singleton managing `AudioPlayer` + `currentTrack`. Registered in `sharedModule` via `singleOf(::PlaybackViewModel)`. Injected with `koinInject()` (not `koinViewModel()`).
- `MiniPlayerBar` (`ui/feature/playback/MiniPlayerBar.kt`) — floating bottom bar (title, artist, play/pause, progress). Rendered via `PlaybackOverlay` on the tab shell and on `Screen.AlbumDetail`. Only visible when `currentTrack != null`.
- `TrackFeedView` — compact row list (no `Card`s), click-to-play, currently-playing track highlighted with `DarkPrimary` accent. Delegates playback to `PlaybackViewModel`. Infinite scroll via `loadMore()` + refresh button (also on `AlbumFeedView` and `PlaylistsView`).
- `SearchView` — debounced (300ms, min 2 chars) track search with clear button and infinite scroll.
- `PlaylistsView` / `PlaylistDetailView` — playlists tab (paginated list + create dialog + refresh) and detail screen (tracks click-to-play, remove via trailing ✕). Backed by `PlaylistsViewModel` / `PlaylistDetailViewModel`.
- `AdaptiveAlbumDetailView` — split layout on wide screens (≥840.dp: cover left, metadata + tracklist right) and stacked on small screens; track rows are click-to-play with actions: ▲/▼ reorder, ✏️ edit (title + number dialog), 🗑️ delete (confirm; stops playback if that track was playing). Edit/reorder call `PUT /admin/tracks/{id}` and `PUT /admin/albums/{id}/tracks/reorder` — contract in `docs/backend-track-edit.md` (endpoints not yet deployed).
- `UploadViewModel` — wizard with artist/album pickers: resolve artist (searchable picker or create new) → resolve album (dropdown of the artist's albums or create new) → upload cover (new albums only; optional, non-fatal) → `updateAlbum(coverUrl)` → read duration (`readAudioDurationSeconds`) → upload track. Artist list endpoint spec in `docs/backend-artist-list.md` (not yet deployed; picker shows an error, create-new flow still works).
- Media URLs: `resolveMediaUrl(path)` (`utils/MediaUrl.kt`) resolves cover art (`Album.cover_url`, `Track.album_cover`) — absolute URLs pass through, `/media/<token>` resolves against `$BASE_URL/orpheum`, legacy root-relative paths fall back to `BASE_URL_BUCKET_PUBLIC`.
- `AudioPlayer` is a platform `single` (DesktopAudioPlayer / AndroidAudioPlayer). `PlaybackViewModel` wraps it; `TrackFeedViewModel` injects `PlaybackViewModel` (not `AudioPlayer` directly).

## Android MediaSession

- `MediaSessionController` (`domain/local/MediaSessionController.kt`) — commonMain interface with `setTrack()`, `setPlaybackState()`, `clearTrack()`, `setCallback()`. No-op default (`NoOpMediaSessionController`) registered in `jvmMain` via `JvmPlatformModule`.
- `AndroidMediaSessionController` (`androidMain/data/local/AndroidMediaSessionController.kt`) — uses `MediaSession` + `Notification.MediaStyle` to surface playback in the Android notification shade and lock-screen media controls. Registered in `AndroidPlatformModule`. Receives `Context` via `get()` (Koin `androidContext()`).
- `PlaybackViewModel` implements `MediaSessionCallback` and calls `mediaSession.setTrack()` / `setPlaybackState()` on play/pause/resume/stop. The callback routes lockscreen button presses back to the ViewModel.

## Notes

- `commonTest` exists but has no tests yet
- No lint/typecheck tasks, no pre-commit hooks
- `configuration-cache=true` — invalidate with `--no-configuration-cache`
- Coil image loader: desktop configured in `shared/jvmMain/main.kt`; Android in `MainActivity` (`setSingletonImageLoaderFactory` + `KtorNetworkFetcherFactory`, deps also declared in `android/build.gradle.kts`)
- Desktop app name: "Orpheum Uploader" (window title in `main.kt`, `packageName` in `shared/build.gradle.kts`); icons in `media/` — `orpheum.svg` (editable source, reconstructed from the Android `ic_launcher` vector), `orpheum.png` (1024 master), `orpheum.icns` (macOS), `orpheum.ico` (Windows), PNG reused for Linux
