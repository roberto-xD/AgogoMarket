package com.passioagogo.market.presentation.view.screens


import androidx.compose.runtime.Composable
import com.passioagogo.market.presentation.view.components.PADrawerSheet

@Composable
fun SearchScreen(
    navigate: (route: String) -> Unit,
){
    PADrawerSheet(
        navigate = navigate,
        toolbarCenter = {}
    ) {

    }
}