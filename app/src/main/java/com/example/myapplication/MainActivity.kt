package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var playButton: ImageView
    private lateinit var songTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var albumImageView: ImageView

    private var currentSeekBarPosition = 0
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var pathToFile: String = Environment.getExternalStorageDirectory().path + "/Music/" + "don't be gone too long (solo ver).m4a"
    val animationSet = AnimationSet(true)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyStoragePermissions()

        playButton = findViewById(R.id.playButton)
        songTextView = findViewById(R.id.songTextView)
        artistTextView = findViewById(R.id.artistTextView)
        seekBar = findViewById(R.id.seekBar)
        albumImageView = findViewById(R.id.albumImageView)

        val scaleAnimation = ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation.duration = 1000
        scaleAnimation.repeatCount = Animation.INFINITE
        scaleAnimation.repeatMode = Animation.REVERSE
        animationSet.addAnimation(scaleAnimation)


        val filename = File(pathToFile).name
        val filenameWithoutExtension = filename.substringBeforeLast(".")

        songTextView.text = "[ $filenameWithoutExtension ]";

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(pathToFile)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: null

        artistTextView.text = if (artist != null) "[ $artist ]" else ""

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(pathToFile)
        mediaPlayer?.prepareAsync()

        mediaPlayer?.setOnErrorListener { _, what, extra ->
            Log.e("MediaPlayer", "Error ($what,$extra)")
            false
        }

}

    fun toggleAudio(view: View) {
        val handler = Handler()

        if (isPlaying) {
            stopAudio()
        } else {
            playAudio()
            seekBar.max = mediaPlayer?.duration ?: 100
            mediaPlayer?.seekTo(currentSeekBarPosition) // Set the SeekBar position
            val updateSeekBarTask = object : Runnable {
                override fun run() {
                    if (mediaPlayer?.isPlaying == true) {
                        seekBar.progress = mediaPlayer?.currentPosition ?: 0
                        currentSeekBarPosition = mediaPlayer?.currentPosition ?: 0 // Store the current SeekBar position
                        handler.postDelayed(this, 100) // Update progress every 100 milliseconds
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
                mediaPlayer?.seekTo(progress)
            }
        })
    }
    private fun playAudio() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            isPlaying = true
            albumImageView.startAnimation(animationSet)
            playButton.setImageResource(R.drawable.iconmonstr_pause_thin)
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

    private fun verifyStoragePermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }
}