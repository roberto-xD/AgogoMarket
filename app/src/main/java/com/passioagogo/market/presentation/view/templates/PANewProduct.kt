package com.passioagogo.market.presentation.view.templates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.presentation.view.components.ImageView
import com.passioagogo.market.presentation.view.components.PABasicButton
import com.passioagogo.market.presentation.view.components.PAContainer
import com.passioagogo.market.presentation.view.components.PATextInput
import com.passioagogo.market.presentation.view.models.PAInfoModel

@Composable
fun PAInfoProduct(
    modifier: Modifier = Modifier,
    rutaImagen: MutableState<List<String>?> = remember { mutableStateOf(null) },
    onSaveClick: (current: PAInfoModel) -> Unit,
    onImageClick: () -> Unit,
    onScanClick: () -> Unit,
){

    val tittle = remember { mutableStateOf("")}
    val sku = remember { mutableStateOf("")}
    val units = remember { mutableStateOf("")}
    val minStock = remember { mutableStateOf("")}
    val description = remember { mutableStateOf("")}
    val buyPrice = remember { mutableStateOf("")}
    val sellPrice = remember { mutableStateOf("")}

    Column(
        modifier = modifier
            .fillMaxSize()
    ){
        PAContainer(
            modifier = modifier,
        ) {
            Row {
                Column(
                    modifier = modifier
                        .padding(top = 20.dp)
                        .weight(2f),
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
                        placeHolder = stringResource(id = R.string.label_codigo_de_barras),
                        trailingIcon = R.drawable.barcode_scann,
                        onTrailingIconClick = onScanClick
                    )
                }
                Box(
                        modifier = modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 10.dp, top = 5.dp)
                            .weight(1f),
                ){
                    ImageView(
                        modifier = modifier,
                        imagePath = rutaImagen.value?.firstOrNull(),
                        onImageClick = onImageClick
                    )
                }
            }
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
                maxLines = 5,
                maxLength = 50,
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        PAContainer(
            modifier = Modifier,
            containerTittle = "Seguimiento del artículo"
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
        PABasicButton(
            label1 = "Guardar",
            onClick1 = {
                onSaveClick(
                    PAInfoModel(
                            nombre = tittle.value,
                            descripcion = description.value,
                            codigoBarras = sku.value,
                    )
                )
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview(

){
    val editedProduct = remember {
        mutableStateOf(ProductoDetallado())
    }
    PAInfoProduct(
        onSaveClick = {},
        onScanClick = {},
        onImageClick = {}
    )
}