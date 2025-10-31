package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.ui.theme.backgroundLight

@Composable
fun ProductCard(
    id: Long,
    tittle: String,
    imagePath: String,
    sellPrice: String,
    onDetailClick: (id: Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .background(
                color = backgroundLight,
                shape = RoundedCornerShape(15.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        PAImageItem(
            imagePath = imagePath,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(
                    color = backgroundLight,
                    shape = RoundedCornerShape(15.dp)
                )
        ) {
            Text(
                text = tittle,
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(),
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2
            )
            if (sellPrice.isNotEmpty()) {
                Text(
                    text = sellPrice,
                    textAlign = TextAlign.Center,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    modifier = Modifier.
                        fillMaxWidth()
                )
            }
            PABasicButton(
                label1 = "Ver detalle",
                onClick1 = {
                    onDetailClick(id)
                }
            )
        }
    }
}

@Composable
@Preview
private fun ProductPre() {
    ProductCard(
        id = 1L,
        tittle = "Rose tongue licking & suction",
        imagePath = "",
        sellPrice = "$1423.35",
    ) {

    }
}