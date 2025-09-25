package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.passioagogo.market.ui.theme.outlineLight
import com.passioagogo.market.ui.theme.primaryLight
import java.io.File

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageView(
    modifier: Modifier = Modifier,
    imagePath: String?,
    onImageClick: () -> Unit = {},
){
    Box(
        modifier = Modifier
            .clickable(
                enabled = true,
                onClick = onImageClick
            )
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = outlineLight,
                shape = MaterialTheme.shapes.small
            )
    ){
        if (imagePath.isNullOrEmpty()){
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onImageClick,
                    modifier = Modifier
                        .background(
                            color = primaryLight,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add ,
                        contentDescription = "Agregar",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Agregar Imagen",
                    color = primaryLight,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            GlideImage(
                model = File(imagePath),
                contentDescription = "",
                modifier = modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
@Preview
private fun Preview(){
    ImageView(
        imagePath = "",
        )
}