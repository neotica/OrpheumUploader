package id.neotica.orpheum.uploader.domain.local

interface MediaSessionController {
    fun setTrack(title: String, artist: String, durationSeconds: Int)
    fun setPlaybackState(isPlaying: Boolean, progress: Float)
    fun clearTrack()
    fun setCallback(callback: MediaSessionCallback?)
}

interface MediaSessionCallback {
    fun onPlay()
    fun onPause()
    fun onStop()
    fun onSeekTo(positionMs: Long)
}

object NoOpMediaSessionController : MediaSessionController {
    override fun setTrack(title: String, artist: String, durationSeconds: Int) {}
    override fun setPlaybackState(isPlaying: Boolean, progress: Float) {}
    override fun clearTrack() {}
    override fun setCallback(callback: MediaSessionCallback?) {}
}
