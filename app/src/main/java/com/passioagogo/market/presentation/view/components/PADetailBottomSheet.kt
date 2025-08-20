package com.passioagogo.market.presentation.view.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.presentation.view.templates.ProductDetails
import com.passioagogo.market.presentation.view.templates.ProductEditor
import com.passioagogo.market.presentation.viewModel.VM
import com.passioagogo.market.presentation.viewModel.products.ProductosViewModel

@Composable
fun DetailBottomSheet(
    productViewModel: ProductosViewModel = hiltViewModel(),
    enableEdit: Boolean = false,
    currentProduct: Producto,
    hideModal: (updateList: Boolean) -> Unit,
){
    val edit = remember {
        mutableStateOf(enableEdit)
    }
    if(edit.value || currentProduct.id == 0L){
        ProductEditor(
            currentProduct = currentProduct,
        ){ productoNuevo ->
            productoNuevo?.let {
                productViewModel.crearProducto(it)
            }
        }
    }else{
        ProductDetails(
            currentProduct = currentProduct,
        ){
            edit.value = true
        }
    }
}
