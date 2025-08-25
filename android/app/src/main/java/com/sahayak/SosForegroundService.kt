package com.sahayak

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat

class SosForegroundService : Service() {
    private val phoneNumber = "+917858995750"
    private val intervalMillis = 5_000L
    private val handler = Handler(Looper.getMainLooper())
    private val sendSosRunnable = object : Runnable {
        override fun run() {
            sendSosSMS()
            handler.postDelayed(this, intervalMillis)
        }
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startInForeground()
        handler.post(sendSosRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(sendSosRunnable)
        super.onDestroy()
    }

    private fun sendSosSMS() {
        val location = getLastLocation()
        val msg = if (location != null) {
            "SOS! I'm in danger. Location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
        } else {
            "SOS! I am in danger. Location unavailable."
        }
        try {
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, msg, null, null)
        } catch (_: Exception) {}
    }

    private fun getLastLocation(): Location? {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (_: SecurityException) {
            null
        }
    }

    private fun startInForeground() {
        val channelId = "sahayak-sos"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, "SOS Active", NotificationManager.IMPORTANCE_DEFAULT)
            getSystemService(NotificationManager::class.java).createNotificationChannel(chan)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SOS is running")
            .setContentText("Sending SOS every 5 seconds")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()
        startForeground(42, notification)
    }
}
