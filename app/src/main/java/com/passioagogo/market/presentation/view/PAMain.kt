package com.passioagogo.market.presentation.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.passioagogo.market.presentation.navigation.PANavigation
import com.passioagogo.market.presentation.navigation.Routes
import com.passioagogo.market.presentation.viewModel.imagenes.ImageGalleryViewModel
import com.passioagogo.market.ui.theme.PassioAgogoMarketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PAMain : ComponentActivity() {

    private val imageGalleryViewModel: ImageGalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PassioAgogoMarketTheme {
                PANavigation(
                    startDestination = getStartDestination(),
                    imageGalleryViewModel = imageGalleryViewModel
                )
            }
        }
    }

    fun getStartDestination(): String{
        val uris = intent.handleSharedImages().orEmpty()

        if(uris.isEmpty()){
            return Routes.Dashboard.Name
        }else {
            imageGalleryViewModel.handleSharedImages(uris)
            return Routes.ImageGallery.Name
        }
    }

    fun Intent.handleSharedImages(): List<Uri>? {
        return if (this.type?.startsWith("image/") == true){
            when (intent?.action) {
                    Intent.ACTION_SEND -> {
                        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        imageUri?.let { uri ->
                            return arrayListOf(uri)
                        }
                    }
                Intent.ACTION_SEND_MULTIPLE -> {
                    val imageUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                    imageUris?.let { uris ->
                        return uris
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
