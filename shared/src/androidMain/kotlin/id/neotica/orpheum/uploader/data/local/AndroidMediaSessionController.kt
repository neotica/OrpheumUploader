package id.neotica.orpheum.uploader.data.local

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import id.neotica.orpheum.uploader.domain.local.MediaSessionCallback
import id.neotica.orpheum.uploader.domain.local.MediaSessionController

class AndroidMediaSessionController(
    private val context: Context
) : MediaSessionController {

    private val mediaSession: MediaSession = MediaSession(context, "OrpheumUploader")
    private var callback: MediaSessionCallback? = null

    private var currentTitle: String = ""
    private var currentArtist: String = ""
    private var currentDurationMs: Long = 0L

    init {
        createNotificationChannel()
        mediaSession.setFlags(
            MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
            MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession.setCallback(object : MediaSession.Callback() {
            override fun onPlay() { callback?.onPlay() }
            override fun onPause() { callback?.onPause() }
            override fun onStop() { callback?.onStop() }
            override fun onSeekTo(pos: Long) { callback?.onSeekTo(pos) }
        })
        mediaSession.isActive = true
    }

    override fun setTrack(title: String, artist: String, durationSeconds: Int) {
        currentTitle = title
        currentArtist = artist
        currentDurationMs = durationSeconds * 1000L

        val metadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, currentDurationMs)
            .build()
        mediaSession.setMetadata(metadata)
    }

    override fun setPlaybackState(isPlaying: Boolean, progress: Float) {
        val state = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        val position = (progress * currentDurationMs).toLong()

        val playbackState = PlaybackState.Builder()
            .setState(state, position, 1f)
            .setActions(
                PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_STOP or
                PlaybackState.ACTION_SEEK_TO
            )
            .build()
        mediaSession.setPlaybackState(playbackState)

        showNotification(isPlaying)
    }

    override fun clearTrack() {
        mediaSession.setMetadata(null)
        mediaSession.setPlaybackState(null)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
    }

    override fun setCallback(callback: MediaSessionCallback?) {
        this.callback = callback
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun showNotification(isPlaying: Boolean) {
        var iconId = context.resources.getIdentifier("ic_launcher", "drawable", context.packageName)
        if (iconId == 0) iconId = android.R.drawable.ic_media_play
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconId)
            .setContentTitle(currentTitle)
            .setContentText(currentArtist)
            .setOngoing(isPlaying)
            .setStyle(Notification.MediaStyle().setShowActionsInCompactView(0, 1))
            .setPriority(Notification.PRIORITY_LOW)
            .build()

        notification.extras?.putParcelable("android.media.session.MediaSession", mediaSession.sessionToken)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "orpheum_playback"
        private const val NOTIFICATION_ID = 1001
    }
}
