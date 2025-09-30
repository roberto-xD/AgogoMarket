package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passioagogo.market.ui.theme.bungeeRegular
import com.passioagogo.market.ui.theme.onPrimaryLight
import com.passioagogo.market.ui.theme.primaryContainerLight
import com.passioagogo.market.ui.theme.primaryLight
import com.passioagogo.market.ui.theme.secondaryContainerLight
import com.passioagogo.market.ui.theme.secondaryLight
import com.passioagogo.market.ui.theme.surfaceLight

@Composable
fun PAContainer(
    modifier: Modifier = Modifier,
    isOpen: Boolean = true,
    containerTittle: String ?= null,
    content: @Composable () -> Unit,
){
    val checked = remember {
        mutableStateOf(isOpen)
    }
    Box(
        modifier = modifier
            .background(color = surfaceLight)
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
                        color = primaryLight,
                        fontSize = 20.sp,
                        fontFamily = bungeeRegular,
                        text = it
                    )
                    Switch(
                        modifier = Modifier
                            .scale(0.5f),
                        checked = checked.value,
                        onCheckedChange = {
                            checked.value = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = onPrimaryLight,
                            checkedTrackColor = primaryContainerLight,
                            uncheckedThumbColor = secondaryLight,
                            uncheckedTrackColor = secondaryContainerLight,
                        )
                    )
                }
            }
            if(checked.value){
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    content.invoke()
                }
            }
        }
    }
}