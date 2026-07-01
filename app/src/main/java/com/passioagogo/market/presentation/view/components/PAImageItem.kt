package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.passioagogo.market.ui.theme.primaryLight
import java.io.File

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PAImageItem(
    modifier: Modifier = Modifier,
    imagePath: String? = null,
    onImageClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if(imagePath.isNullOrBlank()){
                IconButton(
                    onClick = { onImageClick?.invoke() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(0.7F)
                        .background(
                            color = primaryLight,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        modifier = Modifier.fillMaxSize(0.5F),
                        imageVector = Icons.Default.Add ,
                        contentDescription = "Agregar",
                        tint = Color.White
                    )
                }
            }else {
                GlideImage(
                    model = File(imagePath),
                    contentDescription = "Imagen del producto",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Botón de eliminar
            onDeleteClick?.let {
                IconButton(
                    onClick = { showDeleteDialog.value = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(50)
                        )
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar imagen",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar esta imagen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick?.invoke()
                        showDeleteDialog.value = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog.value = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
@Preview
private fun Preview(){
    PAImageItem(
        onImageClick = {},
        onDeleteClick = {}
    )
}