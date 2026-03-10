package com.example.rest

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Guardar tareas en SharedPreferences
 */
fun saveTasks(context: Context, tareas: List<Tarea>) {
    val sharedPreferences = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = gson.toJson(tareas)
    sharedPreferences.edit().putString("tasks_list", json).apply()
}

/**
 * Cargar tareas desde SharedPreferences
 */
fun loadTasks(context: Context): List<Tarea> {
    val sharedPreferences = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
    val json = sharedPreferences.getString("tasks_list", null) ?: return emptyList()
    val gson = Gson()
    val type = object : TypeToken<ArrayList<Tarea>>() {}.type
    val result: ArrayList<Tarea>? = gson.fromJson(json, type)
    return result ?: emptyList()
}
