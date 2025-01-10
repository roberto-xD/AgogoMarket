package com.passioagogo.market.presentation.view.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.presentation.viewModel.VM

@Composable
fun DetailBottomSheet(
    viewModel: VM = hiltViewModel(),
    enableEdit: Boolean = false,
    currentProduct: PAProductBean,
    hideModal: (updateList: Boolean) -> Unit,
){
    val edit = remember {
        mutableStateOf(enableEdit)
    }
    if(edit.value){
        ProductEditor(
            currentProduct = currentProduct,
        ){
            viewModel.updateInfoData(it){
                hideModal(true)
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
