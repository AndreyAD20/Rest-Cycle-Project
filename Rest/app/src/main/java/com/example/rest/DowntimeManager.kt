package com.example.rest

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DowntimeManager {
    private const val PREFS_NAME = "DowntimePrefs"
    private const val PREF_KEY = "HorariosDescanso"
    private val gson = Gson()

    fun getSchedules(context: Context): MutableList<HorarioDescanso> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(PREF_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<HorarioDescanso>>() {}.type
            gson.fromJson(json, type)
        } else {
            // Default empty list or sample data if desired
            mutableListOf()
        }
    }

    fun saveSchedule(context: Context, schedule: HorarioDescanso) {
        val list = getSchedules(context)
        val index = list.indexOfFirst { it.id == schedule.id }
        if (index != -1) {
            list[index] = schedule
        } else {
            list.add(schedule)
        }
        saveList(context, list)
    }

    fun deleteSchedule(context: Context, scheduleId: Int) {
        val list = getSchedules(context)
        val updatedList = list.filter { it.id != scheduleId }
        saveList(context, updatedList)
    }

    private fun saveList(context: Context, list: List<HorarioDescanso>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY, gson.toJson(list)).apply()
    }

    fun isScheduleActive(schedule: HorarioDescanso): Boolean {
        if (!schedule.activo) return false

        val calendar = java.util.Calendar.getInstance()
        val currentDay = (calendar.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7 // Convert Sun=1..Sat=7 to Mon=0..Sun=6
        // Adjusted: Calendar.MONDAY is 2. My list is 0=L, 1=M... 6=D.
        // Calendar: SUN=1, MON=2, TUE=3, WED=4, THU=5, FRI=6, SAT=7
        // Target: MON=0, TUE=1 ... SUN=6
        // (Day + 5) % 7 -> Mon(2) -> 7%7=0. Sun(1) -> 6%7=6. Correct.
        
        if (!schedule.diasActivos[currentDay]) return false

        return isTimeInInterval(calendar, schedule.horaInicio, schedule.horaFin)
    }

    private fun isTimeInInterval(now: java.util.Calendar, startStr: String, endStr: String): Boolean {
        val startMinutes = parseMinutes(startStr)
        val endMinutes = parseMinutes(endStr)
        val currentMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)

        return if (endMinutes < startMinutes) {
            // Crosses midnight (e.g. 10PM to 6AM)
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        } else {
            // Same day (e.g. 2PM to 4PM)
            currentMinutes in startMinutes until endMinutes
        }
    }

    private fun parseMinutes(timeStr: String): Int {
        return try {
            // Format: "10:30 PM"
            val parts = timeStr.split(" ", ":")
            var h = parts[0].toInt()
            val m = parts[1].toInt()
            val ampm = parts[2]

            if (ampm == "PM" && h < 12) h += 12
            if (ampm == "AM" && h == 12) h = 0
            h * 60 + m
        } catch (e: Exception) {
            -1
        }
    }
}
