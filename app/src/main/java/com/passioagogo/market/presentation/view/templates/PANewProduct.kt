package com.passioagogo.market.presentation.view.templates

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import com.passioagogo.market.presentation.view.components.PAContainer
import com.passioagogo.market.presentation.view.components.PATextInput

@Composable
fun PAEditInfoProduct(
    editedProduct: MutableState<PAProductBean> ,
){
    val context = LocalContext.current

    val tittle = remember { mutableStateOf("")}
    val sku = remember { mutableStateOf("")}
    val units = remember { mutableStateOf("")}
    val minStock = remember { mutableStateOf("")}
    val description = remember { mutableStateOf("")}
    val buyPrice = remember { mutableStateOf("")}
    val sellPrice = remember { mutableStateOf("")}

    editedProduct.value = editedProduct.value.update(
        title = tittle.value,
        sku = sku.value,
        description = description.value,
        actual_stock = units.value,
        reposition_quantity = minStock.value,
    )
    Column{
        PAContainer(
            modifier = Modifier,
        ) {
            PATextInput(
                mutableInput = tittle,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_name),
            )
            PATextInput(
                mutableInput = sku,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_sku),
                trailingIcon = R.drawable.barcode_scann,
                onTrailingIconClick = {
                    Toast
                        .makeText(context, "abrir escaner", Toast.LENGTH_SHORT)
                        .show()
                }
            )
//            PADropDown(
//                modifier = Modifier
//                    .padding(top = 10.dp, bottom = 10.dp),
//                placeHolder = "Unidad",
//                list = listOf("caja","pzas","L","ml")
//            ) {
//                editedProduct.value.update(units = it)
//                Toast
//                    .makeText(context, "dato: $it", Toast.LENGTH_SHORT)
//                    .show()
//            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        PAContainer(
            modifier = Modifier,
            containerTittle = "Información de ventas"
        ) {
            PATextInput(
                mutableInput = sellPrice,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_sell_price),
            )
            PATextInput(
                mutableInput = description,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_description),
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        PAContainer(
            modifier = Modifier,
            containerTittle = "Seguimiento de inventario de este articulo"
        ) {
            PATextInput(
                mutableInput = units,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_initial_stock),
                keyboardType = KeyboardType.Number
            )
            PATextInput(
                mutableInput = buyPrice,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_buy_price),
                keyboardType = KeyboardType.Number
            )
            PATextInput(
                mutableInput = minStock,
                modifier = Modifier
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
    PAEditInfoProduct(
        editedProduct = editedProduct
    )
}