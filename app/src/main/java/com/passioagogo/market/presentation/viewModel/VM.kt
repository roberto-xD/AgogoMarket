package com.passioagogo.market.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.PAGetProductInfoUseCase
import com.passioagogo.market.domain.usecase.PASaveProductInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VM @Inject constructor(
    private val productInfo: PAGetProductInfoUseCase,
    private val saveInfo: PASaveProductInfoUseCase,
) : ViewModel(){
    private val _productData = MutableStateFlow(listOf<PAProductBean>())
    val productData: StateFlow<List<PAProductBean>> = _productData
    private val _productUpdateSuccess = MutableStateFlow(false)
    val productUpdateSuccess : StateFlow<Boolean> = _productUpdateSuccess
    val isEditor = MutableStateFlow(false)

    fun getProductData(
        filter: String = "store",
        value: String = "sexshop",
        limit: Long ?= null,
        orderBy: String ?= null,
        lastDocument: String ?= null,
    ){
        viewModelScope.launch {
            productInfo(
                field = filter,
                value = value,
                limit = limit,
                orderBy = orderBy,
                lastDocument = lastDocument,
            ){  response ->
                when(response){
                    is PADomainState.Success -> {
                        Log.i("tag","viewmodel success: ${response.data}")
                        response.data?.let {
                            if(_productData.value.containsAll(it).not()){
                                val newList = ArrayList(_productData.value)
                                newList.addAll(it)
                                _productData.value = newList
                            }
                        }
                    }
                    is PADomainState.Error -> {
                        Log.i("tag","viewmodel error: ${response.error}")
                    }
                    is PADomainState.Loading -> {
                        Log.i("tag","viewmodel is loading: ${response.isLoading}")
                    }
                }
            }
        }
    }

    fun updateInfoData(
        infoProduct : PAProductBean,
        success:() -> Unit
    ){
        viewModelScope.launch{
            saveInfo(
                infoProduct = infoProduct
            ){ response->
                when(response){
                    is PADomainState.Success -> {
                        Log.i("tag","viewmodel success: ${response.data}")
                        response.data?.let {
                            success()
                        }
                    }
                    is PADomainState.Error -> {
                        Log.i("tag","viewmodel error: ${response.error}")
                    }
                    is PADomainState.Loading -> {
                        Log.i("tag","viewmodel is loading: ${response.isLoading}")
                    }
                }
            }
        }
    }
}