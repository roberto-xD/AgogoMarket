package com.passioagogo.market

import android.app.Application
import com.passioagogo.market.core.push.crearCanalNotificaciones
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PAApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // El canal debe existir antes de la primera notificación
        crearCanalNotificaciones(this)
    }
}
