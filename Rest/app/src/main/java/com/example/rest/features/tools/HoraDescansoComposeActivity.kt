package com.example.rest.features.tools

/**
 * 📚 IMPORTACIONES
 *
 * Los "import" le dicen a Kotlin qué librerías externas vamos a usar.
 * Sin importarlas, no podemos usar sus clases y funciones.
 *
 * Grupos de imports:
 *  - android.*     → APIs nativas de Android (permisos, sistema)
 *  - androidx.*    → Librerías Jetpack de Google (Compose, Lifecycle)
 *  - com.example.* → Nuestro propio código del proyecto
 *  - kotlinx.*     → Extensiones de Kotlin (coroutines para async)
 */
import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.DowntimeManager
import com.example.rest.HorarioDescanso
import com.example.rest.UsageMonitorService
import com.example.rest.data.models.Dia
import com.example.rest.data.models.Dispositivo
import com.example.rest.data.models.DiasHorario
import com.example.rest.data.models.Horario
import com.example.rest.data.models.Medida
import com.example.rest.data.repository.HorarioRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

// ---------------------------------------------------------------------------
// 🔑 FUNCIONES DE PERMISO (top-level)
// Estas funciones viven fuera de cualquier clase para que todos los
// Composables (funciones de UI) puedan usarlas sin necesitar una instancia.
// ---------------------------------------------------------------------------

/**
 * ¿Tiene el usuario concedido el permiso de No Molestar?
 *
 * NotificationManager.isNotificationPolicyAccessGranted retorna true
 * solo si el usuario fue a Ajustes → Apps → No Molestar y lo habilitó.
 */
fun checkDNDPermission(context: Context): Boolean {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as android.app.NotificationManager
    return nm.isNotificationPolicyAccessGranted
}

/**
 * ¿Tiene el usuario concedido el permiso de reconocimiento de actividad?
 *
 * Este permiso (ACTIVITY_RECOGNITION) es necesario para la integración
 * con Sleep API. Solo se requiere en Android 10 (Q) o superior.
 * En versiones anteriores, asumimos que sí hay permiso.
 */
fun checkActivityPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true   // Android < 10: no requiere este permiso
    }
}

/**
 * Inicia (o reinicia) el UsageMonitorService.
 *
 * Desde Android 8.0 (Oreo), los servicios en segundo plano que quieren
 * correr mientras la app está en primer plano deben usar startForegroundService().
 * En versiones anteriores, se usa el startService() clásico.
 */
fun restartService(context: Context) {
    val intent = android.content.Intent(context, UsageMonitorService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)   // Requiere notificación visible
    } else {
        context.startService(intent)             // Versión clásica
    }
}

/**
 * 🔕 Activa o desactiva el modo No Molestar manualmente.
 *
 * Esta función se usa cuando el usuario toca el Switch de un horario local:
 * activa/desactiva DND de forma inmediata (sin esperar al servicio).
 *
 * INTERRUPTION_FILTER_NONE = silencio total
 * INTERRUPTION_FILTER_ALL  = modo normal (permite todo)
 */
fun activarDND(context: Context, activar: Boolean) {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as android.app.NotificationManager
    if (!nm.isNotificationPolicyAccessGranted) return   // Sin permiso, nada que hacer
    if (activar) {
        nm.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_NONE)
    } else {
        nm.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALL)
    }
}

// ---------------------------------------------------------------------------
// 🎯 ACTIVITY PRINCIPAL: HoraDescansoComposeActivity
//
// Una "Activity" es la pantalla principal de Android. Esta es la pantalla
// de "Hora de Descanso". Hereda de BaseComposeActivity, que ya configura
// el tema visual y el sistema de Jetpack Compose.
// ---------------------------------------------------------------------------

class HoraDescansoComposeActivity : BaseComposeActivity() {

    /**
     * Repositorio para comunicarse con Supabase (la base de datos en la nube).
     * Contiene funciones para crear, leer y eliminar horarios remotos.
     */
    private val horarioRepository = HorarioRepository()

    /**
     * ID del usuario actual. TODO: obtener dinámicamente de la sesión de login.
     * Por ahora está hardcodeado en 1 para pruebas.
     */
    private val idUsuarioActual = 1

    /**
     * onCreate: punto de entrada de la Activity.
     * Se ejecuta UNA VEZ cuando el usuario abre esta pantalla.
     *
     * setContent {} inicia el mundo de Jetpack Compose:
     * todo lo que está adentro es UI declarativa (describes CÓMO se ve,
     * no CÓMO dibujarlo paso a paso como en el XML tradicional).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {  // Aplica el tema de colores y tipografía de la app
                /**
                 * 📊 ESTADO DEL COMPOSABLE con remember + mutableStateOf
                 *
                 * En Jetpack Compose, el estado (datos que cambian) se guarda con
                 * "remember { mutableStateOf(...) }".
                 *
                 * - remember:       recuerda el valor entre re-composiciones (redraws de UI)
                 * - mutableStateOf: convierte el valor en observable. Cuando cambia,
                 *                   Compose redibuja automáticamente los componentes que lo usan.
                 * - by (delegado): permite escribir  horarios = X  en lugar de  horarios.value = X
                 *
                 * Piensa en estas variables como el "estado de la pantalla".
                 */

                // Lista de horarios remotos obtenidos de Supabase
                var horarios by remember { mutableStateOf<List<Horario>>(emptyList()) }

                // Listas de catálogos necesarios para crear horarios en Supabase
                var dias by remember { mutableStateOf<List<Dia>>(emptyList()) }
                var medidas by remember { mutableStateOf<List<Medida>>(emptyList()) }
                var dispositivos by remember { mutableStateOf<List<Dispositivo>>(emptyList()) }

                // Mapa: idHorario → lista de IDs de días activos (para horarios de Supabase)
                var diasPorHorario by remember { mutableStateOf<Map<Int, List<Int>>>(emptyMap()) }

                // true mientras los datos aún están cargando de Supabase
                var cargando by remember { mutableStateOf(true) }

                // Controla si el diálogo de crear/editar horario está visible
                var mostrarDialogoCrear by remember { mutableStateOf(false) }

                // Horario de Supabase que se está editando (null = no se está editando)
                var horarioAEditar by remember { mutableStateOf<Horario?>(null) }

                // Para edición de horarios locales:
                var localHorarioAEditar by remember { mutableStateOf<HorarioDescanso?>(null) }
                var localIdxAEditar by remember { mutableStateOf(-1) }    // -1 = ningún horario seleccionado

                // Lista de horarios LOCALES (guardados en SharedPreferences del dispositivo)
                var localSchedules by remember {
                    mutableStateOf(DowntimeManager.getSchedules(this@HoraDescansoComposeActivity))
                }

                /**
                 * 🔄 Helper para refrescar la lista de horarios locales en la UI.
                 *
                 * Cuando el usuario crea, edita o elimina un horario local,
                 * llamamos esta función para que la pantalla muestre los cambios.
                 * Compose detecta el cambio en localSchedules y redibuja automáticamente.
                 */
                fun recargarLocales() {
                    localSchedules = DowntimeManager.getSchedules(this@HoraDescansoComposeActivity)
                }

                /**
                 * ⏳ LaunchedEffect: ejecutar código cuando el composable aparece por primera vez.
                 *
                 * LaunchedEffect(Unit) significa: "ejecuta este bloque UNA sola vez
                 * cuando esta parte de la UI se muestra por primera vez".
                 * Unit es una contraseña constante → nunca se re-ejecuta solo.
                 *
                 * Aquí cargamos los datos de Supabase al abrir la pantalla.
                 */
                LaunchedEffect(Unit) {
                    cargarDatos { horariosData, diasData, medidasData, dispositivosData, diasHorarioMap ->
                        // Actualizamos el estado → Compose redibuja la pantalla
                        horarios = horariosData
                        dias = diasData
                        medidas = medidasData
                        dispositivos = dispositivosData
                        diasPorHorario = diasHorarioMap
                        cargando = false   // Ocultamos el indicador de carga
                    }
                }

                PantallaHorasDescanso(
                    horarios = horarios,
                    localSchedules = localSchedules,
                    dias = dias,
                    medidas = medidas,
                    diasPorHorario = diasPorHorario,
                    cargando = cargando,
                    onBackClick = { finish() },
                    onAgregarClick = {
                        horarioAEditar = null
                        localHorarioAEditar = null
                        localIdxAEditar = -1
                        mostrarDialogoCrear = true
                    },
                    onEditarHorario = { horario ->
                        horarioAEditar = horario
                        mostrarDialogoCrear = true
                    },
                    onEliminarHorario = { horario ->
                        lifecycleScope.launch {
                            eliminarHorario(horario) {
                                cargarDatos { horariosData, diasData, medidasData, dispositivosData, diasHorarioMap ->
                                    horarios = horariosData
                                    dias = diasData
                                    medidas = medidasData
                                    dispositivos = dispositivosData
                                    diasPorHorario = diasHorarioMap
                                }
                            }
                        }
                    },
                    onEliminarLocal = { idx ->
                        val list = DowntimeManager.getSchedules(this@HoraDescansoComposeActivity).toMutableList()
                        if (idx < list.size) {
                            list.removeAt(idx)
                            DowntimeManager.saveAllSchedules(this@HoraDescansoComposeActivity, list)
                            recargarLocales()
                        }
                    },
                    onEditarLocal = { idx ->
                        localIdxAEditar = idx
                        localHorarioAEditar = localSchedules.getOrNull(idx)
                        horarioAEditar = null
                        mostrarDialogoCrear = true
                    }
                )

                if (mostrarDialogoCrear) {
                    // Preparamos los datos de pre-relleno cuando EDITAMOS un horario local
                    // Si estamos CREANDO, estos strings serán "", y el diálogo usará valores por defecto
                    val preNombre = localHorarioAEditar?.nombre ?: ""
                    val preInicio = localHorarioAEditar?.horaInicio ?: ""
                    val preFin = localHorarioAEditar?.horaFin ?: ""

                    /**
                     * Diálogo modal de crear/editar horario.
                     *
                     * onDismiss: se ejecuta cuando el usuario toca fuera del diálogo o presiona Cancelar.
                     * onConfirmar: se ejecuta cuando el usuario presiona "Guardar".
                     *   Recibe: nombre, horaInicio, horaFin, diasActivos (lista de 7 booleanos)
                     */
                    DialogoCrearHorario(
                        dias = dias,
                        horarioAEditar = horarioAEditar,
                        diasPorHorarioAEditar = horarioAEditar?.id?.let { diasPorHorario[it] } ?: emptyList(),
                        preNombre = preNombre,
                        preHoraInicio = preInicio,
                        preHoraFin = preFin,
                        onDismiss = { mostrarDialogoCrear = false },
                        onConfirmar = { nombre: String, horaInicio: String, horaFin: String, diasActivosList: List<Boolean> ->
                            if (localIdxAEditar >= 0) {
                                // ✏️ MODO EDICIÓN: actualizamos el horario local existente
                                // .copy() crea una nueva instancia con sólo los campos especificados cambiados
                                // (las data classes en Kotlin son inmutables, por eso usamos copy)
                                val list = DowntimeManager.getSchedules(this@HoraDescansoComposeActivity).toMutableList()
                                if (localIdxAEditar < list.size) {
                                    list[localIdxAEditar] = list[localIdxAEditar].copy(
                                        nombre = nombre,
                                        horaInicio = horaInicio.take(5),   // Solo "HH:mm" (primeros 5 caracteres)
                                        horaFin = horaFin.take(5),
                                        diasActivos = diasActivosList
                                    )
                                    DowntimeManager.saveAllSchedules(this@HoraDescansoComposeActivity, list)
                                    restartService(this@HoraDescansoComposeActivity)  // Aplicar cambios al servicio
                                }
                                mostrarDialogoCrear = false
                                localHorarioAEditar = null
                                localIdxAEditar = -1    // Resetear índice a "ningún horario"
                                recargarLocales()
                            } else {
                                // ➕ MODO CREACIÓN: creamos un horario nuevo
                                // lifecycleScope.launch inicia una coroutine:
                                // permite hacer operaciones de red sin bloquear la UI
                                lifecycleScope.launch {
                                    val idDispositivo = dispositivos.firstOrNull()?.id ?: 0
                                    val idMedida = medidas.firstOrNull()?.id ?: 0
                                    crearHorario(nombre, horaInicio, horaFin, idMedida, idDispositivo, diasActivosList) {
                                        mostrarDialogoCrear = false
                                        recargarLocales()
                                        cargarDatos { horariosData, diasData, medidasData, dispositivosData, diasHorarioMap ->
                                            horarios = horariosData
                                            dias = diasData
                                            medidas = medidasData
                                            dispositivos = dispositivosData
                                            diasPorHorario = diasHorarioMap
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // 📡 LÓGICA DE NEGOCIO: comunicación con Supabase
    //
    // Estas funciones son "private" porque solo las usa esta Activity.
    // Usan coroutines (lifecycleScope.launch) para hacer operaciones de red
    // en segundo plano sin congelar la app.
    // ------------------------------------------------------------------

    /**
     * Carga todos los datos necesarios desde Supabase.
     *
     * El parámetro onComplete es una función lambda (callback):
     * se ejecuta cuando todos los datos ya llegaron. Esto es el patrón
     * de programación asíncrona: "haz X y cuando termines, llama a esto".
     *
     * Usamos el operador "when" (similar al switch de otros lenguajes) para
     * manejar los resultados: Success (datos ok) o Error (algo faló).
     */
    private fun cargarDatos(
        onComplete: (List<Horario>, List<Dia>, List<Medida>, List<Dispositivo>, Map<Int, List<Int>>) -> Unit
    ) {
        lifecycleScope.launch {       // Inicia una coroutine (hilo ligero)
            val dias = when (val r = horarioRepository.obtenerDias()) {
                is HorarioRepository.Result.Success -> r.data
                else -> emptyList()   // Si hay error de red, usamos lista vacía
            }
            val medidas = when (val r = horarioRepository.obtenerMedidas()) {
                is HorarioRepository.Result.Success -> r.data
                else -> emptyList()
            }
            val dispositivos = when (val r = horarioRepository.obtenerDispositivosPorUsuario(idUsuarioActual)) {
                is HorarioRepository.Result.Success -> r.data
                else -> emptyList()
            }
            // Los horarios se obtienen por dispositivo (relación 1 usuario → N dispositivos)
            val todosLosHorarios = mutableListOf<Horario>()
            dispositivos.forEach { dispositivo ->
                dispositivo.id?.let { idDisp ->         // "?.let" protege contra null
                    when (val r = horarioRepository.obtenerHorariosPorDispositivo(idDisp)) {
                        is HorarioRepository.Result.Success -> todosLosHorarios.addAll(r.data)
                        else -> {}
                    }
                }
            }
            // Construimos el mapa idHorario → lista de días (para mostrar los días en cada tarjeta)
            val diasPorHorario = mutableMapOf<Int, List<Int>>()
            todosLosHorarios.forEach { horario ->
                horario.id?.let { idHorario ->
                    when (val r = horarioRepository.obtenerDiasDeHorario(idHorario)) {
                        is HorarioRepository.Result.Success ->
                            diasPorHorario[idHorario] = r.data.mapNotNull { it.idDia }
                        else -> {}
                    }
                }
            }
            // Llamamos el callback cuando todo está listo
            onComplete(todosLosHorarios, dias, medidas, dispositivos, diasPorHorario)
        }
    }

    /**
     * 💾 CREAR UN NUEVO HORARIO
     *
     * Estrategia de doble guardado ("offline-first"):
     *   1. SIEMPRE guardamos en local primero → funciona sin internet
     *   2. Si hay dispositivo registrado, también guardamos en Supabase
     *      para sincronizar entre dispositivos.
     *
     * Si Supabase falla, el horario ya quedó guardado local → la app sigue
     * funcionando aunque no haya conexión a internet.
     *
     * @param diasActivos Lista de 7 booleanos (Lun-Dom), ya lista para guardar.
     * @param onComplete Lambda que se ejecuta cuando todo está listo.
     */
    private fun crearHorario(
        nombre: String,
        horaInicio: String,
        horaFin: String,
        idMedida: Int,
        idDispositivo: Int,
        diasActivos: List<Boolean>,
        onComplete: () -> Unit
    ) {
        lifecycleScope.launch {
            // PASO 1: Guardado local (no requiere internet)
            val localSchedule = HorarioDescanso(
                nombre = nombre,
                horaInicio = horaInicio.take(5),   // "HH:mm:ss" → "HH:mm" (solo primeros 5 chars)
                horaFin = horaFin.take(5),
                diasActivos = diasActivos,
                activo = true,                     // Nuevo horario empieza activo
                bedtimeMode = true                 // Activa DND + escala de grises
            )
            DowntimeManager.saveSchedule(this@HoraDescansoComposeActivity, localSchedule)
            // Reiniciamos el servicio para que recoja el nuevo horario de inmediato
            restartService(this@HoraDescansoComposeActivity)

            // PASO 2: Intentar sincronizar con Supabase (solo si hay dispositivo válido)
            if (idDispositivo > 0) {
                val nuevoHorario = Horario(
                    idDispositivo = idDispositivo,
                    idMedida = if (idMedida > 0) idMedida else null,
                    horaInicio = horaInicio,
                    horaFin = horaFin
                )
                when (val resultado = horarioRepository.crearHorario(nuevoHorario)) {
                    is HorarioRepository.Result.Success -> {
                        resultado.data.id?.let { idHorario ->
                            // Convertir booleanos a IDs de Supabase (1=Lun, 2=Mar, ..., 7=Dom)
                            val diasIds = diasActivos.mapIndexedNotNull { idx, activo ->
                                if (activo) idx + 1 else null
                            }
                            diasIds.forEach { idDia ->
                                horarioRepository.crearDiaHorario(DiasHorario(idHorario = idHorario, idDia = idDia))
                            }
                        }
                    }
                    is HorarioRepository.Result.Error -> {
                        // Supabase falló, pero el horario ya quedó guardado localmente
                        Toast.makeText(
                            this@HoraDescansoComposeActivity,
                            "Guardado localmente (sin sincronizar)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {}
                }
            }

            Toast.makeText(this@HoraDescansoComposeActivity, "Horario '$nombre' creado", Toast.LENGTH_SHORT).show()
            onComplete()   // Avisar a la UI que ya terminó
        }
    }

    /** Elimina un horario de Supabase por su ID. Llama onComplete si tuvo éxito. */
    private fun eliminarHorario(horario: Horario, onComplete: () -> Unit) {
        lifecycleScope.launch {
            horario.id?.let { idHorario ->
                when (val resultado = horarioRepository.eliminarHorario(idHorario)) {
                    is HorarioRepository.Result.Success -> {
                        Toast.makeText(this@HoraDescansoComposeActivity, "Horario eliminado", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
                    is HorarioRepository.Result.Error ->
                        Toast.makeText(this@HoraDescansoComposeActivity, resultado.message, Toast.LENGTH_SHORT).show()
                    else -> {}
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 📱 PANTALLA PRINCIPAL: PantallaHorasDescanso
//
// @Composable = esta función dibuja UI. Solo puede llamarse desde otros @Composable.
// @OptIn = acepta usar APIs en fase beta (ExperimentalMaterial3Api).
//
// Recibe todos sus datos y callbacks como parámetros (patrón "State Hoisting"):
// el estado vive en la Activity y baja a este composable. Así la pantalla
// es "sin estado" (stateless) y más fácil de reutilizar y testear.
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHorasDescanso(
    horarios: List<Horario>,
    localSchedules: List<HorarioDescanso>,
    dias: List<Dia>,
    medidas: List<Medida>,
    diasPorHorario: Map<Int, List<Int>>,
    cargando: Boolean,
    onBackClick: () -> Unit,
    onAgregarClick: () -> Unit,
    onEditarHorario: (Horario) -> Unit,
    onEliminarHorario: (Horario) -> Unit,
    onEliminarLocal: (Int) -> Unit,
    onEditarLocal: (Int) -> Unit
) {
    // LocalContext.current da acceso al Context de Android desde un @Composable
    val context = LocalContext.current

    // Estado local del composable para los permisos (se actualiza al volver de Ajustes)
    var hasPermissionDND by remember { mutableStateOf(checkDNDPermission(context)) }
    var hasPermissionActivity by remember { mutableStateOf(checkActivityPermission(context)) }

    /**
     * rememberLauncherForActivityResult: lanza otras Activities y recibe el resultado.
     * Aquí usamos esto para abrir la pantalla de ajustes de No Molestar.
     * El bloque { } se ejecuta cuando el usuario regresa de esa pantalla.
     */
    val dndLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        hasPermissionDND = checkDNDPermission(context)   // Re-verificar al volver
        if (hasPermissionDND) restartService(context)
    }

    val activityPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermissionActivity = isGranted
        if (isGranted) restartService(context)
    }

    // Pedir el permiso ACTIVITY_RECOGNITION al abrir la pantalla (si no lo tiene)
    LaunchedEffect(Unit) {
        if (!hasPermissionActivity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityPermLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    /**
     * DisposableEffect: ejecuta código cuando el composable aparece (Unit) y lo limpia
     * cuando desaparece (onDispose). Únicamente sirve para efectos con limpieza.
     *
     * Aquí usamos un LifecycleObserver para detectar cuando el usuario vuelve
     * de otra pantalla (ON_RESUME) y verificar si ya concedió los permisos.
     */
    DisposableEffect(Unit) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasPermissionDND = checkDNDPermission(context)
                hasPermissionActivity = checkActivityPermission(context)
            }
        }
        val activity = context as? androidx.activity.ComponentActivity
        activity?.lifecycle?.addObserver(observer)
        onDispose { activity?.lifecycle?.removeObserver(observer) }  // Limpieza al salir
    }

    // Gradiente del fondo: de Primario (arriba) a cian claro (abajo)
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Primario, Color(0xFF80DEEA)),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    /**
     * 🛠️ Scaffold: estructura estándar de Material Design.
     *
     * Scaffold organiza la pantalla automáticamente con:
     *   - topBar:              barra superior (título + botón atrás)
     *   - floatingActionButton: botón flotante (el "+" para agregar)
     *   - content:             el contenido principal (la lista de horarios)
     *
     * paddingValues: Scaffold calcula el espacio que ocupa la topBar y el FAB
     * y nos lo pasa para que no quedemos tapados por ellos.
     */
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Horas de Descanso",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Blanco
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Regresar", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Primario)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarClick,
                containerColor = Color(0xFF00BCD4),
                contentColor = Negro
            ) {
                Icon(Icons.Default.Add, "Agregar Horario")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brochaGradiente)
                .padding(paddingValues)
        ) {
            /**
             * LazyColumn: lista vertical que SOLO renderiza los elementos visibles.
             *
             * A diferencia de Column normal, LazyColumn es eficiente para listas
             * largas porque no pone todos los elementos en memoria a la vez.
             *
             * item {}          → agrega UN elemento a la lista
             * items(lista) {}  → agrega todos los elementos de una lista
             * itemsIndexed(lista) { idx, elemento } → igual pero con índice
             */
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!hasPermissionDND) {
                    item {
                        PermissionWarningCard(
                            text = "Activa el permiso 'No Molestar' para que los horarios funcionen.",
                            buttonText = "Activar permiso",
                            onClick = {
                                dndLauncher.launch(
                                    android.content.Intent(
                                        android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                                    )
                                )
                            }
                        )
                    }
                }

                if (cargando) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = Blanco) }
                    }
                }

                // ---- Horarios locales (guardados en el dispositivo) ----
                // itemsIndexed da el índice (idx) de cada elemento, necesario
                // para eliminarlo/editarlo por posición en la lista.
                if (localSchedules.isNotEmpty()) {
                    item {
                        Text(
                            "Mis horarios",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Blanco,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    itemsIndexed(localSchedules) { idx, schedule ->
                        HorarioLocalCard(
                            schedule = schedule,
                            onEliminar = { onEliminarLocal(idx) },
                            onEditar = { onEditarLocal(idx) },
                            onToggle = { nuevoActivo ->
                                // Persistimos el nuevo estado del switch en SharedPreferences
                                val list = DowntimeManager.getSchedules(context).toMutableList()
                                if (idx < list.size) {
                                    list[idx] = list[idx].copy(activo = nuevoActivo)
                                    DowntimeManager.saveAllSchedules(context, list)
                                }
                            }
                        )
                    }
                }

                // ---- Horarios de Supabase ----
                if (horarios.isNotEmpty()) {
                    items(horarios) { horario ->
                        HorarioCard(
                            horario = horario,
                            dias = dias,
                            medidas = medidas,
                            diasSeleccionados = diasPorHorario[horario.id] ?: emptyList(),
                            onEditar = { onEditarHorario(horario) },
                            onEliminar = { onEliminarHorario(horario) }
                        )
                    }
                }

                if (!cargando && horarios.isEmpty() && localSchedules.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No tienes horarios\nPresiona + para crear uno",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Blanco
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 📳 TARJETA DE HORARIO LOCAL: HorarioLocalCard
//
// Composable que muestra la información de UN horario local con:
//   - Nombre y horario
//   - Indicador "No Molestar activo" cuando está en DND
//   - Switch para activar/desactivar manualmente
//   - Botón de editar (✏️)
//   - Botón de eliminar (🗑️)
// ---------------------------------------------------------------------------
@Composable
fun HorarioLocalCard(
    schedule: HorarioDescanso,
    onEliminar: () -> Unit,
    onEditar: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var activo by remember { mutableStateOf(schedule.activo) }

    // Controla la visibilidad del diálogo de confirmación de eliminación
    var mostrarConfirmEliminar by remember { mutableStateOf(false) }

    // Diálogo de confirmación antes de eliminar
    if (mostrarConfirmEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmEliminar = false },
            title = { Text("¿Eliminar horario?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "El horario \"${schedule.nombre.ifBlank { "Horario" }}\" se eliminará permanentemente."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmEliminar = false
                        onEliminar()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Eliminar", color = Blanco) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmEliminar = false }) {
                    Text("Cancelar", color = Negro)
                }
            },
            containerColor = Blanco,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    schedule.nombre.ifBlank { "Horario" },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${schedule.horaInicio} — ${schedule.horaFin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    if (activo) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "🔕 No Molestar activo",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primario
                        )
                    }
                }
            }
            Switch(
                checked = activo,
                onCheckedChange = { nuevo ->
                    activo = nuevo
                    activarDND(context, nuevo)
                    onToggle(nuevo)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Primario,
                    checkedTrackColor = Primario.copy(alpha = 0.4f)
                )
            )
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, "Editar", tint = Primario.copy(alpha = 0.8f))
            }
            // Al tocar el basurero se muestra el diálogo de confirmación (no elimina directo)
            IconButton(onClick = { mostrarConfirmEliminar = true }) {
                Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 📄 TARJETA DE HORARIO DE SUPABASE: HorarioCard
//
// Muestra horarios sincronizados con la nube. Son los que el usuario creó
// cuando tenía conexión y quedón registrados en la base de datos remota.
// ---------------------------------------------------------------------------
@Composable
fun HorarioCard(
    horario: Horario,
    dias: List<Dia>,
    medidas: List<Medida>,
    diasSeleccionados: List<Int>,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val medida = medidas.find { it.id == horario.idMedida }

    // Controla la visibilidad del diálogo de confirmación de eliminación
    var mostrarConfirmEliminar by remember { mutableStateOf(false) }

    // Diálogo de confirmación antes de eliminar
    if (mostrarConfirmEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmEliminar = false },
            title = { Text("¿Eliminar horario?", fontWeight = FontWeight.Bold) },
            text = {
                val hora = "${horario.horaInicio?.take(5) ?: ""} - ${horario.horaFin?.take(5) ?: ""}"
                Text("El horario \"$hora\" se eliminará permanentemente.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmEliminar = false
                        onEliminar()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Eliminar", color = Blanco) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmEliminar = false }) {
                    Text("Cancelar", color = Negro)
                }
            },
            containerColor = Blanco,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${horario.horaInicio?.take(5) ?: ""} - ${horario.horaFin?.take(5) ?: ""}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Negro
                    )
                    medida?.let {
                        Text(
                            text = it.nombre ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEditar) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Primario
                        )
                    }
                    // Al tocar el basurero se muestra el diálogo de confirmación (no elimina directo)
                    IconButton(onClick = { mostrarConfirmEliminar = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                dias.forEach { dia ->
                    val isSelected = diasSeleccionados.contains(dia.id)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Primario
                                else Color.LightGray.copy(alpha = 0.3f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Primario else Color.Gray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dia.nombre?.take(1) ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) Blanco else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Diálogo crear / editar horario
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// ⏰ DIÁLOGO CREAR/EDITAR HORARIO: DialogoCrearHorario
//
// Este composable muestra un AlertDialog (ventana emergente modal) con:
//   1. Campo de nombre del horario
//   2. Selector de hora inicio (TimePicker visual en 12h AM/PM)
//   3. Selector de hora fin (TimePicker visual en 12h AM/PM)
//   4. Selector de días de la semana (7 círculos, Lu a Do)
//   5. Botón Cancelar y botón Guardar
//
// Los parámetros "pre*" sirven para pre-rellenar el diálogo al EDITAR.
// Si son "", el diálogo muestra valores por defecto (08:00 y 22:00).
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCrearHorario(
    dias: List<Dia>,
    horarioAEditar: Horario?,
    diasPorHorarioAEditar: List<Int>,
    preNombre: String = "",
    preHoraInicio: String = "",
    preHoraFin: String = "",
    onDismiss: () -> Unit,
    onConfirmar: (String, String, String, List<Boolean>) -> Unit
) {
    val context = LocalContext.current

    /**
     * Estado del formulario: nombre del horario y horas en formato "HH:mm:ss".
     *
     * Pre-relleno inteligente:
     *   - Si preNombre/preHoraInicio/preHoraFin tienen valor → estamos editando
     *   - Si están vacíos y hay horarioAEditar (Supabase) → usamos ese horario
     *   - Si están vacíos y no hay horario → usamos valores por defecto
     */
    var nombre by remember { mutableStateOf(if (preNombre.isNotEmpty()) preNombre else "") }
    var horaInicio by remember { mutableStateOf(if (preHoraInicio.isNotEmpty()) preHoraInicio else (horarioAEditar?.horaInicio ?: "08:00:00")) }
    var horaFin by remember { mutableStateOf(if (preHoraFin.isNotEmpty()) preHoraFin else (horarioAEditar?.horaFin ?: "22:00:00")) }

    // Lista de 7 booleanos para los días activos. BooleanArray(7) { false } = ninguno seleccionado al inicio.
    var diasActivos by remember {
        mutableStateOf(BooleanArray(7) { false })
    }

    // Protección anti-doble clic usando AtomicBoolean: bloqueo instantáneo y thread-safe
    // (mutableStateOf era asíncrono y llegaba tarde; AtomicBoolean.compareAndSet es inmediato)
    val clickPermitido = remember { java.util.concurrent.atomic.AtomicBoolean(true) }
    var guardando by remember { mutableStateOf(false) }

    // Helper: convierte hora de 24h a formato 12h AM/PM para mostrar al usuario
    // String.format("%d:%02d %s", h, m, ampm): %d = entero, %02d = entero con 0 a la izq.
    fun formatTo12h(time: String): String {
        return try {
            val parts = time.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            val ampm = if (h < 12) "AM" else "PM"
            val h12 = when {
                h == 0   -> 12
                h > 12   -> h - 12
                else     -> h
            }
            String.format("%d:%02d %s", h12, m, ampm)
        } catch (e: Exception) { time }
    }

    /**
     * rememberTimePickerState: estado del reloj visual de Material3.
     * Guarda qué hora tiene seleccionada el usuario en el TimePicker.
     * Al pulsar Aceptar, leemos pickerInicio.hour y pickerInicio.minute.
     */
    val inicioH = horaInicio.split(":").getOrNull(0)?.toIntOrNull() ?: 8
    val inicioM = horaInicio.split(":").getOrNull(1)?.toIntOrNull() ?: 0
    val pickerInicio = rememberTimePickerState(initialHour = inicioH, initialMinute = inicioM, is24Hour = false)
    var mostrarPickerInicio by remember { mutableStateOf(false) }  // Controla visibilidad del diálogo

    val finH = horaFin.split(":").getOrNull(0)?.toIntOrNull() ?: 22
    val finM = horaFin.split(":").getOrNull(1)?.toIntOrNull() ?: 0
    val pickerFin = rememberTimePickerState(initialHour = finH, initialMinute = finM, is24Hour = false)
    var mostrarPickerFin by remember { mutableStateOf(false) }

    // Material3 TimePicker dialogs
    if (mostrarPickerInicio) {
        AlertDialog(
            onDismissRequest = { mostrarPickerInicio = false },
            title = { Text("Hora de Inicio", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = pickerInicio)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        horaInicio = String.format("%02d:%02d:00", pickerInicio.hour, pickerInicio.minute)
                        mostrarPickerInicio = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primario),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Aceptar", color = Blanco) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPickerInicio = false }) { Text("Cancelar") }
            },
            containerColor = Blanco,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (mostrarPickerFin) {
        AlertDialog(
            onDismissRequest = { mostrarPickerFin = false },
            title = { Text("Hora de Fin", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = pickerFin)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        horaFin = String.format("%02d:%02d:00", pickerFin.hour, pickerFin.minute)
                        mostrarPickerFin = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primario),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Aceptar", color = Blanco) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPickerFin = false }) { Text("Cancelar") }
            },
            containerColor = Blanco,
            shape = RoundedCornerShape(24.dp)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (horarioAEditar == null) "Nuevo Horario de Descanso" else "Editar Horario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Advertencia DND si no está activo
                if (!checkDNDPermission(context)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFE65100))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Activa 'No Molestar' para que funcione",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                // Nombre del horario
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del horario") },
                    placeholder = { Text("Ej: Noche, Siesta...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Hora Inicio — botón que abre el reloj
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Primario.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Primario),
                    modifier = Modifier.fillMaxWidth().clickable { mostrarPickerInicio = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Primario,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Hora Inicio", style = MaterialTheme.typography.labelSmall, color = Primario)
                            Text(
                                formatTo12h(horaInicio),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Hora Fin — botón que abre el reloj
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Primario.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Primario),
                    modifier = Modifier.fillMaxWidth().clickable { mostrarPickerFin = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Primario,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Hora Fin", style = MaterialTheme.typography.labelSmall, color = Primario)
                            Text(
                                formatTo12h(horaFin),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                /**
                 * 🗓️ SELECTOR DE DÍAS (sin depender de Supabase)
                 *
                 * nombresDias está mapeado directamente al índice del array diasActivos:
                 *   nombresDias[0] = "Lu" → diasActivos[0] = (Lunes activo?)
                 *   nombresDias[6] = "Do" → diasActivos[6] = (Úlmite activo?)
                 *
                 * Cuando el usuario toca un círculo:
                 *   diasActivos.clone() → crea una COPIA del array (evita mutar el original)
                 *   it[idx] = !it[idx] → "NOT": si era true lo pone false, y viceversa
                 *
                 * Assignándolo de vuelta a diasActivos, Compose detecta el cambio y redib
                 * (BooleanArray no es observable, su referencia sí lo es).
                 */
                val nombresDias = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do")
                Text("Selecciona los días:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    nombresDias.forEachIndexed { idx, label ->   // forEachIndexed = forEach con índice
                        val isSelected = diasActivos[idx]
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Primario else Color.LightGray.copy(alpha = 0.3f))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Primario else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    // Copia el array y voltea el día tocado (true ↔ false)
                                    diasActivos = diasActivos.clone().also { it[idx] = !it[idx] }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) Blanco else Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Bloqueo anti-doble clic: compareAndSet(true→false) garantiza que
                    // solo UN hilo/toque pase, aunque el segundo llegue antes de la recomposición
                    if (!clickPermitido.compareAndSet(true, false)) return@Button

                    // Verificamos DND antes de guardar
                    val hasDND = checkDNDPermission(context)
                    if (!hasDND) {
                        // Sin permiso DND: advertir e ir a los ajustes del sistema
                        Toast.makeText(
                            context,
                            "¡Activa 'No Molestar' para que el horario funcione!",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                        )
                        context.startActivity(intent)
                        return@Button
                    }
                    if (nombre.isBlank()) {
                        Toast.makeText(context, "Ponle un nombre al horario", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!diasActivos.any { it }) {
                        Toast.makeText(context, "Selecciona al menos un día", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    // Marcar guardando para el feedback visual (spinner)
                    guardando = true
                    onConfirmar(nombre.trim(), horaInicio, horaFin, diasActivos.toList())
                },
                enabled = !guardando,
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Blanco,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar", color = Blanco)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Cancelar", color = Negro)
            }
        },
        containerColor = Blanco,
        shape = RoundedCornerShape(24.dp)
    )
}

// ---------------------------------------------------------------------------
// Tarjeta de advertencia de permisos
// ---------------------------------------------------------------------------

@Composable
fun PermissionWarningCard(text: String, buttonText: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(text, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(buttonText, color = Blanco)
                    }
                }
            }
        }
    }
}
