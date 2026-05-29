package id.neotica.orpheum.uploader.domain.local

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    fun play(url: String)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(seconds: Int)
    val playbackProgress: StateFlow<Float>
    val isPlaying: StateFlow<Boolean>
}