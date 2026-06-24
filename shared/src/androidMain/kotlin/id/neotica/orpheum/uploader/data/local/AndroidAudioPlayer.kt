package id.neotica.orpheum.uploader.data.local

import android.media.MediaPlayer
import id.neotica.orpheum.uploader.domain.local.AudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidAudioPlayer : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    override val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    override fun play(url: String) {
        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener {
                    start()
                    _isPlaying.update { true }
                }
                setOnCompletionListener {
                    _isPlaying.update { false }
                    _playbackProgress.update { 1f }
                }
                setOnErrorListener { _, what, extra ->
                    println("Android Audio Error: $what $extra")
                    _isPlaying.update { false }
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            println("Failed to load media: ${e.message}")
        }
    }

    override fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
        _isPlaying.update { false }
    }

    override fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) it.start()
        }
        _isPlaying.update { true }
    }

    override fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        _isPlaying.update { false }
        _playbackProgress.update { 0f }
    }

    override fun seekTo(seconds: Int) {
        mediaPlayer?.seekTo(seconds * 1000)
    }
}
