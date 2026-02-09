package com.example.rest

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppBlockingManager {
    private const val PREFS_NAME = "AppBlockingPrefs"
    private const val KEY_BLOCKED_APPS = "blocked_apps"

    fun getBlockedApps(context: Context): List<AppBloqueo> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_BLOCKED_APPS, null) ?: return emptyList()
        val type = object : TypeToken<List<AppBloqueo>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveBlockedApp(context: Context, app: AppBloqueo) {
        val apps = getBlockedApps(context).toMutableList()
        val index = apps.indexOfFirst { it.packageName == app.packageName }
        if (index != -1) {
            apps[index] = app
        } else {
            apps.add(app)
        }
        saveApps(context, apps)
    }

    fun removeBlockedApp(context: Context, packageName: String) {
         val apps = getBlockedApps(context).toMutableList()
         apps.removeAll { it.packageName == packageName }
         saveApps(context, apps)
    }

    private fun saveApps(context: Context, apps: List<AppBloqueo>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(apps)
        prefs.edit().putString(KEY_BLOCKED_APPS, json).apply()
    }
    
    fun isAppBlocked(context: Context, packageName: String): Boolean {
        // Here we would check if usage > limit.
        // This requires the Service to update 'usageTime' or calculating it from UsageStatsManager.
        // For simplicity, we'll let the Service do the check logic, this Manager just holds the config.
        return getBlockedApps(context).any { it.packageName == packageName && it.limitMinutes > 0 }
    }
}
