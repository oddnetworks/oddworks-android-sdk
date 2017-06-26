package io.oddworks.device.local

import android.content.Context
import android.content.SharedPreferences

class Progress(val context: Context) {
    fun setVideoProgress(id: String, position: Long) : Boolean {
        return getPrefsEditor(VIDEO_PREFS_NAME).putLong(id, position).commit()
    }

    fun setCollectionProgress(id: String, entityId: String) : Boolean {
        val prefs = getPrefs(COLLECTION_PREFS_NAME)
        val progress = prefs.getStringSet(id, mutableSetOf())
        progress.add(entityId)
        return prefs.edit().putStringSet(id, progress).commit()
    }

    fun getVideoProgress(id: String) : Long {
        return getPrefs(VIDEO_PREFS_NAME).getLong(id, 0L)
    }

    fun getCollectionProgress(id: String) : Set<String> {
        return getPrefs(COLLECTION_PREFS_NAME).getStringSet(id, mutableSetOf())
    }

    fun getAllVideoProgress() : Map<String, *> {
        return getPrefs(VIDEO_PREFS_NAME).all
    }

    fun getAllCollectionProgress() : Map<String, *> {
        return getPrefs(COLLECTION_PREFS_NAME).all
    }

    fun clearVideoProgress(id: String) : Boolean {
        return getPrefsEditor(VIDEO_PREFS_NAME).remove(id).commit()
    }

    fun clearCollectionProgress(id: String) : Boolean {
        return getPrefsEditor(COLLECTION_PREFS_NAME).remove(id).commit()
    }

    fun clearAllVideoProgress() : Boolean {
        return getPrefsEditor(VIDEO_PREFS_NAME).clear().commit()
    }

    fun clearAllCollectionProgress() : Boolean {
        return getPrefsEditor(COLLECTION_PREFS_NAME).clear().commit()
    }

    private fun getPrefs(prefsName: String) : SharedPreferences {
        return context.getSharedPreferences(prefsName, 0)
    }

    private fun getPrefsEditor(prefsName: String) : SharedPreferences.Editor {
        return getPrefs(prefsName).edit()
    }

    companion object {
        private val VIDEO_PREFS_NAME = "VideoProgress"
        private val COLLECTION_PREFS_NAME = "CollectionProgress"
    }
}
