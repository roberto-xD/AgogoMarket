package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.passioagogo.market.R

@Composable
fun ProductCard(
    id: String,
    tittle: String,
    urlImage: String,
    finalPrice: String,
    originalPrice: String? = null,
    onStock: Boolean = true,
    discount: String? = null,
    onDetailClick: (id: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(3.dp)
            .background(
                color = colorResource(id = R.color.white),
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        if (onStock) {
            Column(
                modifier = Modifier
                    .background(
                        color = colorResource(id = R.color.gray),
                        shape = RoundedCornerShape(15.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = "En Existencia",
                        modifier = Modifier.weight(2.0f),
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
                discount?.let {
                    if (it.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "Descuento ${it}%",
                                modifier = Modifier.padding(end = 5.dp),
                            )
                        }
                    }
                }
            }
        }

        Image(
            painter = rememberAsyncImagePainter(model = "https://www.distribuidoradesexshop.com" + urlImage),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.white),
                    RoundedCornerShape(15.dp)
                )
                .padding(5.dp)
                .height(200.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.gray),
                    shape = RoundedCornerShape(15.dp)
                )
        ) {
            Text(
                text = tittle,
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(),
                maxLines = 3,
                minLines = 3
            )
            Row(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth()
            ) {
                originalPrice?.let {
                    if (it.isNotEmpty() && it.equals(finalPrice).not()) {
                        Text(
                            text = it,
                            textDecoration = TextDecoration.LineThrough,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .weight(1f),
                        )
                    }
                }
                if (finalPrice.isNotEmpty()) {
                    Text(
                        text = finalPrice,
                        color = colorResource(id = R.color.primary),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    modifier = Modifier.padding(5.dp),
                    onClick = {
                        onDetailClick(id)
                    }
                ) {
                    Text(text = "Ver detalle")
                }
            }
        }
    }
}

@Composable
@Preview
private fun ProductPre() {
    ProductCard(
        id = "cuac",
        discount = "45",
        onStock = true,
        tittle = "Rose tongue licking & suction",
        urlImage = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQKqtZRN86wSXbhdhZteqKTyfOAASDirHwrldktjgnn9rCqWLIXCY9nQwbB79DrGhCtiL0&usqp=CAU",
        finalPrice = "1423.35",
        originalPrice = "2000"
    ) {

    }
}