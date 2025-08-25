package com.sahayak

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager

class SmsAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val msg      = intent.getStringExtra("msg") ?: return
        val interval = intent.getIntExtra("interval", 5)
        val number   = "+917858995750"

        try {
            SmsManager.getDefault().sendTextMessage(number, null, msg, null, null)
        } catch (_: Exception) { }

        // 🔁 Re-schedule next run
        SmsScheduler.schedule(ctx, msg, interval)
    }
}