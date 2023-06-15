package com.example.myapplication.enums

enum class MusicExtension(val extension: String) {
    MP3(".mp3"),
    WAV(".wav"),
    FLAC(".flac"),
    AAC(".aac"),
    OGG(".ogg");

    companion object {
        fun isMusicFile(fileName: String): Boolean {
            val extension = fileName.substringAfterLast('.', "")
            return values().any { it.extension.equals(extension, ignoreCase = true) }
        }
    }
}