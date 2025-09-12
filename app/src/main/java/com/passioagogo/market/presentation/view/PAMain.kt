package com.passioagogo.market.presentation.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.passioagogo.market.presentation.navigation.PANavigation
import com.passioagogo.market.ui.theme.PassioAgogoMarketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PAMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleSharedImages(intent)
        setContent {
            PassioAgogoMarketTheme {
                PANavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedImages(intent)
    }

    fun handleSharedImages(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("image/") == true) {
                    val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    imageUri?.let { uri ->
                        // Navegar a la pantalla de galería y procesar la imagen
                        // Puedes usar el ViewModel directamente o pasar el URI como parámetro
                    }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                if (intent.type?.startsWith("image/") == true) {
                    val imageUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                    imageUris?.let { uris ->
                        // Procesar múltiples imágenes
                    }
                }
            }
        }
    }
}
