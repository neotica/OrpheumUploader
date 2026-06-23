package id.neotica.orpheum.uploader.data.local

import id.neotica.orpheum.uploader.domain.local.AudioPlayer
import javax.swing.SwingUtilities
import javafx.embed.swing.JFXPanel
import javafx.scene.media.MediaPlayer
import javafx.scene.media.Media
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DesktopAudioPlayer : AudioPlayer {

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    override val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        SwingUtilities.invokeLater { JFXPanel() }
    }

    override fun play(url: String) {
        stop()

        javafx.application.Platform.runLater {
            try {
                val media = Media(url)
                mediaPlayer = MediaPlayer(media).apply {
                    setOnReady {
                        play()
                        _isPlaying.update { true }
                    }
                    setOnEndOfMedia {
                        _isPlaying.update { false }
                        _playbackProgress.update { 1f }
                    }
                    setOnError {
                        println("JavaFX Audio Error: ${error.message}")
                        _isPlaying.update { false }
                    }
                }
            } catch (e: Exception) {
                println("Failed to load media: ${e.message}")
            }
        }
        startProgressPolling()
    }

    override fun pause() {
        javafx.application.Platform.runLater {
            mediaPlayer?.pause()
            _isPlaying.update { false }
        }
    }

    override fun resume() {
        javafx.application.Platform.runLater {
            mediaPlayer?.play()
            _isPlaying.update { true }
        }
    }

    override fun stop() {
        stopProgressPolling()
        javafx.application.Platform.runLater {
            mediaPlayer?.stop()
            mediaPlayer?.dispose()
            mediaPlayer = null
            _isPlaying.update { false }
            _playbackProgress.update { 0f }
        }
    }

    override fun seekTo(seconds: Int) {
        javafx.application.Platform.runLater {
            mediaPlayer?.seek(javafx.util.Duration.seconds(seconds.toDouble()))
        }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                delay(250)
                javafx.application.Platform.runLater {
                    val mp = mediaPlayer
                    if (mp != null) {
                        val total = mp.totalDuration.toMillis() // Double
                        val current = mp.currentTime.toMillis() // Double
                        if (total > 0.0) {
                            _playbackProgress.update {
                                (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }
}
