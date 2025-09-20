package com.passioagogo.market.presentation.view.templates

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.usecase.producto.CrearProductoParams

@Composable
fun ProductEditor(
    currentProduct: Producto,
    updateItem: (item: CrearProductoParams?) -> Unit,
) {
    val context = LocalContext.current


    Column {
//        PATextInput(
//            value = editedProduct.value.nombre,
//            onValueChange = {
//                editedProduct.value = editedProduct.value.copy(nombre = it.orEmpty())
//            },
//            modifier = Modifier
//                .padding(10.dp)
//                .fillMaxWidth(),
//            placeHolder = stringResource(id = R.string.label_name),
//        )
        /*Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            PATextInput(
                value = editedProduct.value.price?.public?.removePrefix("$"),
                onValueChange = {
                    editedProduct.value =
                        editedProduct.value.updatePrice(public = "$"+it)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 3.dp),
                placeHolder = stringResource(id = R.string.label_original_price),
                keyboardType = KeyboardType.Number
            )
            PATextInput(
                value = editedProduct.value.price?.discount?.removePrefix("$"),
                onValueChange = {
                    editedProduct.value =
                        editedProduct.value.updatePrice(discount = "$"+it)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 3.dp),
                placeHolder = stringResource(id = R.string.label_final_price),
                keyboardType = KeyboardType.Number
            )
        }*/
        /*PATextInput(
            value = editedProduct.value.category,
            onValueChange = {
                editedProduct.value = editedProduct.value.copy(category = it)
            },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            placeHolder = stringResource(id = R.string.label_category)
        )*/
//        PATextInput(
//            value = editedProduct.value.skuInterno,
//            onValueChange = {
//                editedProduct.value = editedProduct.value.copy(skuInterno = it.orEmpty())
//            },
//            modifier = Modifier
//                .padding(10.dp)
//                .fillMaxWidth(),
//            placeHolder = stringResource(id = R.string.label_code)
//        )
//        PATextInput(
//            value = editedProduct.value.descripcion,
//            onValueChange = {
//                editedProduct.value = editedProduct.value.copy(descripcion = it)
//            },
//            modifier = Modifier
//                .padding(10.dp)
//                .fillMaxWidth(),
//            placeHolder = stringResource(id = R.string.label_description),
//        )
        /*Image(
            painter = rememberAsyncImagePainter(model = "https://www.distribuidoradesexshop.com" + currentProduct.image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .height(150.dp)
        )*/
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
//                    updateItem(editedProduct.value)
                    Toast.makeText(context, "Actualizando", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text(
                    text = if (currentProduct.id != 0L) {
                        stringResource(id = R.string.update_info)
                    } else {
                        stringResource(id = R.string.add_new_item)
                    },
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview2() {
    ProductEditor(
        currentProduct = Producto(
            nombre = "Desensibilizador para sexo oral \"Garganta profunda\"",
            descripcion = "Spray oral de acción rápida con benzocaína.\n" +
                    "\n" +
                    "\uD83D\uDE08Desensibiliza ligeramente la garganta para un juego aún más profundo.\n" +
                    "\n" +
                    " Está formulado naturalmente para una deliciosa experiencia de placer. \n" +
                    "\n" +
                    "La fórmula de acción rápida adormece\uD83D\uDE34 ligeramente la garganta \uD83D\uDE35\uD83D\uDE35 con benzocaína natural con un toque sabroso de menta verde\uD83C\uDF43\uD83C\uDF43 refrescante. \n" +
                    "\n" +
                    "La sensación de adormecimiento se desvanece naturalmente con el tiempo.\n" +
                    "\n" +
                    " Cont. Neto 60 ml.",
            precioVenta = 100.0,
            precioCompra = 50.0
        )
    ) {}
}
