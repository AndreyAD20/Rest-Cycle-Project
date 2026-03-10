package com.example.rest.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.PathInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.rest.R
import com.example.rest.data.AppNotification
import com.example.rest.data.NotificationRepository

/**
 * Servicio Overlay tipo Messenger — Sistema NotificationPROM.
 *
 * Comportamiento:
 * - Al llegar notificación: burbuja + toast rojo a la DERECHA con slide-in 300ms
 * - Toast: ícono categoría + título (negrita) + subtítulo (mensaje breve), 5 segundos
 * - Notificaciones consecutivas: se encolan y muestran secuencialmente
 * - Tap mientras toast visible: re-muestra la más reciente
 * - Tap: navega al origen, oculta toast, burbuja permanece
 */
class ChatHeadOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var chatHeadView: View? = null
    private var closeView: View? = null
    private var tooltipView: View? = null

    private lateinit var closeParams: WindowManager.LayoutParams
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private var tooltipParams: WindowManager.LayoutParams? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tooltipVisible = false

    /** Cola de notificaciones que llegaron mientras el toast ya estaba activo */
    private val notificationQueue = ArrayDeque<AppNotification>()

    private val cubicInterpolator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            PathInterpolator(0.4f, 0f, 0.2f, 1f)
        else null
    }

    private val hideTooltipRunnable = Runnable { ocultarTooltip() }

    companion object {
        const val CHANNEL_ID      = "chathead_overlay_service_channel"
        const val NOTIFICATION_ID = 4001
        const val TOAST_DURATION  = 5000L
        const val ANIM_DURATION   = 300L
        const val SLIDE_OFFSET    = 300f
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ──────────────────────────────────────────────────────────────────────────
    // onCreate
    // ──────────────────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        // ── 1. Burbuja principal ─────────────────────────────────────────────
        chatHeadView = LayoutInflater.from(this).inflate(R.layout.layout_chat_head, null)
        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 0; y = 200 }
        windowManager.addView(chatHeadView, bubbleParams)

        // ── 1.5 Badge Observer ───────────────────────────────────────────────
        val badgeTv = chatHeadView?.findViewById<TextView>(R.id.chat_head_badge_tv)
        NotificationRepository.setBadgeObserver { count ->
            mainHandler.post {
                badgeTv?.visibility = if (count > 0) View.VISIBLE else View.GONE
                badgeTv?.text = if (count > 99) "99+" else count.toString()
            }
        }

        // ── 1.6 Auto-Toast Observer ──────────────────────────────────────────
        NotificationRepository.setNewNotificationObserver { newNotif ->
            mainHandler.post {
                if (tooltipVisible) {
                    // Toast ya activo → encolar para mostrar después
                    notificationQueue.addLast(newNotif)
                } else {
                    mostrarTooltipConNotif(newNotif)
                }
            }
        }

        // ── 2. Zona de cierre "X" ────────────────────────────────────────────
        closeView = LayoutInflater.from(this).inflate(R.layout.layout_chat_close, null)
        closeParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; x = 0; y = 100 }
        closeView?.visibility = View.GONE
        windowManager.addView(closeView, closeParams)

        // ── 3. Toast flotante ────────────────────────────────────────────────
        tooltipView = LayoutInflater.from(this).inflate(R.layout.layout_tooltip_burbuja, null)
        tooltipParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bubbleParams.x + 160
            y = bubbleParams.y + 20
        }
        tooltipView?.visibility = View.GONE
        windowManager.addView(tooltipView, tooltipParams!!)

        // ── 4. Físicas ───────────────────────────────────────────────────────
        configurarFisicas()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Arrastre y click
    // ──────────────────────────────────────────────────────────────────────────

    private fun configurarFisicas() {
        val chatIcon = chatHeadView?.findViewById<ImageView>(R.id.chat_head_profile_iv)
        var iX = 0; var iY = 0; var iTX = 0f; var iTY = 0f
        val sw = resources.displayMetrics.widthPixels
        val sh = resources.displayMetrics.heightPixels

        chatIcon?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    iX = bubbleParams.x; iY = bubbleParams.y
                    iTX = event.rawX; iTY = event.rawY
                    closeView?.visibility = View.VISIBLE
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    bubbleParams.x = iX + (event.rawX - iTX).toInt()
                    bubbleParams.y = iY + (event.rawY - iTY).toInt()
                    windowManager.updateViewLayout(chatHeadView, bubbleParams)
                    if (tooltipVisible) moverTooltipJuntoBurbuja()
                    val sobreX = isOverCloseView(bubbleParams.x, bubbleParams.y, sw, sh)
                    closeView?.scaleX = if (sobreX) 1.2f else 1.0f
                    closeView?.scaleY = if (sobreX) 1.2f else 1.0f
                    true
                }
                MotionEvent.ACTION_UP -> {
                    closeView?.visibility = View.GONE
                    closeView?.scaleX = 1.0f; closeView?.scaleY = 1.0f

                    if (isOverCloseView(bubbleParams.x, bubbleParams.y, sw, sh)) {
                        stopSelf(); return@setOnTouchListener true
                    }

                    val dX = event.rawX - iTX; val dY = event.rawY - iTY
                    if (kotlin.math.abs(dX) < 15 && kotlin.math.abs(dY) < 15) {
                        onBubbleClick()
                    } else {
                        // Snap-to-edge
                        val vw = chatHeadView?.width ?: 150
                        bubbleParams.x = if (bubbleParams.x + vw / 2 < sw / 2) 0 else sw - vw
                        windowManager.updateViewLayout(chatHeadView, bubbleParams)
                        if (tooltipVisible) moverTooltipJuntoBurbuja()
                    }
                    view.performClick()
                    true
                }
                else -> false
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Toast: mostrar / ocultar / cola
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Muestra el toast con la última notificación del repositorio.
     * Llamado por tap en la burbuja.
     */
    private fun mostrarTooltip() {
        val notif = NotificationRepository.notifications.value.firstOrNull()
        if (notif != null) mostrarTooltipConNotif(notif)
    }

    /**
     * Muestra el toast con una [notif] específica.
     * Si hay otra en pantalla, ya debería haber pasado por la cola.
     */
    private fun mostrarTooltipConNotif(notif: AppNotification) {
        // ── Contenido ────────────────────────────────────────────────────────
        val titleTv    = tooltipView?.findViewById<TextView>(R.id.tooltip_title_tv)
        val subtitleTv = tooltipView?.findViewById<TextView>(R.id.tooltip_subtitle_tv)
        val iconIv     = tooltipView?.findViewById<ImageView>(R.id.tooltip_icon_iv)

        titleTv?.text = notif.title

        // Subtítulo: visible solo si hay mensaje
        if (notif.message.isNotBlank()) {
            subtitleTv?.text = notif.message
            subtitleTv?.visibility = View.VISIBLE
        } else {
            subtitleTv?.visibility = View.GONE
        }

        // Ícono según categoría
        val iconRes = when (notif.category) {
            "Notificación de Sistema"      -> android.R.drawable.ic_dialog_info
            "Notificación de Tarea"        -> android.R.drawable.ic_menu_my_calendar
            "Notificación de Estadísticas" -> android.R.drawable.ic_menu_report_image
            else                           -> android.R.drawable.ic_popup_reminder
        }
        iconIv?.setImageResource(iconRes)

        // ── Posición: a la derecha de la burbuja ─────────────────────────────
        tooltipParams?.x = bubbleParams.x + (chatHeadView?.width ?: 150) + 16
        tooltipParams?.y = bubbleParams.y + 20

        // ── Animación slide-in desde la derecha ──────────────────────────────
        tooltipView?.visibility = View.VISIBLE
        tooltipView?.translationX = SLIDE_OFFSET
        val anim = tooltipView?.animate()
            ?.translationX(0f)
            ?.setDuration(ANIM_DURATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim?.setInterpolator(cubicInterpolator)
        }
        anim?.start()
        tooltipVisible = true

        try { windowManager.updateViewLayout(tooltipView, tooltipParams) } catch (_: Exception) {}

        // ── Timer 5 segundos ─────────────────────────────────────────────────
        mainHandler.removeCallbacks(hideTooltipRunnable)
        mainHandler.postDelayed(hideTooltipRunnable, TOAST_DURATION)
    }

    /** Slide-out hacia la derecha. Al terminar, muestra la siguiente en cola si la hay. */
    private fun ocultarTooltip() {
        val anim = tooltipView?.animate()
            ?.translationX(SLIDE_OFFSET)
            ?.setDuration(ANIM_DURATION)
            ?.withEndAction {
                tooltipView?.visibility = View.GONE
                tooltipVisible = false

                // Siguiente en cola
                val siguiente = notificationQueue.removeFirstOrNull()
                if (siguiente != null) mostrarTooltipConNotif(siguiente)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim?.setInterpolator(cubicInterpolator)
        }
        anim?.start()
    }

    /** Click en burbuja: navega al historial central (BubbleChatActivity) y oculta el toast. La burbuja permanece. */
    private fun onBubbleClick() {
        // En vez de ir al sourceClass directo, siempre abrimos el panel de notificaciones
        try {
            val intent = Intent(this, com.example.rest.features.tools.BubbleChatActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("ChatHead", "Error al abrir BubbleChatActivity: ${e.message}")
        }
        
        // Ocultamos el toolTip flotante (Toast rojo)
        if (tooltipVisible) {
            mainHandler.removeCallbacks(hideTooltipRunnable)
            notificationQueue.clear() // Descartar cola al hacer click manual
            ocultarTooltip()
        }
    }

    private fun moverTooltipJuntoBurbuja() {
        tooltipParams?.x = bubbleParams.x + (chatHeadView?.width ?: 150) + 16
        tooltipParams?.y = bubbleParams.y + 20
        try { windowManager.updateViewLayout(tooltipView, tooltipParams) } catch (_: Exception) {}
    }

    // ──────────────────────────────────────────────────────────────────────────
    private fun isOverCloseView(bx: Int, by: Int, sw: Int, sh: Int): Boolean =
        bx > (sw / 2 - 200) && bx < (sw / 2 + 200) && by > sh - 250

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacks(hideTooltipRunnable)
        notificationQueue.clear()
        NotificationRepository.setBadgeObserver(null)
        NotificationRepository.setNewNotificationObserver(null)
        chatHeadView?.let { try { windowManager.removeView(it) } catch (_: Exception) {} }
        closeView?.let    { try { windowManager.removeView(it) } catch (_: Exception) {} }
        tooltipView?.let  { try { windowManager.removeView(it) } catch (_: Exception) {} }
    }

    private fun buildForegroundNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, "Asistente Rest Cycle", NotificationManager.IMPORTANCE_LOW)
                .also { getSystemService(NotificationManager::class.java).createNotificationChannel(it) }
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Asistente Rest Cycle activo")
            .setContentText("Toca la burbuja para ver tus alertas")
            .setSmallIcon(R.mipmap.logo_buho)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
