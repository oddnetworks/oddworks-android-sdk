package io.oddworks.device.model.video

interface OddSourceable {
    val sources: Set<OddSource>

    fun getSourcesByContainer(container: String) : List<OddSource> {
        return sources.filter { it.container.contentEquals(container) }
    }

    fun getSourcesByContainer(container: Regex) : List<OddSource> {
        return sources.filter { it.container.matches(container) }
    }

    fun getVideoSources() : List<OddSource> {
        return sources.filter { it.mimeType is OddSource.VideoMimeType }
    }

    fun getAudioSources() : List<OddSource> {
        return sources.filter { it.mimeType is OddSource.AudioMimeType }
    }

    fun getCaptionSources() : List<OddSource> {
        return sources.filter { it.mimeType is OddSource.CaptionMimeType }
    }

    fun getSourcesByMimeType(mimeType: OddSource.MimeType) : List<OddSource> {
        return sources.filter { it.mimeType == mimeType }
    }

    fun getSourcesByLabel(label: String) : List<OddSource> {
        return sources.filter { it.label.contentEquals(label) }
    }

    fun getSourcesByLabel(label: Regex) : List<OddSource> {
        return sources.filter { it.label.matches(label) }
    }

    fun getSourcesBySourceType(sourceType: OddSource.SourceType) : List<OddSource> {
        return sources.filter { it.sourceType == sourceType }
    }

    fun getSourcesByBroadcasting(isBroadcasting: Boolean) : List<OddSource> {
        return sources.filter { it.broadcasting == isBroadcasting }
    }
}