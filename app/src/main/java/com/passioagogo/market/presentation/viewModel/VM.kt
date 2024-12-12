package com.passioagogo.market.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.usecase.PAGetProductInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VM @Inject constructor(
    private val productInfo: PAGetProductInfoUseCase
) : ViewModel(){


    fun getProductData(){
        viewModelScope.launch {
            productInfo().collect{

            }
        }
    }
}