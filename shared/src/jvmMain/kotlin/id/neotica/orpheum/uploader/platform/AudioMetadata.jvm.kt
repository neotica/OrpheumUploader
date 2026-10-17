package id.neotica.orpheum.uploader.platform

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.swing.SwingUtilities
import kotlin.coroutines.resume
import kotlin.math.roundToInt

actual suspend fun readAudioDurationSeconds(file: File): Int? =
    suspendCancellableCoroutine { cont ->
        SwingUtilities.invokeLater {
            JFXPanel()
            Platform.runLater {
                try {
                    val media = Media(file.toURI().toString())
                    val player = MediaPlayer(media)

                    val finish: (Int?) -> Unit = { seconds ->
                        player.dispose()
                        if (cont.isActive) cont.resume(seconds)
                    }

                    player.setOnReady {
                        finish(media.duration?.toSeconds()?.roundToInt())
                    }
                    player.setOnError {
                        finish(null)
                    }
                    media.setOnError {
                        finish(null)
                    }
                } catch (e: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }
