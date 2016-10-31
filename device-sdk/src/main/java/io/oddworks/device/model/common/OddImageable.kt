package io.oddworks.device.model.common

interface OddImageable {
    val images: Set<OddImage>

    fun getImagesByMimeType(mimeType: String): List<OddImage> {
        return images.filter {
            it.mimeType == mimeType
        }
    }

    fun getImagesByLabel(label: String): List<OddImage> {
        return images.filter {
            it.label == label
        }
    }

    fun getImagesByMaxWidth(width: Int): List<OddImage> {
        return images.filter {
            it.width <= width
        }
    }

    fun getImagesByMaxHeight(height: Int): List<OddImage> {
        return images.filter {
            it.height <= height
        }
    }

    fun getImagesByMinWidth(width: Int): List<OddImage> {
        return images.filter {
            it.width >= width
        }
    }

    fun getImagesByMinHeight(height: Int): List<OddImage> {
        return images.filter {
            it.height >= height
        }
    }
}
