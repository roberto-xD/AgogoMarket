package com.passioagogo.market.presentation.view.templates

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.PAPriceBean
import com.passioagogo.market.domain.bean.PAProductBean

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductDetails(
    currentProduct: PAProductBean,
    changeView: () -> Unit
) {
    val context = LocalContext.current
    val clipBoardManager = LocalClipboardManager.current
    val scroll = rememberScrollState()
    val title = currentProduct.title.orEmpty()
    val urlImage = currentProduct.image.orEmpty()
    val originalPrice = currentProduct.price?.public.orEmpty()
    val finalPrice = currentProduct.price?.discount.orEmpty()
    val description = currentProduct.description.orEmpty()
    val category = currentProduct.category.orEmpty()
    val isActive = currentProduct.isActive ?: false
    val code = currentProduct.sku.orEmpty()

    Column {
        Text(
            text = title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        )
        if (isActive) {
            Row(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = "Existencias:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(end = 3.dp)
                        .weight(1f)
                )
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .weight(1f)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f)
                .padding(5.dp)
                .height(150.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = "https://www.distribuidoradesexshop.com" + urlImage),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
            )
            Image(
                painter = painterResource(id = R.drawable.edit_24),
                colorFilter = ColorFilter.tint(Color.Black),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .clickable {
                        changeView()
                    }
            )
        }

        Row(
            modifier = Modifier.padding(3.dp)
        ) {
            if (originalPrice.isNotEmpty() && originalPrice.equals(finalPrice).not()) {
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
            if (finalPrice.isNotEmpty()) {
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
        if (category.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(3.dp)
            ) {
                Text(
                    text = "Categoria:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(end = 3.dp)
                        .weight(1f)
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .weight(1f)
                )
            }
        }
        if (code.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(3.dp)
            ) {
                Text(
                    text = "Código:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(end = 3.dp)
                        .weight(1f)
                )
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .weight(1f)
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
        /*Box (
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
        }*/
    }
}

@Composable
@Preview
private fun Preview1() {
    ProductDetails(
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
            sku = "10234",
            category = "BDSM",
            price = PAPriceBean(
                public = "$300.00",
                discount = ""
            ),
        )
    ) {}
}
