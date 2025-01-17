package com.passioagogo.market.presentation.view.screens


import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.presentation.view.components.DrawerSheet
import com.passioagogo.market.presentation.viewModel.VM

@Composable
fun SearchScreen(
    viewModel: VM = hiltViewModel(),
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