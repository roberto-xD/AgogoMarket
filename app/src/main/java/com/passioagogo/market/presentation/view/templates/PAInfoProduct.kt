package com.passioagogo.market.presentation.view.templates

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.passioagogo.market.presentation.view.components.ImageView
import com.passioagogo.market.presentation.view.components.PABasicButton
import com.passioagogo.market.presentation.view.components.PAContainer
import com.passioagogo.market.presentation.view.components.PACustomAlertDialog
import com.passioagogo.market.presentation.view.components.PADropDown
import com.passioagogo.market.presentation.view.components.PATextInput
import com.passioagogo.market.presentation.view.models.PAInfoModel

@Composable
fun PAInfoProduct(
    modifier: Modifier = Modifier,
    initialData: MutableState<PAInfoModel>,
    onSaveClick: () -> Unit,
    onImageClick: () -> Unit,
    onScanClick: () -> Unit,
){
    LaunchedEffect(Unit) {
        Log.i("tag_pg","familias: ${initialData.value.familyList}")
    }
    val showNewCategoryDialog = remember { mutableStateOf(false) }
    val showNewSubcategoryDialog = remember { mutableStateOf(false) }

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
                        mutableInput = initialData.value.tittle,
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeHolder = stringResource(id = R.string.label_name),
                    )
                    PATextInput(
                        mutableInput = initialData.value.sku,
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeHolder = stringResource(id = R.string.label_sku),
                    )
                    PATextInput(
                        mutableInput = initialData.value.codigoBarra,
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeHolder = stringResource(id = R.string.label_codigo_de_barras),
                        trailingIcon = R.drawable.barcode_reader,
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
                        imagePath = initialData.value.pathImageList.firstOrNull(),
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
                mutableInput = initialData.value.sellPrice,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_sell_price),
                keyboardType = KeyboardType.Number
            )
            PATextInput(
                mutableInput = initialData.value.description,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_description),
                minLines = 3,
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
                mutableInput = initialData.value.currentStock,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_actual_stock),
                keyboardType = KeyboardType.Number
            )
            PATextInput(
                mutableInput = initialData.value.minStock,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_reposition_quantity),
                keyboardType = KeyboardType.Number
            )
            PATextInput(
                mutableInput = initialData.value.buyPrice,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_buy_price),
                keyboardType = KeyboardType.Number
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        PAContainer(
            modifier = modifier,
            containerTittle = "Atributos"
        ){
            PADropDown(
                item = initialData.value.family,
                items = initialData.value.familyList,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_family),
            ){
                Log.i("tag","dropdown: $it")
            }
            PADropDown(
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_categories),
                items = initialData.value.categoryList,
                item = initialData.value.category,
                onAddNewClick = {
                    showNewCategoryDialog.value = true
                    Log.i("tag","onAddNewClick")
                }
            ){
                Log.i("tag","dropdown: $it")
            }
            PADropDown(
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_subcategories),
                items = initialData.value.subcategoryList,
                item = initialData.value.subcategory,
                onAddNewClick = {
                    showNewSubcategoryDialog.value = true
                    Log.i("tag","onAddNewClick")
                }
            ){
                Log.i("tag","dropdown: $it")
            }
        }
        PABasicButton(
            label1 = "Guardar",
            onClick1 = {
//                onSaveClick(
//                    PAInfoModel(
//                            nombre = tittle.value,
//                            descripcion = description.value,
//                            imagenes = rutasImagenList ?: emptyList(),
//                            codigoBarras = codigoBarra.value,
//                            skuInterno = sku.value,
//                            precioCompra = buyPrice.value,
//                            precioVenta = sellPrice.value,
//                            cantidadActual = actualStock.value,
//                            cantidadMinima = minStock.value,
//                            familia = family.value,
//                            categoria = category.value,
//                    )
//                )
            }
        )
    }

    PACustomAlertDialog(
        tittle = "Añadir nueva categoría",
        showDialog = showNewCategoryDialog
    ){
        initialData.value.categoryList.add(it)
        initialData.value.category.value = it
    }
    PACustomAlertDialog(
        tittle = "Añadir nueva subcategoría",
        showDialog = showNewSubcategoryDialog
    ){
        initialData.value.subcategoryList.add(it)
        initialData.value.subcategory.value = it
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview(

){
    PAInfoProduct(
        initialData = mutableStateOf(PAInfoModel()),
        onSaveClick = {

        },
        onScanClick = {},
        onImageClick = {}
    )
}