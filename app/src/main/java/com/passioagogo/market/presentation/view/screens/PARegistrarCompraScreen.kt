package com.passioagogo.market.presentation.view.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.presentation.view.components.containers.PAContainer
import com.passioagogo.market.presentation.view.components.PADrawerSheet
import com.passioagogo.market.presentation.view.components.ProveedorSection
import com.passioagogo.market.presentation.viewModel.proveedor.ProveedoresViewModel


@Composable
fun RegistrarCompraScreen(
    viewModel: ProveedoresViewModel = hiltViewModel(),
    navigate: (route: String) -> Unit,
){
    PADrawerSheet(
        navigate = navigate,
        toolbarCenter = {

        }
    ){  padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(top = 10.dp)
        ) {
            PAContainer(
                modifier = Modifier,
                isOpen = true,
            ){
                ProveedorSection(
                    proveedorModel = viewModel.uiState.value.proveedorModel,
                ) {
                    viewModel.actualizarItem(it)
                }
            }
        }
    }
}

@Composable
@Preview
private fun preview(){
    RegistrarCompraScreen(){}
}