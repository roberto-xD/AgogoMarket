package com.passioagogo.market.core.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.passioagogo.market.MainActivity
import com.passioagogo.market.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

const val CANAL_ALERTAS = "passion_alertas"

/** Claves que viajan en el payload y que MainActivity lee para navegar. */
const val EXTRA_TIPO = "tipo"
const val EXTRA_ID = "id"

fun crearCanalNotificaciones(context: Context) {
    val canal = NotificationChannel(
        CANAL_ALERTAS,
        "Alertas de administración",
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = "Solicitudes de pedido y mensajes de contacto"
    }
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(canal)
}

@AndroidEntryPoint
class PAMessagingService : FirebaseMessagingService() {

    @Inject lateinit var pushTokenRepository: PushTokenRepository

    // El servicio no tiene ciclo de vida propio para corrutinas
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * FCM rota el token: al reinstalar, restaurar el dispositivo o
     * limpiar datos. Sin este registro, el usuario deja de recibir
     * notificaciones en silencio.
     */
    override fun onNewToken(token: String) {
        scope.launch { pushTokenRepository.registrarToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val tipo = message.data[EXTRA_TIPO]
        val id = message.data[EXTRA_ID]
        val titulo = message.notification?.title ?: "Passion A Gogo"
        val cuerpo = message.notification?.body.orEmpty()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_TIPO, tipo)
            putExtra(EXTRA_ID, id)
        }
        val pending = PendingIntent.getActivity(
            this,
            // Id único por notificación: si fuera fijo, todas reutilizarían
            // los extras de la primera.
            (id ?: tipo ?: "").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificacion = NotificationCompat.Builder(this, CANAL_ALERTAS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        runCatching {
            NotificationManagerCompat.from(this)
                .notify((id ?: tipo ?: "").hashCode(), notificacion)
        }
    }
}
