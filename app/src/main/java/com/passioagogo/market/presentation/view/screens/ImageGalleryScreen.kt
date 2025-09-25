package com.passioagogo.market.presentation.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passioagogo.market.presentation.view.components.PAImageItem
import com.passioagogo.market.presentation.view.components.items.ImagePreviewDialog
import com.passioagogo.market.presentation.viewModel.imagenes.ImageGalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGalleryScreen(
    viewModel: ImageGalleryViewModel,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galería de Imágenes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.images.isEmpty() -> {
                    Text(
                        text = "No hay imágenes guardadas",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = uiState.images.size,
                            itemContent = { index ->
                                val imagePath = uiState.images[index]
                                PAImageItem(
                                    imagePath = imagePath,
                                    onImageClick = { viewModel.showImagePreview(imagePath) },
                                    onDeleteClick = { viewModel.deleteImage(imagePath) },
                                )
                            }
                        )
                    }
                }
            }

            // Mostrar errores
            uiState.errorMessage?.let { message ->
                LaunchedEffect(message) {
                    // Aquí podrías mostrar un Snackbar
                    viewModel.clearError()
                }
            }
        }
    }

    // Dialog de vista previa
    uiState.selectedImageForPreview?.let { imagePath ->
        ImagePreviewDialog(
            imagePath = imagePath,
            onDismiss = { viewModel.hideImagePreview() },
            onDelete = {
                viewModel.deleteImage(imagePath)
                viewModel.hideImagePreview()
            }
        )
    }
}