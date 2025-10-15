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
import com.passioagogo.market.presentation.view.models.getDescripcion

@Composable
fun PAInfoProduct(
    modifier: Modifier = Modifier,
    initialData: PAInfoModel,
    onFamilyDropDownSelect: (family: String) -> Unit,
    onSaveClick: () -> Unit,
    onImageClick: () -> Unit,
    onScanClick: () -> Unit,
    onDataChange: (PAInfoModel) -> Unit,
){
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
                        value = initialData.tittle,
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeHolder = stringResource(id = R.string.label_name),
                    ){
                        onDataChange(initialData.copy(tittle = it))
                    }
                    PATextInput(
                        value = initialData.sku,
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeHolder = stringResource(id = R.string.label_sku),
                    ){
                        onDataChange(initialData.copy(sku = it))
                    }
                    PATextInput(
                        value = initialData.codigoBarra,
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeHolder = stringResource(id = R.string.label_codigo_de_barras),
                        trailingIcon = R.drawable.barcode_reader,
                        onTrailingIconClick = onScanClick
                    ){
                        onDataChange(initialData.copy(codigoBarra = it))
                    }
                }
                Box(
                        modifier = modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 10.dp, top = 5.dp)
                            .weight(1f),
                ){
                    ImageView(
                        modifier = modifier,
                        imagePath = initialData.pathImageList.firstOrNull()?.rutaImagen,
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
                value = initialData.sellPrice,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_sell_price),
                keyboardType = KeyboardType.Number
            ){
                onDataChange(initialData.copy(sellPrice = it))
            }
            PATextInput(
                value = initialData.description,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_description),
                minLines = 3,
                maxLines = 5,
                maxLength = 50,
            ){
                onDataChange(initialData.copy(description = it))
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        PAContainer(
            modifier = Modifier,
            containerTittle = "Seguimiento del artículo"
        ) {
            PATextInput(
                value = initialData.currentStock,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_actual_stock),
                keyboardType = KeyboardType.Number
            ){
                onDataChange(initialData.copy(currentStock = it))
            }
            PATextInput(
                value = initialData.minStock,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_reposition_quantity),
                keyboardType = KeyboardType.Number
            ){
                onDataChange(initialData.copy(minStock = it))
            }
            PATextInput(
                value = initialData.buyPrice,
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_buy_price),
                keyboardType = KeyboardType.Number
            ){
                onDataChange(initialData.copy(buyPrice = it))
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        PAContainer(
            modifier = modifier,
            containerTittle = "Atributos"
        ){
            PADropDown(
                item = initialData.familyList.getDescripcion(initialData.familyId),
                items = initialData.familyList.map{it.descripcion},
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_family),
            ){ seleccion ->
                Log.i("tag","dropdown: $seleccion")
                initialData.familyList.find { it.descripcion.equals(seleccion) }?.let { reasigned ->
                    onDataChange(initialData.copy(
                        familyId = reasigned.id,
                    ))
                }
//                onFamilyDropDownSelect(it)
            }
            PADropDown(
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_categories),
                item = initialData.category,
                items = initialData.categoryList,
                onAddNewClick = {
                    showNewCategoryDialog.value = true
                    Log.i("tag","onAddNewClick")
                }
            ){
                onDataChange(initialData.copy(category = it))
                Log.i("tag","dropdown: $it")
            }
//            PADropDown(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                placeHolder = stringResource(id = R.string.label_subcategories),
//                items = initialData.subcategoryList,
//                item = initialData.subcategory,
//                onAddNewClick = {
//                    showNewSubcategoryDialog.value = true
//                    Log.i("tag","onAddNewClick")
//                }
//            ){
//            onDataChange(initialData.copy(subcategory = it))
//                Log.i("tag","dropdown: $it")
//            }
        }
        PABasicButton(
            label1 = "Guardar",
            onClick1 = {
                onSaveClick()
            }
        )
    }

    PACustomAlertDialog(
        tittle = "Añadir nueva categoría",
        showDialog = showNewCategoryDialog
    ){
        onDataChange(
            initialData.copy(
                category = it,
                categoryList = initialData.categoryList + it
            )
        )
    }
    PACustomAlertDialog(
        tittle = "Añadir nueva subcategoría",
        showDialog = showNewSubcategoryDialog
    ){
        onDataChange(
            initialData.copy(
                subcategory = it,
                subcategoryList = initialData.subcategoryList + it
            )
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview(

){
    PAInfoProduct(
        initialData = PAInfoModel(),
        onFamilyDropDownSelect = {},
        onSaveClick = {},
        onScanClick = {},
        onImageClick = {}
    ){}
}