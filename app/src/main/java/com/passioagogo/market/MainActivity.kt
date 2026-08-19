package com.passioagogo.market

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.passioagogo.market.core.push.EXTRA_ID
import com.passioagogo.market.core.push.EXTRA_TIPO
import com.passioagogo.market.ui.navigation.DeepLinkDestino
import com.passioagogo.market.ui.session.AppRoot
import com.passioagogo.market.ui.theme.PassioAgogoMarketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Extras que deja PAMessagingService al tocar una notificación. */
    private fun leerDeepLink(intent: android.content.Intent?): DeepLinkDestino? {
        val tipo = intent?.getStringExtra(EXTRA_TIPO) ?: return null
        return DeepLinkDestino(tipo = tipo, id = intent.getStringExtra(EXTRA_ID))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barras claras fijas: el tema es solo claro, y el modo automático
        // pondría iconos blancos ilegibles si el sistema está en oscuro.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        val deepLink = leerDeepLink(intent)
        setContent {
            PassioAgogoMarketTheme {
                AppRoot(deepLink = deepLink)
            }
        }
    }
}
