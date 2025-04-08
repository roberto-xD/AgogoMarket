package com.passioagogo.market.presentation.view.templates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.presentation.view.components.PATextInput

@Composable
fun PANewProduct(
    currentProduct: PAProductBean,
){
    val editedProduct = remember {
        mutableStateOf(currentProduct)
    }

    Box(
        modifier = Modifier
            .shadow(elevation = 3.dp)
            .border(border = BorderStroke(width = 5.dp, color = Color.Gray), shape = CircleShape)
    ) {
        Column {
            PATextInput(
                value = editedProduct.value.title,
                onValueChange = {
                    editedProduct.value = editedProduct.value.copy(title = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_name),
            )
            PATextInput(
                value = editedProduct.value.sku,
                onValueChange = {
                    editedProduct.value = editedProduct.value.copy(sku = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_sku),
            )
        }
    }
}