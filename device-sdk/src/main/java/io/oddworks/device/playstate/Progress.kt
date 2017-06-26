package io.oddworks.device.playstate

import android.content.Context
import android.content.SharedPreferences

class Progress(val context: Context) {
    fun setPosition(id: String, position: Long) : Boolean {
        return getPrefsEditor().putLong(id, position).commit()
    }

    fun getPosition(id: String) : Long {
        val position = getPrefs().getLong(id, 0L)
        return position
    }

    fun getAll() : Map<String, *> {
        return getPrefs().all
    }

    fun clear(id: String) : Boolean {
        return getPrefsEditor().remove(id).commit()
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

    companion object {
        private val PREFS_NAME = "Progress"
    }
}
