package com.passioagogo.market.presentation.view.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.passioagogo.market.domain.bean.PAPriceBean
import com.passioagogo.market.domain.bean.PAProductBean

@Composable
fun ProductEditor(
    currentProduct: PAProductBean,
    updateItem:(item: PAProductBean) -> Unit,
){
    val context = LocalContext.current
    val editedProduct = remember {
        mutableStateOf(currentProduct)
    }

    Column {
        TextField(
            value = editedProduct.value.title,
            onValueChange = {
                editedProduct.value = editedProduct.value.copy(title= it)
            },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            label = {
                Text(text = "Nombre del producto")
            }
        )
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            TextField(
                value = editedProduct.value.price?.price_normal_og.orEmpty().removePrefix("$"),
                onValueChange = {
                    editedProduct.value = currentProduct.copy(price = editedProduct.value.price?.copy(price_normal_og = "$"+it))
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 3.dp),
                label = {
                    Text(text = "Precio original")
                },
                prefix = {
                    Text(text = "$")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextField(
                value = editedProduct.value.price?.price_og.orEmpty().removePrefix("$"),
                onValueChange = {
                    editedProduct.value = currentProduct.copy(price = editedProduct.value.price?.copy(price_og = "$"+it))
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 3.dp),
                label = {
                    Text(text = "Precio final")
                },
                prefix = {
                    Text(text = "$")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        TextField(
            value = editedProduct.value.category,
            onValueChange = {
                editedProduct.value = currentProduct.copy(category = it)
            },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            label = {
                Text(text = "Categoría del producto")
            }
        )
        TextField(
            value = editedProduct.value.description,
            onValueChange = {
                editedProduct.value = editedProduct.value.copy(description = it)
            },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            label = {
                Text(text = "Descripción del producto")
            },
            minLines = 3
        )
        Image(
            painter = rememberAsyncImagePainter(model ="https://www.distribuidoradesexshop.com"+currentProduct.image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .height(150.dp)
        )
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ){
            Button(
                onClick = {
                    updateItem(editedProduct.value)
                    Toast.makeText(context,"Actualizando",Toast.LENGTH_SHORT).show()
                }
            ) {
                Text(
                    text = "Actualizar información",
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview2(){
    ProductEditor(
        currentProduct = PAProductBean(
            title = "Desensibilizador para sexo oral \"Garganta profunda\"",
            description = "Spray oral de acción rápida con benzocaína.\n" +
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
            image = "",
            price = PAPriceBean(
                price_normal_og = "$300.00",
                price_og = ""
            ),
        )
    ){}
}
