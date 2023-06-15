package com.example.myapplication.enums

enum class MusicExtension(val extension: String) {
    MP3(".mp3"),
    WAV(".wav"),
    FLAC(".flac"),
    AAC(".aac"),
    OGG(".ogg"),
    MPEG(".mpeg"),
    M4A(".m4a"),
    MP4(".mp4");

    companion object {
        fun isMusicFile(fileName: String): Boolean {
            val extension = fileName.substringAfterLast('.', "")
            return values().any { it.extension.equals(extension, ignoreCase = true) }
        }
    }
}