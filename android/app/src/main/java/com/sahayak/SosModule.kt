package com.sahayak

import android.content.Intent
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.content.Context
import android.location.LocationManager
import android.provider.Settings

class SosModule(private val ctx: ReactApplicationContext) :
    ReactContextBaseJavaModule(ctx) {

    override fun getName() = "SosModule"

    @ReactMethod
    fun startSOSService() {
        val serviceIntent = Intent(reactApplicationContext, SosForegroundService::class.java)
        reactApplicationContext.startForegroundService(serviceIntent)
    }

    @ReactMethod
    fun stopSOSService() {
        val serviceIntent = Intent(reactApplicationContext, SosForegroundService::class.java)
        reactApplicationContext.stopService(serviceIntent)
    }

    @ReactMethod
    fun promptIfGpsOff() {
        val lm = reactApplicationContext
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            reactApplicationContext.startActivity(i)
        }
    }
}
