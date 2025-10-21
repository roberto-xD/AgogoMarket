package com.passioagogo.market.presentation.view.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PAToolbar(
    @DrawableRes leftIcon: Int = R.drawable.menu,
    onLeftClick: () -> Unit,
    @DrawableRes rightIcon: Int = R.drawable.edit_square,
    rightText: String? = null,
    onRightClick: (() -> Unit) ?= null,
    @DrawableRes centerIcon: Int = R.drawable.branding_passion_20,
    centerText: String ?= null,
    onCenterClick: (() -> Unit) ?= null,
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ){
                centerText?.let {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable {
                                onCenterClick?.invoke()
                            },
                        text = it,
                        fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                    )
                } ?: run {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clickable {
                                onCenterClick?.invoke()
                            },
                        painter = painterResource(
                            id = centerIcon
                        ),
                        contentDescription = null,
                    )
                }
            }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
            ){
                Icon(
                    painter = painterResource(id = leftIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(6.dp)
                        .align(Alignment.Center)
                        .clickable {
                            onLeftClick()
                        }
                )
            }
        },
        actions = {
            onRightClick?.let {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                ){
                    rightText?.let {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(3.dp)
                                .clickable {
                                    onRightClick.invoke()
                                },
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = MaterialTheme.typography.bodySmall.fontFamily ,
                        )
                    } ?: run {
                        Icon(
                            painter = painterResource(id = rightIcon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                                .padding(6.dp)
                                .clickable {
                                    onRightClick.invoke()
                                }
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth(),
    )
}

@Composable
@Preview
private fun Preview(){
    PAToolbar(
        centerText = "Titulo diferente",
        rightText = "Guardar",
        onLeftClick = {},
        onRightClick = {},
        onCenterClick = {}
    )
}