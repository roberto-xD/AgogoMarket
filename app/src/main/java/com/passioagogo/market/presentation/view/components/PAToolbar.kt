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
    @DrawableRes navigationIcon: Int = R.drawable.list_24,
    onNavigationClick: () -> Unit,
    @DrawableRes actionIcon: Int = R.drawable.layer_plus_24,
    actionText: String? = null,
    onActionIconClick: () -> Unit,
    @DrawableRes tittleIcon: Int = R.drawable.branding_passion_20,
    tittle: String ?= null,
    onTittleClick: () -> Unit
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ){
                tittle?.let {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable {
                                onTittleClick()
                            },
                        text = it
                    )
                } ?: run {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clickable {
                                onTittleClick()
                            },
                        painter = painterResource(
                            id = tittleIcon
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
                    painter = painterResource(id = navigationIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(6.dp)
                        .align(Alignment.Center)
                        .clickable {
                            onNavigationClick()
                        }
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
            ){
                actionText?.let {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(3.dp)
                            .clickable {
                                onActionIconClick()
                            },
                        text = it
                    )
                } ?: run {
                    Icon(
                        painter = painterResource(id = actionIcon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                            .padding(6.dp)
                            .clickable {
                                onActionIconClick()
                            }
                    )
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
        tittle = "Titulo diferente",
        actionText = "Guardar",
        onNavigationClick = {},
        onActionIconClick = {},
        onTittleClick = {}
    )
}