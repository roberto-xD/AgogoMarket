package com.passioagogo.market.presentation.view.templates

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.ui.utils.PAConstants.TAG_PG
import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.presentation.uiState.CatalogoUiState
import com.passioagogo.market.presentation.view.components.ImageView
import com.passioagogo.market.presentation.view.components.containers.PAContainer
import com.passioagogo.market.presentation.view.components.PACustomAlertDialog
import com.passioagogo.market.presentation.view.components.PADropDown
import com.passioagogo.market.presentation.view.components.PATextInput
import com.passioagogo.market.ui.decorators.toEditableString
import com.passioagogo.market.ui.decorators.toIntSafe
import com.passioagogo.market.ui.utils.PAConstants.NEW_CATEGORY
import com.passioagogo.market.ui.utils.PAConstants.NEW_SUBCATEGORY

@Composable
fun PAInfoProduct(
    modifier: Modifier = Modifier,
    initialData: ProductoDetallado,
    catalogData: CatalogoUiState,
    createNew: (param : Pair<String, String>) -> Unit,
    onImageClick: () -> Unit,
    onScanClick: () -> Unit,
    onDataChange: (ProductoDetallado) -> Unit,
){

    val details = remember { mutableStateOf(false) }
    val showNewCategoryDialog = remember { mutableStateOf(false) }
    val showNewSubcategoryDialog = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
    ){
        PAContainer(
            modifier = modifier,
        ) {
            Column(
                modifier = modifier
                    .padding(top = 20.dp),
            ) {
                PATextInput(
                    value = initialData.producto.nombre,
                    modifier = Modifier
                        .fillMaxWidth(),
                    maxLength = 30,
                    minLenght = 5,
                    placeHolder = stringResource(id = R.string.label_name),
                ){
                    onDataChange(initialData.copy(producto = initialData.producto.copy(nombre = it)))
                }
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    PATextInput(
                        value = initialData.producto.cantidadActual.toEditableString(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 5.dp)
                            .fillMaxWidth(),
                        maxLength = 4,
                        placeHolder = stringResource(id = R.string.label_actual_stock),
                        keyboardType = KeyboardType.Number
                    ){
                        onDataChange(initialData.copy(producto = initialData.producto.copy(cantidadActual = it.toIntSafe())))
                    }
                    PATextInput(
                        value = initialData.producto.precioVenta,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp)
                            .fillMaxWidth(),
                        placeHolder = stringResource(id = R.string.label_sell_price),
                    ){
                        onDataChange(initialData.copy(producto = initialData.producto.copy(precioVenta = it)))
                    }
                }
                PATextInput(
                    value = initialData.producto.codigoBarras.orEmpty(),
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeHolder = stringResource(id = R.string.label_codigo_de_barras),
                    trailingIcon = R.drawable.barcode_reader,
                    onTrailingIconClick = onScanClick
                ){
                    onDataChange(initialData.copy(producto = initialData.producto.copy(codigoBarras = it)))
                }
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        PAContainer(
            modifier = Modifier,
            isOpen = details.value,
            containerTittle = "Información detallada"
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = modifier
                        .padding(end = 5.dp)
                        .weight(1f)
                        .fillMaxHeight()
                ){
                    ImageView(
                        modifier = Modifier.fillMaxSize(),
                        imagePath = initialData.imagenes.firstOrNull()?.rutaImagen,
                        onImageClick = onImageClick
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .weight(1f)
                ) {
                    PATextInput(
                        value = initialData.producto.skuInterno,
                        modifier = Modifier,
                        placeHolder = stringResource(id = R.string.label_sku),
                    ){
                        onDataChange(initialData.copy(producto = initialData.producto.copy(skuInterno = it)))
                    }
                    PATextInput(
                        value = initialData.producto.precioCompra,
                        modifier = Modifier,
                        placeHolder = stringResource(id = R.string.label_buy_price),
                    ){
                        onDataChange(initialData.copy(producto = initialData.producto.copy(precioCompra = it)))
                    }
                }

            }

            PATextInput(
                value = initialData.producto.descripcion.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_description),
                minLines = 2,
                maxLines = 5,
                maxLength = 50,
            ){
                onDataChange(initialData.copy(producto = initialData.producto.copy(descripcion = it)))
            }

            PADropDown(
                item = catalogData.familias.find { it.id == initialData.idFamilia }?.descripcion.orEmpty(),
                items = catalogData.familias.map{it.descripcion},
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_family),
            ){ seleccion ->
                Log.i(TAG_PG,"dropdown: $seleccion")
                catalogData.familias.find { it.descripcion.equals(seleccion) }?.let { reasigned ->
                    onDataChange(initialData.copy(
                        idFamilia = reasigned.id,
                    ))
                }
            }

            if(initialData.idFamilia != 0L){
                PADropDown(
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeHolder = stringResource(id = R.string.label_categories),
                    item = catalogData.categorias.find { it.id == initialData.idCategoria }?.descripcion.orEmpty(),
                    items = catalogData.categorias.map{it.descripcion.orEmpty()},
                    onAddNewClick = {
                        showNewCategoryDialog.value = true
                    }
                ){ seleccion ->
                    Log.i(TAG_PG,"dropdown: $seleccion")
                    catalogData.categorias.find { it.descripcion.equals(seleccion) }?.let { reasigned ->
                        onDataChange(initialData.copy(
                            idCategoria = reasigned.id,
                        ))
                    }
                }
            }

            if(initialData.idCategoria != 0L){
                PADropDown(
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeHolder = stringResource(id = R.string.label_subcategories),
                    item = catalogData.subcategorias.find { it.id == initialData.idSubCategoria }?.descripcion.orEmpty(),
                    items = catalogData.subcategorias.map{it.descripcion.orEmpty()},
                    onAddNewClick = {
                        showNewSubcategoryDialog.value = true
                    }
                ){seleccion ->
                    Log.i(TAG_PG,"dropdown: $seleccion")
                    catalogData.subcategorias.find { it.descripcion.equals(seleccion) }?.let { reasigned ->
                        onDataChange(initialData.copy(
                            idSubCategoria = reasigned.id,
                        ))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        PAContainer(
            modifier = Modifier,
            isOpen = false,
            containerTittle = "Seguimiento del artículo"
        ) {

            PATextInput(
                value = initialData.producto.cantidadMinima.toString(),
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_reposition_quantity),
                keyboardType = KeyboardType.Number
            ){
                onDataChange(initialData.copy(producto = initialData.producto.copy(cantidadMinima = it.toIntSafe())))
            }

        }
    }

    PACustomAlertDialog(
        tittle = "Añadir nueva categoría",
        showDialog = showNewCategoryDialog
    ){
        Log.i(TAG_PG,"categoria para crear: $it")
        showNewCategoryDialog.value = false
        createNew(Pair(NEW_CATEGORY,it))
    }

    PACustomAlertDialog(
        tittle = "Añadir nueva subcategoría",
        showDialog = showNewSubcategoryDialog
    ){
        Log.i(TAG_PG,"subcategoria para crear: $it")
        showNewSubcategoryDialog.value = false
        createNew(Pair(NEW_SUBCATEGORY,it))
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview(

){
    PAInfoProduct(
        initialData = ProductoDetallado(),
        catalogData = CatalogoUiState(),
        createNew = {},
        onScanClick = {},
        onImageClick = {}
    ){}
}