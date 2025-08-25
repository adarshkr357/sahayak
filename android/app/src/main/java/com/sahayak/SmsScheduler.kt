package com.sahayak

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.Settings

object SmsScheduler {

    fun schedule(ctx: Context, message: String, delaySec: Int) {
        val alarmMgr = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Ask for the user-level exemption once (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmMgr.canScheduleExactAlarms()
        ) {
            val i = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
            // return early; caller can retry after the user responds
            return
        }

        val pi = PendingIntent.getBroadcast(
            ctx,
            0,
            Intent(ctx, SmsAlarmReceiver::class.java).putExtra("msg", message),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmMgr.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + delaySec * 1000L,
            pi
        )
    }
}