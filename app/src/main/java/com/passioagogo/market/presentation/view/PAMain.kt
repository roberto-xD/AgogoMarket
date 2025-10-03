package com.passioagogo.market.presentation.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.passioagogo.market.presentation.navigation.PANavigation
import com.passioagogo.market.presentation.navigation.Routes
import com.passioagogo.market.presentation.viewModel.imagenes.ImageGalleryViewModel
import com.passioagogo.market.presentation.viewModel.products.DetalleProductoViewModel
import com.passioagogo.market.ui.theme.PassioAgogoMarketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PAMain : ComponentActivity() {

    private val imageGalleryViewModel: ImageGalleryViewModel by viewModels()
    private val detalleViewModel: DetalleProductoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PassioAgogoMarketTheme {
                PANavigation(
                    startDestination = getStartDestination(),
                    imageViewModel = imageGalleryViewModel,
                    detalleViewModel = detalleViewModel,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val uris = intent.handleSharedImages().orEmpty()
        imageGalleryViewModel.saveSharedImages(uris)
    }

    fun getStartDestination(): String{
        val uris = intent.handleSharedImages().orEmpty()
        Log.i("tag","uris vacias: ${uris.isEmpty()}")
        if(uris.isEmpty()){
            return Routes.Dashboard.Name
        }else {
            imageGalleryViewModel.saveSharedImages(uris)
            return Routes.ItemDetail.Name
        }
    }

    fun Intent.handleSharedImages(): List<Uri>? {
        return if (this.type?.startsWith("image/") == true || this.type?.startsWith("*/*") == true){
            when (intent?.action) {
                    Intent.ACTION_SEND -> {
                        try {
                            val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                            imageUri?.let { uri ->
                                return arrayListOf(uri)
                            }
                        }catch (e: Exception){
                            return null
                        }

                    }
                Intent.ACTION_SEND_MULTIPLE -> {
                    try {
                        val imageUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                        imageUris?.let { uris ->
                            return uris
                        }
                    }catch (e: Exception){
                        return null
                    }
                }
                else -> {
                    null
                }
            }
        } else {
            null
        }
    }
}
