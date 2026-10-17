package id.neotica.orpheum.uploader.ui.feature.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import id.neotica.orpheum.uploader.domain.model.playlist.PlaylistRemoteModel
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import id.neotica.orpheum.uploader.ui.feature.playback.PlaybackViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistDetailUiState(
    val playlist: PlaylistRemoteModel? = null,
    val tracks: List<TrackRemoteModel> = emptyList(),
    val isLoading: Boolean = true,
    val isRemoving: Boolean = false,
    val errorMessage: String? = null
)

class PlaylistDetailViewModel(
    private val slug: String,
    private val repository: CatalogRepository,
    private val playbackViewModel: PlaybackViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistDetailUiState())
    val state: StateFlow<PlaylistDetailUiState> = _state.asStateFlow()

    val currentTrack: StateFlow<TrackRemoteModel?> = playbackViewModel.currentTrack

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getPlaylist(slug)
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            playlist = response.playlist,
                            tracks = response.tracks,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun playTrack(track: TrackRemoteModel) {
        playbackViewModel.play(track)
    }

    fun removeTrack(track: TrackRemoteModel) {
        if (_state.value.isRemoving) return

        viewModelScope.launch {
            _state.update { it.copy(isRemoving = true) }

            repository.removeTrackFromPlaylist(slug, track.id)
                .onSuccess {
                    _state.update {
                        it.copy(
                            tracks = it.tracks.filterNot { t -> t.id == track.id },
                            isRemoving = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isRemoving = false, errorMessage = error.message) }
                }
        }
    }
}
