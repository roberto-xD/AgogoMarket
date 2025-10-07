package com.passioagogo.market.presentation.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.presentation.view.models.PAInfoModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(
) : ViewModel() {

    val uiState: MutableState<PAInfoModel> =  mutableStateOf(PAInfoModel())

    fun setProductData(
        data: ProductoDetallado
    ){
        uiState.value = uiState.value.update(data)
    }

    fun setFamilyList(
        family : MutableList<String>
    ){
        uiState.value = uiState.value.copy(
            familyList = family
        )
    }

    fun setCategoryList(
        category : MutableList<String>
    ){
        uiState.value = uiState.value.copy(
            categoryList = category
        )
    }

    fun setImagePaths(
        pathImageList : MutableList<String>
    ){
        uiState.value = uiState.value.copy(
            pathImageList = pathImageList
        )
    }

    fun setBarCode(
        barCode : String
    ) {
        uiState.value = uiState.value.copy(
            codigoBarra = mutableStateOf(barCode)
        )
    }

}
