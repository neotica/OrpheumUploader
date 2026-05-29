package id.neotica.orpheum.uploader.ui.feature.playback

import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel

data class PlaybackUiState(
    val currentTrack: TrackRemoteModel? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f
)