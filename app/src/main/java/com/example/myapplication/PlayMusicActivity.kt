package com.example.myapplication

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.enums.MusicExtension
import java.io.File

class PlayMusicActivity : AppCompatActivity() {
    private var pathToFile: String = ""

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var isMusicEnded = false

    private lateinit var randomIconButton: ImageView
    private lateinit var previousIconButton: ImageView
    private lateinit var playButton: ImageView
    private lateinit var nextIconButton: ImageView
    private lateinit var loopIconButton: ImageView

    private lateinit var songTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var albumImageView: ImageView
    private lateinit var lengthTextView: TextView
    private lateinit var durationTextView: TextView

    private var maxDuration = 0L
    private var currentPlayingPosition: Int = 0
    private var currentSeekBarPosition = 0

    private val animationSet = AnimationSet(true)

    private var musicFileCount = 0
    private lateinit var musicFiles: List<File>

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_player)

        this.pathToFile = intent.getStringExtra("filePath") ?: ""

        playButton = findViewById(R.id.playButton)
        songTextView = findViewById(R.id.songTextView)
        artistTextView = findViewById(R.id.artistTextView)
        seekBar = findViewById(R.id.seekBar)
        albumImageView = findViewById(R.id.albumImageView)
        lengthTextView = findViewById(R.id.length)
        durationTextView = findViewById(R.id.duration)

        nextIconButton = findViewById(R.id.nextIcon)
        nextIconButton.setOnClickListener { onRightButtonClick(it) }

        previousIconButton = findViewById(R.id.previousIcon)
        previousIconButton.setOnClickListener {onLeftButtonClick(it)}

        randomIconButton = findViewById(R.id.randomIcon)

        loopIconButton = findViewById(R.id.loopIcon)
        loopIconButton.setOnClickListener {
            toggleLooping()
        }

        val musicFiles = retrieveMusicFiles()
        musicFileCount = musicFiles.size

        updateImageViewState()

        val scaleAnimation = ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation.duration = 1000
        scaleAnimation.repeatCount = Animation.INFINITE
        scaleAnimation.repeatMode = Animation.REVERSE
        animationSet.addAnimation(scaleAnimation)

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(pathToFile)

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(pathToFile)

        retriever.setDataSource(pathToFile)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

        val duration = durationString?.toLongOrNull() ?: 0
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        val durationText = String.format("%d:%02d", minutes, seconds)
        lengthTextView.text = durationText

        mediaPlayer?.prepareAsync()

        mediaPlayer?.setOnErrorListener { _, what, extra ->
            Log.e("MediaPlayer", "Error ($what,$extra)")
            false
        }

        updateUIForCurrentSong()
    }

    private fun updateUIForCurrentSong() {
        val currentSongFile = File(pathToFile)
        
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(currentSongFile.path)

        maxDuration = mediaPlayer?.duration?.toLong() ?: 0L
        val maxMinutes = (maxDuration / 1000) / 60
        val maxSeconds = (maxDuration / 1000) % 60
        val maxDurationText = String.format("%d:%02d", maxMinutes, maxSeconds)
        lengthTextView.text = maxDurationText

        val filenameWithoutExtension = currentSongFile.nameWithoutExtension
        songTextView.text = "[ $filenameWithoutExtension ]"

        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        artistTextView.text = if (artist != null) "[ $artist ]" else ""

        retriever.release()
    }

    fun toggleAudio(view: View) {
        val handler = Handler()

        if (isPlaying) {
            stopAudio()
        } else {
            playAudio()
            seekBar.max = mediaPlayer?.duration ?: 100
            mediaPlayer?.seekTo(currentSeekBarPosition)
            val updateSeekBarTask = object : Runnable {
                override fun run() {
                    if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                        seekBar.progress = mediaPlayer?.currentPosition ?: 0
                        currentSeekBarPosition = mediaPlayer?.currentPosition ?: 0

                        val minutes = (currentSeekBarPosition / 1000) / 60
                        val seconds = (currentSeekBarPosition / 1000) % 60
                        val currentDurationText = String.format("%d:%02d", minutes, seconds)
                        durationTextView.text = currentDurationText

                        handler.postDelayed(this, 100)
                    } else if (mediaPlayer != null && mediaPlayer?.isLooping == true) {
                        // Handle looping when the music has ended
                        seekBar.progress = 0
                        currentSeekBarPosition = 0

                        val minutes = (currentSeekBarPosition / 1000) / 60
                        val seconds = (currentSeekBarPosition / 1000) % 60
                        val currentDurationText = String.format("%d:%02d", minutes, seconds)
                        durationTextView.text = currentDurationText

                        handler.postDelayed(this, 100)
                    }
                }
            }
            handler.postDelayed(updateSeekBarTask, 100)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: 0
                val duration = mediaPlayer?.duration ?: 0

                if (progress <= duration) {
                    mediaPlayer?.seekTo(progress)

                    if (isMusicEnded) {
                        playAudio()
                    }
                } else {
                    Log.e("MediaPlayer", "Attempted to seek beyond the end of the file")
                }
            }
        })
    }
    private fun playAudio() {
        if (mediaPlayer?.isPlaying == false || isMusicEnded) {
            mediaPlayer?.start()
            isPlaying = true
            albumImageView.startAnimation(animationSet)
            playButton.setImageResource(R.drawable.iconmonstr_pause_thin)
            isMusicEnded = false

            mediaPlayer?.setOnCompletionListener {
                albumImageView.clearAnimation()
                isPlaying = false
                playButton.setImageResource(R.drawable.iconmonstr_play_thin)
                isMusicEnded = true

            }
        }
    }
    private fun stopAudio() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            albumImageView.clearAnimation()
            seekBar.progress = mediaPlayer?.currentPosition ?: 0
            currentSeekBarPosition = mediaPlayer?.currentPosition ?: 0 // Store the current SeekBar position
            isPlaying = false
            playButton.setImageResource(R.drawable.iconmonstr_play_thin)
        }
    }

    private fun retrieveMusicFiles(): List<File> {
        val musicFiles = mutableListOf<File>()

        val directory = File(pathToFile).parentFile

        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    val fileName = file.name
                    val isFolder = file.isDirectory

                    if (!isFolder && MusicExtension.isMusicFile(fileName)) {
                        musicFiles.add(file)
                    }
                }
            }
        }

        this.musicFiles = musicFiles
        return musicFiles
    }


    private fun onRightButtonClick(view: View) {
        currentPlayingPosition++

        if (currentPlayingPosition >= musicFiles.size) {
            currentPlayingPosition = 0
        }

        val nextSongFile = musicFiles[currentPlayingPosition]
        if (nextSongFile.exists()) {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(nextSongFile.path)
            mediaPlayer?.setOnPreparedListener { mp ->
                mp.start()
                isPlaying = true
                albumImageView.startAnimation(animationSet)
                playButton.setImageResource(R.drawable.iconmonstr_pause_thin)
                isMusicEnded = false

                pathToFile = nextSongFile.path

                updateUIForCurrentSong()
                seekBar.max = mediaPlayer?.duration ?: 0

                val handler = Handler()
                val updateSeekBarTask = object : Runnable {
                    override fun run() {
                        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                            seekBar.progress = mediaPlayer?.currentPosition ?: 0

                            val minutes = (seekBar.progress / 1000) / 60
                            val seconds = (seekBar.progress / 1000) % 60
                            val currentDurationText = String.format("%d:%02d", minutes, seconds)

                            durationTextView.text = currentDurationText

                            handler.postDelayed(this, 100)
                        }
                    }
                }
                handler.postDelayed(updateSeekBarTask, 100)
            }
            mediaPlayer?.setOnErrorListener { mp, what, extra ->
                Log.e("MediaPlayer", "Error ($what, $extra)")
                false
            }
            mediaPlayer?.prepareAsync()
            pathToFile = nextSongFile.absolutePath
        } else {
        }
    }
    private fun onLeftButtonClick(view: View) {
        currentPlayingPosition--

        if (currentPlayingPosition < 0) {
            currentPlayingPosition = musicFiles.size - 1
        }

        val previousSongFile = musicFiles[currentPlayingPosition]
        if (previousSongFile.exists()) {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(previousSongFile.path)
            mediaPlayer?.setOnPreparedListener { mp ->
                mp.start()
                isPlaying = true
                albumImageView.startAnimation(animationSet)
                playButton.setImageResource(R.drawable.iconmonstr_pause_thin)
                isMusicEnded = false

                pathToFile = previousSongFile.path

                updateUIForCurrentSong()
                seekBar.max = mediaPlayer?.duration ?: 0

                val handler = Handler()
                val updateSeekBarTask = object : Runnable {
                    override fun run() {
                        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                            seekBar.progress = mediaPlayer?.currentPosition ?: 0

                            val minutes = (seekBar.progress / 1000) / 60
                            val seconds = (seekBar.progress / 1000) % 60
                            val currentDurationText = String.format("%d:%02d", minutes, seconds)

                            durationTextView.text = currentDurationText

                            handler.postDelayed(this, 100)
                        }
                    }
                }
                handler.postDelayed(updateSeekBarTask, 100)
            }
            mediaPlayer?.setOnErrorListener { mp, what, extra ->
                Log.e("MediaPlayer", "Error ($what, $extra)")
                false
            }
            mediaPlayer?.prepareAsync()
            pathToFile = previousSongFile.absolutePath
        } else {
        }
    }

    private fun toggleLooping() {
        val isLooping = mediaPlayer?.isLooping ?: false

        if (isLooping) {
            mediaPlayer?.isLooping = false
            val loopIcon = resources.getDrawable(R.drawable.iconmonstr_loop_thin)
            loopIcon.setTint(resources.getColor(R.color.grey))
            loopIconButton.setImageDrawable(loopIcon)

        } else {
            mediaPlayer?.isLooping = true
            val loopIcon = resources.getDrawable(R.drawable.iconmonstr_loop_thin)
            loopIcon.setTint(resources.getColor(R.color.white))
            loopIconButton.setImageDrawable(loopIcon)

            if (isMusicEnded) {
                isMusicEnded = false
                playAudio()
            }
        }
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateImageViewState() {
        val nextIcon = resources.getDrawable(R.drawable.iconmonstr_next_thin)
        val previousIcon = resources.getDrawable(R.drawable.iconmonstr_previous_thin)
        val randomIcon = resources.getDrawable(R.drawable.iconmonstr_random_thin)

        if (musicFileCount <= 1) {
            nextIcon.setTint(resources.getColor(R.color.grey))
            nextIconButton.setImageDrawable(nextIcon)
            nextIconButton.isEnabled = false
            nextIconButton.isClickable = false

            previousIcon.setTint(resources.getColor(R.color.grey))
            previousIconButton.setImageDrawable(previousIcon)
            previousIconButton.isEnabled = false
            previousIconButton.isClickable = false

            randomIcon.setTint(resources.getColor(R.color.grey))
            randomIconButton.setImageDrawable(randomIcon)
            randomIconButton.isEnabled = false
            randomIconButton.isClickable = false
        } else {
            nextIcon.setTint(resources.getColor(R.color.white))
            nextIconButton.setImageDrawable(nextIcon)
            nextIconButton.isEnabled = true
            nextIconButton.isClickable = true

            previousIcon.setTint(resources.getColor(R.color.white))
            previousIconButton.setImageDrawable(previousIcon)
            previousIconButton.isEnabled = true
            previousIconButton.isClickable = true

            randomIcon.setTint(resources.getColor(R.color.white))
            randomIconButton.setImageDrawable(randomIcon)
            randomIconButton.isEnabled = true
            randomIconButton.isClickable = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}