package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PAContainer(
    modifier: Modifier = Modifier,
    containerTittle: String ?= null,
    content: @Composable () -> Unit,
){
    val checked = remember {
        mutableStateOf(true)
    }
    Box(
        modifier = modifier
            .background(color = Color(0xFFFFFFFF))
            .clip(shape = RoundedCornerShape(4.dp))
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(4.dp))
    ){
        Column(
            modifier = Modifier
        ) {
            containerTittle?.let {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        text = it
                    )
                    Switch(
                        modifier = Modifier
                            .scale(0.5f),
                        checked = checked.value,
                        onCheckedChange = {
                            checked.value = it
                        }
                    )
                }
            }
            if(checked.value){
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                ) {
                    content.invoke()
                }
            }
        }
    }
}