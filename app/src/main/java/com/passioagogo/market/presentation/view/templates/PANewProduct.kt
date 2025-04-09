package com.passioagogo.market.presentation.view.templates

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.presentation.view.components.PAContainer
import com.passioagogo.market.presentation.view.components.PADropDown
import com.passioagogo.market.presentation.view.components.PATextInput

@Composable
fun PANewProduct(
    currentProduct: PAProductBean,
){
    val editedProduct = remember {
        mutableStateOf(currentProduct)
    }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        PAContainer(
            modifier = Modifier
                .padding(5.dp),
        ) {
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
            PADropDown(
                modifier = Modifier
                    .padding(10.dp),
                placeHolder = "Unidad",
                list = listOf("caja","pzas","L","ml")
            ) {
                Toast
                    .makeText(context, "dato: $it", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        PAContainer(
            modifier = Modifier
                .padding(5.dp),
            containerTittle = "Información de ventas"
        ) {
            PATextInput(
                value = editedProduct.value.price.price_normal.toString(),
                onValueChange = {
                    editedProduct.value = editedProduct.value.copy(title = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_original_price),
            )
            PATextInput(
                value = editedProduct.value.description,
                onValueChange = {
                    editedProduct.value = editedProduct.value.copy(description = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_description),
            )
        }
    }
}

@Composable
@Preview(showBackground = true,showSystemUi = true)
private fun Preview(

){
    PANewProduct(
        currentProduct = PAProductBean()
    )
}