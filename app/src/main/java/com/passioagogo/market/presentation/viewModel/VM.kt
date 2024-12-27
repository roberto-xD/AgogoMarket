package com.passioagogo.market.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.PAGetProductInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VM @Inject constructor(
    private val productInfo: PAGetProductInfoUseCase
) : ViewModel(){
    private val _productData = MutableStateFlow(listOf<PAProductBean>())
    val productData: StateFlow<List<PAProductBean>> = _productData

    fun getProductData(){
        viewModelScope.launch {
            productInfo(
                field = "store",
                value = "sexshop",
                limit = 3
            ){  response ->
                when(response){
                    is PADomainState.Success -> {
                        Log.i("tag","viewmodel success: ${response.data}")
                        response.data?.let {
                            _productData.value = it
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