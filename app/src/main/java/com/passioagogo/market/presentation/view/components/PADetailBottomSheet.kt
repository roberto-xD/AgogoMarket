package com.passioagogo.market.presentation.view.components

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.PAPriceBean
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.presentation.viewModel.VM

@Composable
fun DetailBottomSheet(
    enableEdit: Boolean = false,
    currentProduct: PAProductBean,
    hideModal: (updateList: Boolean) -> Unit,
){
    if(enableEdit){
        EditDetails(
            currentProduct = currentProduct,
            hideModal = hideModal
        )
    }else{
        ShowDetails(
            currentProduct = currentProduct,
        )
    }
}

@Composable
fun EditDetails(
    viewModel: VM = hiltViewModel(),
    currentProduct: PAProductBean,
    hideModal: (updateList: Boolean) -> Unit,
){
    val context = LocalContext.current
    val editedProduct = remember {
        mutableStateOf(currentProduct)
    }


    Column {
        TextField(
            value = editedProduct.value.title,
            onValueChange = {
                editedProduct.value = currentProduct.copy(title= it)
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
                    editedProduct.value = currentProduct.copy(price = currentProduct.price?.copy(price_normal_og = "$"+it))
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
                    editedProduct.value = currentProduct.copy(price = currentProduct.price?.copy(price_og = "$"+it))
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
                editedProduct.value = currentProduct.copy(description = it)
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
                    viewModel.updateInfoData(editedProduct.value){
                        hideModal(true)
                    }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShowDetails(
    currentProduct: PAProductBean,
){
    val context = LocalContext.current
    val clipBoardManager = LocalClipboardManager.current
    val scroll = rememberScrollState()
    val title = currentProduct.title
    val urlImage = currentProduct.image
    val originalPrice = currentProduct.price?.price_og.orEmpty()
    val finalPrice = currentProduct.price?.price_normal_og.orEmpty()
    val description = currentProduct.description

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(10.dp)
        )
        Image(
            painter = rememberAsyncImagePainter(model ="https://www.distribuidoradesexshop.com"+urlImage),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f)
                .padding(5.dp)
                .height(150.dp)
        )
        Row(
            modifier = Modifier.padding(10.dp)
        ) {
            if(originalPrice.isNotEmpty() && originalPrice.equals(finalPrice).not()){
                Text(
                    text = originalPrice,
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    textDecoration = TextDecoration.LineThrough,
                    textAlign = TextAlign.Center
                )
            }
            if(finalPrice.isNotEmpty()){
                Text(
                    text = finalPrice,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.purple_200),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        Text(
            text = description,
            modifier = Modifier
                .padding(10.dp)
                .aspectRatio(1f)
                .verticalScroll(scroll)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        val fullText = title + "\n\n" + description + "\n\n Precio:" + finalPrice
                        val clipData = ClipData.newPlainText("descripcion", fullText)
                        val clipEntry = ClipEntry(clipData)
                        clipBoardManager.setClip(clipEntry)
                        Toast
                            .makeText(context, "copiado", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
        )
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ){
            Button(
                onClick = {
                    Toast.makeText(context,"Añadido a la bolsa",Toast.LENGTH_SHORT).show()
                }
            ) {
                Text(
                    text = "Añadir a la bolsa",
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview1(){
    ShowDetails(
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
    )
}

@Composable
@Preview
private fun Preview2(){
    EditDetails(
        viewModel = hiltViewModel(),
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