package io.oddworks.device.model.common

import android.util.Log

data class OddImage (val url: String,
                     val mimeType: MimeType,
                     val width: Int,
                     val height: Int,
                     val label: String) {

    enum class MimeType(val mimeType: String, val extensions: List<String>) {
        BMP("image/bmp", listOf(".bmp")),
        GIF("image/gif", listOf(".gif")),
        JPEG("image/jpeg", listOf(".jpg")),
        PNG("image/png", listOf(".png")),
        WEBP("image/webp", listOf(".webp")),
        SVG("image/svg+xml", listOf(".svg"));

        companion object {
            fun valueFromMimeType(mimeType: String) : MimeType? {
                return when (mimeType) {
                    "image/bmp" -> BMP
                    "image/gif" -> GIF
                    "image/jpeg", "image/jpg" -> JPEG
                    "image/png" -> PNG
                    "image/webp" -> WEBP
                    "image/svg+xml" -> SVG
                    else -> {
                        Log.w(MimeType::class.java.simpleName, "Unknown MimeType $mimeType")
                        null
                    }
                }
            }
        }
    }
}
