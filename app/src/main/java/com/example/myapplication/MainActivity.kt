package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
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

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var pathToFile: String = Environment.getExternalStorageDirectory().path + "/Music/" + "don't be gone too long (solo ver).m4a"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyStoragePermissions()

        playButton = findViewById(R.id.playButton)
        songTextView = findViewById(R.id.songTextView)
        artistTextView = findViewById(R.id.artistTextView)
        seekBar = findViewById(R.id.seekBar)


        val filename = File(pathToFile).name
        val filenameWithoutExtension = filename.substringBeforeLast(".")

        songTextView.text = "[$filenameWithoutExtension]";

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(pathToFile)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: null

        artistTextView.text = if (artist != null) "[$artist]" else ""

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(pathToFile)
        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnErrorListener { _, what, extra ->
            Log.e("MediaPlayer", "Error ($what,$extra)")
            false
        }
}

    fun toggleAudio(view: View) {
        if (isPlaying) {
            stopAudio()
        } else {
            playAudio()
        }
    }
    private fun playAudio() {
        mediaPlayer?.start()
        isPlaying = true
        playButton.setImageResource(R.drawable.iconmonstr_pause_thin)

    }

    private fun stopAudio() {
        mediaPlayer?.stop()

        playButton.setImageResource(R.drawable.iconmonstr_play_thin)
        isPlaying = false
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