package id.neotica.orpheum.uploader.platform

import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual suspend fun readAudioDurationSeconds(file: File): Int? = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(file.absolutePath)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull()
            ?.let { (it / 1000).toInt() }
    } catch (e: Exception) {
        null
    } finally {
        try {
            retriever.release()
        } catch (_: Exception) {
        }
    }
}
