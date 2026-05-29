package id.neotica.orpheum.uploader.data.local

import id.neotica.orpheum.uploader.domain.local.AudioPlayer
import javax.swing.SwingUtilities
import javafx.embed.swing.JFXPanel
import javafx.scene.media.MediaPlayer
import javafx.scene.media.Media
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DesktopAudioPlayer : AudioPlayer {

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f) // 0.0 to 1.0
    override val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    init {
        // Compose Desktop does not run JavaFX by default.
        // Instantiating JFXPanel on the Swing thread initializes the JavaFX toolkit headlessly.
        SwingUtilities.invokeLater { JFXPanel() }
    }

    override fun play(url: String) {
        stop() // Clear previous track if any

        // JavaFX requires UI thread or its own thread for initialization
        javafx.application.Platform.runLater {
            try {
                val media = Media(url)
                mediaPlayer = MediaPlayer(media).apply {

                    setOnReady {
                        play()
                        _isPlaying.update { true }
                    }

                    currentTimeProperty().addListener { _, _, newValue ->
                        val totalDuration = totalDuration.toMillis()
                        if (totalDuration > 0) {
                            _playbackProgress.update { (newValue.toMillis() / totalDuration).toFloat() }
                        }
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
}