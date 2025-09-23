package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passioagogo.market.ui.theme.onPrimaryContainerLight
import com.passioagogo.market.ui.theme.primaryLight
import com.passioagogo.market.ui.theme.primaryLightMediumContrast
import com.passioagogo.market.ui.theme.surfaceDimLightMediumContrast

@Composable
fun PABasicButton(
    label1: String,
    onClick1: ()->Unit,
){
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            colors = ButtonColors(
                containerColor = primaryLight,
                contentColor = onPrimaryContainerLight,
                disabledContainerColor = surfaceDimLightMediumContrast,
                disabledContentColor = primaryLightMediumContrast
            ),
            onClick = {
                onClick1()
            }
        ) {
            Text(text = label1)
        }
    }
}