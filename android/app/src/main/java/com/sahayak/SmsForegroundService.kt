package com.sahayak

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.telephony.SmsManager

class SmsForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "sos_channel"
        private const val NOTIF_ID = 1001
    }

    private var workerThread: HandlerThread? = null
    private var handler: Handler? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var message: String = "SOS!"
    private var number: String = "+911234567890"
    private var intervalMs: Long = 5000L
    private var running = false

    private val task = object : Runnable {
        override fun run() {
            if (!running) return
            try {
                // Send the SMS silently
                SmsManager.getDefault().sendTextMessage(number, null, message, null, null)
            } catch (_: Exception) { /* consider logging */ }

            // schedule again
            handler?.postDelayed(this, intervalMs)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Dedicated background thread
        workerThread = HandlerThread("SmsForegroundWorker").also { it.start() }
        val looper: Looper = workerThread!!.looper
        handler = Handler(looper)

        // WakeLock to keep CPU on (extra safety in Doze)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sahayak:SOSWakeLock")
        wakeLock?.setReferenceCounted(false)
        wakeLock?.acquire(10 * 60 * 1000L /*10 min timeout; will be re-acquired on demand*/)

        // Notification channel & foreground
        createNotificationChannelIfNeeded()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        message = intent?.getStringExtra("msg") ?: message
        number = intent?.getStringExtra("number") ?: number
        intervalMs = (intent?.getLongExtra("interval", 5L) ?: 5L) * 1000L

        // ensure wakelock is held
        if (wakeLock?.isHeld != true) {
            wakeLock?.acquire(10 * 60 * 1000L)
        }

        // start loop
        if (!running) {
            running = true
            handler?.removeCallbacksAndMessages(null)
            handler?.post(task)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        handler?.removeCallbacksAndMessages(null)
        workerThread?.quitSafely()
        if (wakeLock?.isHeld == true) wakeLock?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SOS Alerts",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service sending SOS SMS periodically"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("SOS Active")
                .setContentText("Sending emergency SMS every 5 seconds.")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("SOS Active")
                .setContentText("Sending emergency SMS every 5 seconds.")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setOngoing(true)
                .build()
        }
    }
}
