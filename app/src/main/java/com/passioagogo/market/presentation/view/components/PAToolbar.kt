package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Toolbar(
    addItem: () -> Unit,
    showDrawer: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.add_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.Magenta),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .padding(6.dp)
                .clickable {
                    showDrawer()
                }
        )
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .padding(top = 5.dp),
            painter = painterResource(
                id = R.drawable.branding_passion_20
            ),
            colorFilter = ColorFilter.tint(Color.Black),
            contentDescription = null,
        )
        Image(
            painter = painterResource(id = R.drawable.add_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.Magenta),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
                .padding(6.dp)
                .clickable {
                    addItem()
                }
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Toolbar(
        showDrawer = {},
        addItem = {}
    )
}