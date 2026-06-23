package id.neotica.orpheum.uploader.ui.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.orpheum.uploader.domain.local.AudioPlayer
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackFeedViewModel(
    private val repository: CatalogRepository,
    val audioPlayer: AudioPlayer // Injected via Koin
) : ViewModel() {

    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getNewReleases()
                .onSuccess { response ->
                    _state.update { it.copy(tracks = response.data, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun playTrack(track: TrackRemoteModel) {
        // Construct the relay URL through Ktor
        val streamUrl = "https://dev.neotica.id/orpheum/stream/${track.id}"

        _state.update { it.copy(currentlyPlayingId = track.id) }
        audioPlayer.play(streamUrl)
    }

    fun pausePlayback() {
        audioPlayer.pause()
    }

    fun resumePlayback() {
        audioPlayer.resume()
    }

    fun stopPlayback() {
        audioPlayer.stop()
        _state.update { it.copy(currentlyPlayingId = null) }
    }
}