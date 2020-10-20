package com.example.ai_sangeet.songservice


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.example.ai_sangeet.MainActivity
import com.example.ai_sangeet.R



class MusicService : MediaBrowserServiceCompat(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {
    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var metadataBuilder: MediaMetadataCompat.Builder
    private lateinit var afChangeListener: AudioManager.OnAudioFocusChangeListener
    private lateinit var service: MediaBrowserService
    private lateinit var player: MediaPlayer
    private lateinit var audioFocusRequest: AudioFocusRequest

    private val songList = mutableListOf<MediaBrowserCompat.MediaItem>()
    private lateinit var currentSongId: String

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.SIZE
    )

    override fun onCreate() {
        super.onCreate()

        player = MediaPlayer()
        player.apply {
            setOnPreparedListener(this@MusicService)
            setOnCompletionListener(this@MusicService)
        }

        val channel =
            NotificationChannel("1", "music-notification", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "session").apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                            or PlaybackStateCompat.ACTION_PAUSE
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            or PlaybackStateCompat.ACTION_STOP
                )
            setPlaybackState(stateBuilder.build())

            metadataBuilder = MediaMetadataCompat.Builder()

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    Log.d("call", "play")
                    startService(Intent(applicationContext, MusicService::class.java))
                    isActive = true
                    player.start()
                    setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1.0F
                        ).build()

                    )
                }

                override fun onStop() {
                    service.stopSelf()
                    isActive = false
                    player.stop()
                    setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_STOPPED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1.0F
                        ).build()
                    )
                    stopForeground(true)
                }

                override fun onPause() {
                    Log.d("call", "pause")
                    player.pause()
                    setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_PAUSED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1.0F
                        ).build()
                    )
                    stopForeground(false)
                }

                override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
                    startService(Intent(applicationContext, MusicService::class.java))
                    isActive = true
                    player.apply {
                        stop()
                        reset()
                        setDataSource(
                            this@MusicService,
                            Uri.parse(extras.getString(MediaMetadata.METADATA_KEY_MEDIA_URI))
                        )
                        prepareAsync()
                    }
                    currentSongId = mediaId
                    setMetadata(bundleToMetadata(extras))
                    setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1.0F
                        ).build()
                    )
                }

                override fun onSkipToNext() {
                    Log.d("call", "next")
                    val song = getNextSong(songList, currentSongId)
                    currentSongId = song?.description?.mediaId.toString()
                    setMetadata(song?.description?.extras?.let { bundleToMetadata(it) })
                    player.apply {
                        stop()
                        reset()
                        setDataSource(this@MusicService, song?.description?.mediaUri!!)
                        prepareAsync()
                    }
                    setMetadata(song?.description?.extras?.let { bundleToMetadata(it) })
                    setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1.0F
                        ).build()
                    )
                }
            })
            setMediaButtonReceiver(null)
            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }
        getSongs()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onPrepared(mp: MediaPlayer?) {
        player.start()
        startForeground(2, buildNotification().build())
    }

    override fun onCompletion(mp: MediaPlayer?) {
        val song = getNextSong(songList, currentSongId)
        currentSongId = song?.description?.mediaId.toString()
        mediaSession?.setMetadata(song?.description?.extras?.let { bundleToMetadata(it) })
        player.apply {
            stop()
            reset()
            setDataSource(this@MusicService, song?.description?.mediaUri!!)
            prepareAsync()
        }
        mediaSession?.setMetadata(song?.description?.extras?.let { bundleToMetadata(it) })
        mediaSession?.setPlaybackState(
            stateBuilder.setState(
                PlaybackStateCompat.STATE_PLAYING,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1.0F
            ).build()
        )
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(
        parentMediaId: String,
        result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(songList)
    }

    private fun bundleToMetadata(bundle: Bundle): MediaMetadataCompat {
        return metadataBuilder
            .putString(
                MediaMetadata.METADATA_KEY_MEDIA_ID,
                bundle.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)
            )
            .putString(
                MediaMetadata.METADATA_KEY_MEDIA_URI,
                bundle.getString(MediaMetadata.METADATA_KEY_MEDIA_URI)
            )
            .putString(
                MediaMetadata.METADATA_KEY_TITLE,
                bundle.getString(MediaMetadata.METADATA_KEY_TITLE)
            )
            .putString(
                MediaMetadata.METADATA_KEY_ARTIST,
                bundle.getString(MediaMetadata.METADATA_KEY_ARTIST)
            )
            .putString(
                MediaMetadata.METADATA_KEY_ALBUM,
                bundle.getString(MediaMetadata.METADATA_KEY_ALBUM)
            )
            .putString(
                MediaMetadata.METADATA_KEY_ART_URI,
                bundle.getString(MediaMetadata.METADATA_KEY_ART_URI)
            )
            .putLong(
                MediaMetadata.METADATA_KEY_DURATION,
                bundle.getInt(MediaMetadata.METADATA_KEY_DURATION).toLong()
            ).build()
    }

    private fun getSongById(
        songList: List<MediaBrowserCompat.MediaItem>,
        mediaId: String
    ): MediaBrowserCompat.MediaItem? {
        return songList.find { it.mediaId == mediaId }
    }

    private fun getNextSong(
        songList: List<MediaBrowserCompat.MediaItem>,
        mediaId: String
    ): MediaBrowserCompat.MediaItem? {
        val index = songList.indexOfFirst { it.mediaId == mediaId }
        return songList[index + 1]
    }

    private fun buildNotification(): NotificationCompat.Builder {
        val controller = mediaSession?.controller
        val metadata = controller?.metadata?.bundle
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }
        return NotificationCompat.Builder(this, "1")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentTitle(metadata?.getString(MediaMetadata.METADATA_KEY_TITLE))
            .setContentText(metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_pause,
                    "pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_PAUSE
                    )
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_play,
                    "pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_PLAY
                    )
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_stop,
                    "stop",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_STOP
                    )

                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_next,
                    "stop",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0)
            )
    }

    private fun getSongs() {
        val query = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, null, emptyArray(), MediaStore.Audio.AudioColumns.TITLE + " ASC"
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)

            val titleColumn =  cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn =  cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn =  cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)

                val uri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val art: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

//                val bitmap : Bitmap = Glide
//                    .with(this)
//                    .asBitmap()
//                    .load(art)
//                    .into(100, 100)
//                    .get();

                val extras = Bundle()
                extras.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, id.toString())
                extras.putString(MediaMetadata.METADATA_KEY_MEDIA_URI, uri.toString())
                extras.putString(MediaMetadata.METADATA_KEY_TITLE, title)
                extras.putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                extras.putString(MediaMetadata.METADATA_KEY_ALBUM, album)
                extras.putString(MediaMetadata.METADATA_KEY_ART_URI, art.toString())
                extras.putInt(MediaMetadata.METADATA_KEY_DURATION, duration)
                extras.putInt("SIZE", size)

                val desc = MediaDescriptionCompat.Builder()
                    .setTitle(title)
                    .setSubtitle(artist)
                    .setDescription(album)
                    .setMediaId(id.toString())
                    .setMediaUri(uri)
                    .setIconUri(art)
                    //.setIconBitmap(bitmap)
                    .setExtras(extras)
                    .build()

                songList.add(
                    MediaBrowserCompat.MediaItem(
                        desc,
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                    )
                )
            }
        }
    }
}
