package com.example.rest

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

/**
 * 🧠 CLASE PRINCIPAL: DowntimeManager
 *
 * "object" en Kotlin significa que es un SINGLETON: solo existe UNA instancia
 * de esta clase en toda la app. Es como una variable global inteligente.
 *
 * DowntimeManager hace dos cosas fundamentales:
 *   1. Guardar y cargar horarios en el almacenamiento local del dispositivo (SharedPreferences).
 *   2. Decidir si un horario debe estar ACTIVO en este momento (lógica de tiempo).
 *
 * Sin esta clase, los horarios se perderían al cerrar la app.
 */
object DowntimeManager {

    // ---------------------------------------------------------------------------
    // Constantes de configuración (valores fijos que no cambian)
    // ---------------------------------------------------------------------------

    /** Nombre del archivo de preferencias donde guardamos los horarios. */
    private const val PREFS_NAME = "downtime_prefs"

    /** Clave (key) dentro del archivo para la lista de horarios. */
    private const val KEY_SCHEDULES = "schedules_list"

    /** Clave para saber si el usuario habilitó la integración con Sleep API. */
    private const val KEY_SLEEP_API_ENABLED = "sleep_api_enabled"

    // ---------------------------------------------------------------------------
    // Acceso al almacenamiento local
    // ---------------------------------------------------------------------------

    /**
     * Abre (o crea) el archivo de preferencias del dispositivo.
     *
     * SharedPreferences es como un diccionario clave→valor guardado en el dispositivo.
     * Es simple y rápido, ideal para listas pequeñas de datos como esta.
     *
     * @param context El "contexto" de Android: da acceso al sistema del dispositivo.
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ---------------------------------------------------------------------------
    // CRUD: Crear, Leer, Actualizar y Borrar horarios
    // ---------------------------------------------------------------------------

    /**
     * 💾 GUARDAR un horario individual.
     *
     * Primero busca si ya existe un horario con el mismo ID (para actualizar).
     * Si no lo encuentra, lo agrega al final de la lista.
     * Finalmente guarda toda la lista en disco.
     *
     * @param context Contexto de Android (necesario para acceder a SharedPreferences).
     * @param schedule El objeto HorarioDescanso que queremos guardar.
     */
    fun saveSchedule(context: Context, schedule: HorarioDescanso) {
        // Cargamos la lista actual de horarios guardados
        val schedules = getSchedules(context).toMutableList()

        // Buscamos si ya existe un horario con este ID
        val index = schedules.indexOfFirst { it.id == schedule.id }

        if (index != -1) {
            // Si existe → lo reemplazamos en la misma posición
            schedules[index] = schedule
        } else {
            // Si no existe → lo agregamos al final
            schedules.add(schedule)
        }

        // Guardamos la lista completa actualizada
        saveList(context, schedules)
    }

    /**
     * 📖 LEER todos los horarios guardados.
     *
     * Lee el JSON guardado en SharedPreferences y lo convierte de vuelta
     * a una lista de objetos HorarioDescanso usando la librería Gson.
     *
     * JSON es un formato de texto para guardar datos estructurados.
     * Gson convierte: Objeto Kotlin ↔ texto JSON.
     *
     * @return Lista de horarios, o lista vacía si no hay ninguno guardado.
     */
    fun getSchedules(context: Context): List<HorarioDescanso> {
        // Leemos el texto JSON guardado. Si es null, retornamos lista vacía
        val json = getPrefs(context).getString(KEY_SCHEDULES, null) ?: return emptyList()

        // Definimos el "tipo" de dato que queremos recuperar (List<HorarioDescanso>)
        // Esto es necesario porque Gson necesita saber el tipo exacto en tiempo de ejecución
        val type = object : TypeToken<List<HorarioDescanso>>() {}.type

        return try {
            // Convertimos el texto JSON a lista de objetos Kotlin
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            // Si hay algún error al parsear el JSON, devolvemos lista vacía
            // (esto puede pasar si el formato guardado es inválido)
            emptyList()
        }
    }

    /**
     * 🗑️ ELIMINAR un horario por su ID.
     *
     * Filtra la lista para quitar el horario cuyo ID coincida.
     * "removeAll" recorre la lista y quita los elementos que cumplan la condición.
     *
     * @param scheduleId El ID del horario que queremos borrar.
     */
    fun deleteSchedule(context: Context, scheduleId: Long) {
        val schedules = getSchedules(context).toMutableList()
        schedules.removeAll { it.id == scheduleId }
        saveList(context, schedules)
    }

    /**
     * 🔄 REEMPLAZAR TODA la lista de horarios.
     *
     * Útil cuando queremos modificar varios horarios a la vez,
     * por ejemplo al borrar un elemento por índice o al reordenar.
     *
     * @param list La nueva lista completa de horarios a guardar.
     */
    fun saveAllSchedules(context: Context, list: List<HorarioDescanso>) {
        saveList(context, list)
    }

    /**
     * Función privada que hace el guardado real en disco.
     *
     * Convierte la lista de objetos a texto JSON y lo guarda en SharedPreferences.
     * "private" significa que solo puede usarse dentro de este mismo objeto.
     * ".apply()" aplica el cambio de forma asíncrona (no bloquea la app).
     */
    private fun saveList(context: Context, list: List<HorarioDescanso>) {
        val json = Gson().toJson(list)                      // Objeto → JSON
        getPrefs(context).edit()
            .putString(KEY_SCHEDULES, json)                 // Guardamos el texto
            .apply()                                        // Aplicamos el cambio
    }

    // ---------------------------------------------------------------------------
    // Configuración de Sleep API
    // ---------------------------------------------------------------------------

    /** Guarda si el usuario activó la integración con la API de sueño de Google. */
    fun setSleepApiEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SLEEP_API_ENABLED, enabled).apply()
    }

    /** Retorna true si el usuario tiene activada la integración con Sleep API. */
    fun isSleepApiEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SLEEP_API_ENABLED, false)
    }

    // ---------------------------------------------------------------------------
    // LÓGICA DE TIEMPO: ¿Está activo el horario AHORA?
    // ---------------------------------------------------------------------------

    /**
     * ⏰ ¿DEBE ESTAR ACTIVO ESTE HORARIO EN ESTE MOMENTO?
     *
     * Esta es la función más importante del sistema. El servicio en segundo plano
     * (UsageMonitorService) la llama cada 3 segundos para saber si hay que
     * activar o desactivar el modo No Molestar.
     *
     * Maneja dos casos:
     *   - Horario normal (ej. 22:00 → 23:00): inicio < fin
     *   - Horario que CRUZA medianoche (ej. 22:00 → 07:00): inicio > fin
     *
     * @param schedule El horario que queremos evaluar.
     * @return true si el horario debe estar activo ahora mismo.
     */
    fun isScheduleActive(schedule: HorarioDescanso): Boolean {

        // Primero verificamos que el usuario haya activado el horario con el switch
        if (!schedule.activo) return false

        // Obtenemos la hora actual del dispositivo
        val now = Calendar.getInstance()

        // Convertimos la hora actual a "minutos totales desde las 00:00"
        // Ejemplo: 22:30 → 22*60 + 30 = 1350 minutos
        val currentMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        // Calculamos qué índice (0-6) corresponde al día de hoy.
        // Calendar.DAY_OF_WEEK: Domingo=1, Lunes=2, ..., Sábado=7
        // Con la fórmula (valor + 5) % 7 lo convertimos a: Lunes=0, Martes=1, ..., Domingo=6
        val dayOfWeek = (now.get(Calendar.DAY_OF_WEEK) + 5) % 7

        // Convertimos las horas del horario a minutos totales para comparar
        val start = parseMinutes(schedule.horaInicio)
        val end   = parseMinutes(schedule.horaFin)

        // Si alguna hora es inválida (parseMinutes retornó -1), salimos
        if (start == -1 || end == -1) return false

        return if (start < end) {
            // ──────────────────────────────────────────────────────────
            // CASO 1: Horario normal (mismo día)
            // Ejemplo: 22:00 → 23:30
            //
            // Verificamos:
            //   a) Hoy es un día activo según diasActivos
            //   b) La hora actual está entre inicio y fin
            // ──────────────────────────────────────────────────────────
            if (!schedule.diasActivos[dayOfWeek]) return false
            currentMinute in start until end

        } else {
            // ──────────────────────────────────────────────────────────
            // CASO 2: Horario que cruza medianoche
            // Ejemplo: 22:00 → 07:00 (empieza el martes, termina el miércoles)
            //
            // Si son las 23:00 → estamos en el día de INICIO
            // Si son las 02:00 → estamos en el día de AYER (día de inicio)
            // ──────────────────────────────────────────────────────────
            if (currentMinute >= start) {
                // Todavía no llegó medianoche → checar día actual
                schedule.diasActivos[dayOfWeek]
            } else if (currentMinute < end) {
                // Ya pasó medianoche → checar el día de AYER
                val yesterday = (dayOfWeek + 6) % 7   // +6 % 7 = día anterior
                schedule.diasActivos[yesterday]
            } else {
                // Estamos fuera del rango → no activo
                false
            }
        }
    }

    /**
     * 🔢 Convierte una hora en texto a minutos totales desde las 00:00.
     *
     * Acepta formato "HH:mm" o "HH:mm:ss". Los segundos se ignoran.
     *
     * Ejemplo:
     *   "08:30" → 8*60 + 30 = 510 minutos
     *   "22:00" → 22*60 + 0 = 1320 minutos
     *
     * @return Los minutos totales, o -1 si el formato es inválido.
     */
    fun parseMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.split(":")
            val h = parts[0].toInt()     // Horas
            val m = parts[1].toInt()     // Minutos
            h * 60 + m                   // Total de minutos
        } catch (e: Exception) {
            -1   // Si el formato es incorrecto, retornamos -1 como señal de error
        }
    }
}
