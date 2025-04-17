package com.passioagogo.market.presentation.view.templates

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.bean.update
import com.passioagogo.market.domain.bean.updatePrice
import com.passioagogo.market.presentation.view.components.PAContainer
import com.passioagogo.market.presentation.view.components.PADropDown
import com.passioagogo.market.presentation.view.components.PATextInput

@Composable
fun PANewProduct(
    editedProduct: MutableState<PAProductBean> ,
){
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .verticalScroll(
                rememberScrollState()
            )
            .padding(5.dp)
            .fillMaxSize()
    ) {
        PAContainer(
            modifier = Modifier,
        ) {
            PATextInput(
                value = editedProduct.value.title.orEmpty(),
                onValueChange = {
                    editedProduct.value.update(title = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_name),
            )
            PATextInput(
                value = editedProduct.value.sku,
                onValueChange = {
                    editedProduct.value.update(sku = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_sku),
                trailingIcon = R.drawable.barcode_scann,
                onTrailingIconClick = {
                    Toast
                        .makeText(context, "abrir escaner", Toast.LENGTH_SHORT)
                        .show()
                }
            )
            PADropDown(
                modifier = Modifier
                    .padding(10.dp),
                placeHolder = "Unidad",
                list = listOf("caja","pzas","L","ml")
            ) {
                editedProduct.value.update(units = it)
                Toast
                    .makeText(context, "dato: $it", Toast.LENGTH_SHORT)
                    .show()
            }
            Spacer(modifier = Modifier.size(10.dp))
        }
        PAContainer(
            modifier = Modifier,
            containerTittle = "Información de ventas"
        ) {
            PATextInput(
                value = editedProduct.value.price?.public,
                onValueChange = {
                    editedProduct.value.updatePrice(public = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_original_price),
            )
            PATextInput(
                value = editedProduct.value.description,
                onValueChange = {
                    editedProduct.value.update(description = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_description),
            )
        }
        PAContainer(
            modifier = Modifier,
            containerTittle = "Seguimiento de inventario de este articulo"
        ) {
            PATextInput(
                value = editedProduct.value.actual_stock,
                onValueChange = {
                    editedProduct.value.update(actual_stock = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_initial_stock),
                keyboardType = KeyboardType.Number
            )
            PATextInput(
                value = editedProduct.value.price?.supplier,
                onValueChange = {
                    editedProduct.value.updatePrice(supplier = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_initial_price),
                keyboardType = KeyboardType.Number
            )
            PATextInput(
                value = editedProduct.value.reposition_quantity,
                onValueChange = {
                    editedProduct.value.update(reposition_quantity = it)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_reposition_quantity),
                keyboardType = KeyboardType.Number
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview(

){
    val editedProduct = remember {
        mutableStateOf(PAProductBean())
    }
    PANewProduct(
        editedProduct = editedProduct
    )
}