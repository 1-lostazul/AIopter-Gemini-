package com.example.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.OverlayState
import com.example.ui.overlay.OverlayBubbleContent
import com.example.ui.overlay.OverlayPanelContent
import com.example.ui.theme.MyApplicationTheme
import kotlin.math.hypot

/**
 * Supported screen docking locations for the AIopter floating propeller.
 */
enum class PropellerCorner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

class AIopterOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var sessionManager: AIopterSessionManager
    private val lifecycleDispatcher = ServiceLifecycleDispatcher()

    private var bubbleView: ComposeView? = null
    private var panelView: ComposeView? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams

    private var isPanelExpanded = false
    private var screenWidth = 1080
    private var screenHeight = 2400

    private var bubbleSizePx = 180
    private var marginXPx = 60
    private var marginTopPx = 220
    private var marginBottomPx = 260

    private var dockAnimator: ValueAnimator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        sessionManager = AIopterSessionManager.getInstance(this)

        updateScreenDimensions()
        createNotificationChannel()
        startForegroundServiceWithNotification()

        if (Settings.canDrawOverlays(this)) {
            setupViews()
            sessionManager.updateOverlayState(OverlayState.BUBBLE)
        } else {
            sessionManager.updateOverlayState(OverlayState.PERMISSION_MISSING)
            stopSelf()
        }
    }

    private fun updateScreenDimensions() {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels

        val density = metrics.density
        bubbleSizePx = (64 * density).toInt()
        marginXPx = (20 * density).toInt() // 20dp margin from screen edge
        marginTopPx = (80 * density).toInt() // 80dp safe margin (avoids status bar & cutouts)
        marginBottomPx = (96 * density).toInt() // 96dp safe margin (avoids gesture pill & nav bars)
    }

    /**
     * Calculates the target coordinates for each of the 4 supported corner locations.
     */
    private fun getCornerCoordinates(corner: PropellerCorner): Point {
        val leftX = marginXPx
        val rightX = (screenWidth - bubbleSizePx - marginXPx).coerceAtLeast(leftX)
        val topY = marginTopPx
        val bottomY = (screenHeight - bubbleSizePx - marginBottomPx).coerceAtLeast(topY)

        return when (corner) {
            PropellerCorner.TOP_LEFT -> Point(leftX, topY)
            PropellerCorner.TOP_RIGHT -> Point(rightX, topY)
            PropellerCorner.BOTTOM_LEFT -> Point(leftX, bottomY)
            PropellerCorner.BOTTOM_RIGHT -> Point(rightX, bottomY)
        }
    }

    /**
     * Finds the geographically closest corner using Euclidean distance squared.
     */
    private fun findClosestCorner(currentX: Int, currentY: Int): Pair<PropellerCorner, Point> {
        val corners = PropellerCorner.values().map { corner ->
            corner to getCornerCoordinates(corner)
        }

        return corners.minByOrNull { (_, point) ->
            val dx = currentX - point.x
            val dy = currentY - point.y
            (dx.toLong() * dx) + (dy.toLong() * dy)
        } ?: (PropellerCorner.TOP_RIGHT to getCornerCoordinates(PropellerCorner.TOP_RIGHT))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_KILL -> {
                sessionManager.triggerKillSwitch()
                stopSelf()
            }
            ACTION_TOGGLE_PANEL -> {
                togglePanel()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AIopter Assistant Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time status of the floating AIopter assistant"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val killIntent = Intent(this, AIopterOverlayService::class.java).apply {
            action = ACTION_KILL
        }
        val killPendingIntent = PendingIntent.getService(
            this,
            1,
            killIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AIopter Assistant Active")
            .setContentText("Propeller ready • Tap to open panel • Magnetic docking")
            .setSmallIcon(R.drawable.aiopter_rotor_icon_1789341287865)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.aiopter_rotor_icon_1789341287865, "Emergency Stop / KILL", killPendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } catch (e: Exception) {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupViews() {
        // --- 1. Bubble Layout Params & Saved Position ---
        val prefs = sessionManager.repository.preferences
        val savedCorner = runCatching {
            PropellerCorner.valueOf(prefs.dockedCorner)
        }.getOrDefault(PropellerCorner.TOP_RIGHT)

        val initialPoint = getCornerCoordinates(savedCorner)

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialPoint.x
            y = initialPoint.y
        }

        bubbleView = ComposeView(this).apply {
            lifecycleDispatcher.attachToComposeView(this)
            setContent {
                MyApplicationTheme(darkTheme = true) {
                    val state by sessionManager.sessionState.collectAsState()
                    OverlayBubbleContent(
                        screenCaptureState = state.screenCaptureState,
                        microphoneState = state.microphoneState,
                        aiRequestState = state.aiRequestState,
                        onBubbleClick = { togglePanel() }
                    )
                }
            }
        }

        // --- 2. Robust Drag vs Tap Detection and Magnetic Self-Docking ---
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop.toFloat()

        bubbleView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var initialParamsX = 0
            private var initialParamsY = 0
            private var isDragging = false
            private var downTime = 0L

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        initialParamsX = bubbleParams.x
                        initialParamsY = bubbleParams.y
                        isDragging = false
                        downTime = SystemClock.uptimeMillis()
                        // Cancel any ongoing self-docking glide animation immediately
                        dockAnimator?.cancel()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY
                        val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()

                        if (!isDragging && dist > touchSlop) {
                            isDragging = true
                        }

                        if (isDragging) {
                            bubbleParams.x = (initialParamsX + dx).toInt()
                            bubbleParams.y = (initialParamsY + dy).toInt()
                            try {
                                windowManager.updateViewLayout(bubbleView, bubbleParams)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val duration = SystemClock.uptimeMillis() - downTime
                        if (!isDragging && duration < 400) {
                            // Quick stationary tap: open/toggle compact AIopter assistant panel
                            togglePanel()
                        } else if (isDragging) {
                            // Drag released: perform automatic self-docking to closest corner
                            selfDockToClosestCorner()
                        }
                        return true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        if (isDragging) {
                            selfDockToClosestCorner()
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(bubbleView, bubbleParams)

        // --- 3. Assistant Panel Layout Params ---
        panelParams = WindowManager.LayoutParams(
            (screenWidth * 0.94f).toInt().coerceAtMost(600),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            y = marginTopPx - 20
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        panelView = ComposeView(this).apply {
            lifecycleDispatcher.attachToComposeView(this)
            setContent {
                MyApplicationTheme(darkTheme = true) {
                    OverlayPanelContent(
                        sessionManager = sessionManager,
                        onCollapse = { collapsePanel() },
                        onOpenMainApp = {
                            collapsePanel()
                            val intent = Intent(this@AIopterOverlayService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(intent)
                        }
                    )
                }
            }

            // Dismiss panel when user taps outside of it
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    collapsePanel()
                    true
                } else {
                    false
                }
            }
        }
    }

    /**
     * Smoothly animates the propeller to the closest screen corner with magnetic deceleration.
     */
    private fun selfDockToClosestCorner() {
        val (closestCorner, targetPoint) = findClosestCorner(bubbleParams.x, bubbleParams.y)
        animateToCorner(targetPoint.x, targetPoint.y, closestCorner)
    }

    private fun animateToCorner(targetX: Int, targetY: Int, corner: PropellerCorner) {
        dockAnimator?.cancel()
        val startX = bubbleParams.x
        val startY = bubbleParams.y

        dockAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 280L
            interpolator = DecelerateInterpolator(1.8f) // Magnetic snap curve
            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                bubbleParams.x = (startX + (targetX - startX) * fraction).toInt()
                bubbleParams.y = (startY + (targetY - startY) * fraction).toInt()
                if (bubbleView?.isAttachedToWindow == true) {
                    try {
                        windowManager.updateViewLayout(bubbleView, bubbleParams)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val prefs = sessionManager.repository.preferences
                    prefs.bubblePosX = targetX.toFloat()
                    prefs.bubblePosY = targetY.toFloat()
                    prefs.dockedCorner = corner.name
                }
            })
            start()
        }
    }

    private fun togglePanel() {
        if (isPanelExpanded) {
            collapsePanel()
        } else {
            expandPanel()
        }
    }

    private fun expandPanel() {
        if (isPanelExpanded || panelView == null) return
        try {
            windowManager.addView(panelView, panelParams)
            isPanelExpanded = true
            sessionManager.updateOverlayState(OverlayState.PANEL)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun collapsePanel() {
        if (!isPanelExpanded || panelView == null) return
        try {
            windowManager.removeView(panelView)
            isPanelExpanded = false
            sessionManager.updateOverlayState(OverlayState.BUBBLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dockAnimator?.cancel()
        if (isPanelExpanded && panelView != null) {
            try {
                windowManager.removeView(panelView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (bubbleView != null) {
            try {
                windowManager.removeView(bubbleView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        lifecycleDispatcher.destroy()
        sessionManager.updateOverlayState(OverlayState.DISABLED)
    }

    companion object {
        const val CHANNEL_ID = "aiopter_overlay_channel"
        const val NOTIFICATION_ID = 4040
        const val ACTION_KILL = "com.example.action.KILL"
        const val ACTION_TOGGLE_PANEL = "com.example.action.TOGGLE_PANEL"

        fun start(context: Context) {
            val intent = Intent(context, AIopterOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AIopterOverlayService::class.java)
            context.stopService(intent)
        }
    }
}
