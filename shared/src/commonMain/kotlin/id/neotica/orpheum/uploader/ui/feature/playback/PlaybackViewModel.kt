package id.neotica.orpheum.uploader.ui.feature.playback

import androidx.lifecycle.ViewModel
import id.neotica.orpheum.uploader.domain.local.AudioPlayer
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlaybackViewModel(
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _currentTrack = MutableStateFlow<TrackRemoteModel?>(null)
    val currentTrack: StateFlow<TrackRemoteModel?> = _currentTrack.asStateFlow()

    val isPlaying: StateFlow<Boolean> = audioPlayer.isPlaying
    val playbackProgress: StateFlow<Float> = audioPlayer.playbackProgress

    fun play(track: TrackRemoteModel) {
        _currentTrack.update { track }
        val streamUrl = "https://dev.neotica.id/orpheum/stream/${track.id}"
        audioPlayer.play(streamUrl)
    }

    fun pause() {
        audioPlayer.pause()
    }

    fun resume() {
        audioPlayer.resume()
    }

    fun stop() {
        audioPlayer.stop()
        _currentTrack.update { null }
    }

    fun seekTo(seconds: Int) {
        audioPlayer.seekTo(seconds)
    }
}
