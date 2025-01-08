package com.passioagogo.market.presentation.view.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.passioagogo.market.R
import kotlin.math.round

@Composable
fun ProductCard(
    id: String,
    tittle : String,
    urlImage: String,
    finalPrice: String,
    originalPrice: String? = null,
    onStock: Boolean = true,
    discount: String? = null,
    onDetailClick: (id: String) -> Unit,
){
    Column(
        modifier = Modifier
            .background(
                color = colorResource(id = R.color.gray),
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        if(onStock){
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
        }
        discount?.let {
            if(it.isNotEmpty()){
                Box (
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ){
                    Text(
                        text = "Descuento ${it}%",
                        modifier = Modifier.padding(end = 5.dp),
                    )
                }
            }
        }
        Image(
            painter = rememberAsyncImagePainter(model ="https://www.distribuidoradesexshop.com"+urlImage),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(5.dp)
                .height(200.dp)
        )
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ){
            Text(
                text = tittle,
                modifier = Modifier
                    .aspectRatio(3f)
                    .padding(bottom = 5.dp),
                maxLines = 3,

            )
            Row {
                originalPrice?.let {
                    if(it.isNotEmpty() && it.equals(finalPrice).not()){
                        Text(
                            text = it,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(end = 5.dp),
                        )
                    }
                }
                if(finalPrice.isNotEmpty()){
                    Text(
                        text = finalPrice,
                        color = colorResource(id = R.color.purple_200),
                        modifier = Modifier
                    )
                }
            }
        }
        Box (
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            Button(
                onClick = {
                    onDetailClick(id)
                }
            ) {
                Text(text = "Ver detalle")
            }
        }

    }
}

@Composable
@Preview
fun Preview(){
    ProductCard(
        id = "cuac",
        discount = "",
        onStock = false,
        tittle = "Rose tongue licking & suction",
        urlImage = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQKqtZRN86wSXbhdhZteqKTyfOAASDirHwrldktjgnn9rCqWLIXCY9nQwbB79DrGhCtiL0&usqp=CAU",
        finalPrice = "",
        originalPrice = ""
    ){}
}