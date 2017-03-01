package io.oddworks.device.model.video

import android.util.Log

class OddSource(val url: String,
                val container: String,
                val mimeType: MimeType,
                val label: String,
                val sourceType: SourceType = SourceType.VOD,
                val broadcasting: Boolean = false) {

    enum class SourceType {
        VOD,
        LINEAR,
        AUDIO,
        CAPTION
    }

    interface MimeType {
        val mimeType: String
        val extensions: List<String>

        companion object {
            fun valueFromMimeType(mimeType: String) : MimeType? {
                return when (mimeType) {
                    "audio/aac" -> AudioMimeType.AAC
                    "audio/mp4" -> AudioMimeType.MP4
                    "audio/mpeg" -> AudioMimeType.MPEG
                    "audio/ogg" -> AudioMimeType.OGG
                    "audio/wav" -> AudioMimeType.WAV
                    "audio/webm" -> AudioMimeType.WEBM
                    "application/x-mpegURL" -> VideoMimeType.HLS
                    "application/dash+xml" -> VideoMimeType.DASH
                    "video/mp4" -> VideoMimeType.MP4
                    "video/ogg" -> VideoMimeType.OGG
                    "video/webm" -> VideoMimeType.WEBM
                    "video/mkv" -> VideoMimeType.MKV
                    "text/vtt" -> CaptionMimeType.VTT
                    else -> {
                        Log.w(MimeType::class.java.simpleName, "Unknown MimeType $mimeType")
                        null
                    }
                }
            }
        }
    }

    enum class AudioMimeType(override val mimeType: String, override val extensions: List<String>) : MimeType {
        AAC("audio/aac", listOf(".aac")),
        MP4("audio/mp4", listOf(".mp4", ".m4a")),
        MPEG("audio/mpeg", listOf(".mp1", ".mp2", ".mp3", ".mpg", ".mpeg")),
        OGG("audio/ogg", listOf(".oga", ".ogg")),
        WAV("audio/wav", listOf(".wav")),
        WEBM("audio/webm", listOf(".webm"))
    }

    enum class VideoMimeType(override val mimeType: String, override val extensions: List<String>) : MimeType {
        HLS("application/x-mpegURL", listOf(".m3u8")),
        DASH("application/dash+xml", listOf(".mpd")),
        MP4("video/mp4", listOf(".mp4", ".m4v")),
        OGG("video/ogg", listOf(".ogv")),
        WEBM("video/webm", listOf(".webm")),
        MKV("video/mkv", listOf(".mkv"))
    }

    enum class CaptionMimeType(override val mimeType: String, override val extensions: List<String>) : MimeType {
        VTT("text/vtt", listOf(".vtt"))
    }
}
