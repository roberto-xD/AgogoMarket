package com.passioagogo.market.presentation.view.screens


import androidx.compose.runtime.Composable
import com.passioagogo.market.presentation.view.components.DrawerSheet

@Composable
fun SearchScreen(
    navigate: (route: String) -> Unit,
){
    DrawerSheet(
        addItem = {

        },
        navigate = navigate,
        search = {}
    ) {

    }
}