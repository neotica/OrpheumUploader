package id.neotica.orpheum.uploader.ui.feature.playback

import androidx.lifecycle.ViewModel
import id.neotica.orpheum.uploader.domain.local.AudioPlayer
import id.neotica.orpheum.uploader.domain.local.MediaSessionCallback
import id.neotica.orpheum.uploader.domain.local.MediaSessionController
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlaybackViewModel(
    private val audioPlayer: AudioPlayer,
    private val mediaSession: MediaSessionController
) : ViewModel(), MediaSessionCallback {

    private val _currentTrack = MutableStateFlow<TrackRemoteModel?>(null)
    val currentTrack: StateFlow<TrackRemoteModel?> = _currentTrack.asStateFlow()

    val isPlaying: StateFlow<Boolean> = audioPlayer.isPlaying
    val playbackProgress: StateFlow<Float> = audioPlayer.playbackProgress

    init {
        mediaSession.setCallback(this)
    }

    fun play(track: TrackRemoteModel) {
        _currentTrack.update { track }
        mediaSession.setTrack(track.title, track.artistName, track.durationSeconds)
        mediaSession.setPlaybackState(true, 0f)
        val streamUrl = "https://dev.neotica.id/orpheum/stream/${track.id}"
        audioPlayer.play(streamUrl)
    }

    fun pause() {
        audioPlayer.pause()
        mediaSession.setPlaybackState(false, audioPlayer.playbackProgress.value)
    }

    fun resume() {
        audioPlayer.resume()
        mediaSession.setPlaybackState(true, audioPlayer.playbackProgress.value)
    }

    fun stop() {
        audioPlayer.stop()
        _currentTrack.update { null }
        mediaSession.clearTrack()
    }

    fun seekTo(seconds: Int) {
        audioPlayer.seekTo(seconds)
    }

    override fun onPlay() = resume()
    override fun onPause() = pause()
    override fun onStop() = stop()
    override fun onSeekTo(positionMs: Long) {
        _currentTrack.value?.let {
            seekTo((positionMs / 1000).toInt())
        }
    }
}
