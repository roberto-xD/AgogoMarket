package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PAImageGalery(
    imagePaths : List<String>,
    onDeleteImage : (path: String) -> Unit,
    onAddNew : (() -> Unit) ?= null,
){
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = imagePaths.size,
        ){ item ->
            PAImageItem(
                imagePath = imagePaths[item],
                onDeleteClick = {
                    onDeleteImage.invoke(imagePaths[item])
                }
            )
        }
        onAddNew?.let {
            item{
                PAImageItem(
                    onImageClick = {
                        onAddNew.invoke()
                    }
                )
            }
        }
    }
}