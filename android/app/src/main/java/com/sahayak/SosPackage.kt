package com.sahayak

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.facebook.react.bridge.NativeModule

class SosPackage : ReactPackage {
    override fun createViewManagers(reactContext: ReactApplicationContext)
            : List<ViewManager<*, *>> = emptyList()

    override fun createNativeModules(reactContext: ReactApplicationContext)
            : List<NativeModule> = listOf(SosModule(reactContext))
}