package io.oddworks.device.local

import android.content.Context
import android.content.SharedPreferences

class WatchList(val context: Context) {

    fun add(id: String, type: Type = Type.GENERAL) : Boolean {
        val prefs = getPrefs()
        val watchlist = prefs.getStringSet(type.toString(), mutableSetOf())
        watchlist.add(id)
        return prefs.edit().putStringSet(type.toString(), watchlist).commit()
    }

    fun remove(id: String, type: Type = Type.GENERAL) : Boolean {
        val prefs = getPrefs()
        val watchlist = prefs.getStringSet(type.toString(), mutableSetOf())
        watchlist.remove(id)
        return prefs.edit().putStringSet(type.toString(), watchlist).commit()
    }

    fun get(type: Type = Type.GENERAL) : Set<String> {
        return getPrefs().getStringSet(type.toString(), mutableSetOf())
    }

    fun clear(type: Type = Type.GENERAL) : Boolean {
        return getPrefsEditor().putStringSet(type.toString(), mutableSetOf()).commit()
    }

    fun clearAll() : Boolean {
        return getPrefsEditor().clear().commit()
    }

    private fun getPrefs() : SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, 0)
    }

    private fun getPrefsEditor() : SharedPreferences.Editor {
        return getPrefs().edit()
    }

    enum class Type {
        GENERAL,
        VIDEO,
        COLLECTION
    }

    companion object {
        private val PREFS_NAME = "WatchListProgress"
    }
}
