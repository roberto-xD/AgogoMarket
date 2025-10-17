package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passioagogo.market.ui.theme.onPrimaryLight
import com.passioagogo.market.ui.theme.primaryContainerLight
import com.passioagogo.market.ui.theme.secondaryContainerLight
import com.passioagogo.market.ui.theme.secondaryLight

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
    Card(
        modifier = modifier
            .fillMaxWidth()
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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