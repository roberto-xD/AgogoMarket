package com.passioagogo.market.presentation.view.components

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R

@Composable
fun Splash(){
    val transition = rememberInfiniteTransition()
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 25.dp),
            painter = painterResource(
                id = R.drawable.branding_passion_20),
            colorFilter = ColorFilter.tint(Color.Magenta),
            contentDescription = null,
        )
    }
}