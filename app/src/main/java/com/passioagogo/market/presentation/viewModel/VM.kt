package com.passioagogo.market.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.passioagogo.market.domain.PAConstants.TAG_PG
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.PAGetProductInfoUseCase
import com.passioagogo.market.domain.usecase.PANewProductUseCase
import com.passioagogo.market.domain.usecase.PASaveProductInfoUseCase
import com.passioagogo.market.domain.usecase.PASearchProductInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VM @Inject constructor(
    private val productInfo: PAGetProductInfoUseCase,
    private val saveInfo: PASaveProductInfoUseCase,
    private val newProduct: PANewProductUseCase,
    private val searchProduct: PASearchProductInfoUseCase,
) : ViewModel(){
    private val _productData = MutableStateFlow(listOf<PAProductBean>())
    val productData: StateFlow<List<PAProductBean>> = _productData
    private val _loader = MutableStateFlow(false)
    val loader : StateFlow<Boolean> = _loader
    private val lastQuery = MutableStateFlow((listOf<DocumentSnapshot>()))
    val isEditor = MutableStateFlow(false)


    fun getProductData(
        filter: String = "store",
        value: String = "sexshop",
        limit: Long ?= null,
        startAfterLast: Boolean = false,
    ){
        viewModelScope.launch {
            productInfo(
                field = filter,
                value = value,
                limit = limit,
                lastDocument = if(startAfterLast && lastQuery.value.size != 0)lastQuery.value.get(lastQuery.value.size -1) else null,
            ){  response ->
                when(response){
                    is PADomainState.Success -> {
                        _loader.value = false
                        response.data?.documents?.let {
                            lastQuery.value = it
                        }
                        val plop = response.data?.map {
                            it.toObject(PAProductBean::class.java).apply { id = it.id }
                        }
                        plop?.let {
                            if(_productData.value.containsAll(it).not()){
                                val newList = ArrayList(_productData.value)
                                newList.addAll(it)
                                _productData.value = newList
                            }
                        }
                    }
                    is PADomainState.Error -> {
                        _loader.value = false
                        Log.i(TAG_PG,"viewmodel error: ${response.error}")
                    }
                    is PADomainState.Loading -> {
                        _loader.value = true
                        Log.i(TAG_PG,"viewmodel is loading: ${response.isLoading}")
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
                        Log.i(TAG_PG,"viewmodel success: ${response.data}")
                        response.data?.let {
                            success()
                        }
                    }
                    is PADomainState.Error -> {
                        Log.i(TAG_PG,"viewmodel error: ${response.error}")
                    }
                    is PADomainState.Loading -> {
                        Log.i(TAG_PG,"viewmodel is loading: ${response.isLoading}")
                    }
                }
            }
        }
    }

    fun addNewProduct(
        infoProduct : PAProductBean,
        success:() -> Unit
    ){
        viewModelScope.launch{
            newProduct(
                infoProduct = infoProduct
            ){response->
                when(response){
                    is PADomainState.Success -> {
                        Log.i(TAG_PG,"viewmodel new success: ${response.data}")
                        response.data?.let {
                            success()
                        }
                    }
                    is PADomainState.Error -> {
                        Log.i(TAG_PG,"viewmodel error: ${response.error}")
                    }
                    is PADomainState.Loading -> {
                        Log.i(TAG_PG,"viewmodel is loading: ${response.isLoading}")
                    }
                }

            }
        }
    }

    fun searchItems(
        filter: String = "store",
        value: String = "sexshop",
        limit: Long ?= null,
    ){
        viewModelScope.launch {
            searchProduct(
                field = filter,
                value = value,
                limit = limit,
            ){  response ->
                when(response){
                    is PADomainState.Success -> {
                        _loader.value = false
                        response.data?.let {query ->
                            _productData.value = query.map {
                                it.toObject(PAProductBean::class.java).apply { id = it.id }
                            }
                        }
                    }
                    is PADomainState.Error -> {
                        _loader.value = false
                        Log.i(TAG_PG,"viewmodel error: ${response.error}")
                    }
                    is PADomainState.Loading -> {
                        _loader.value = true
                        Log.i(TAG_PG,"viewmodel is loading: ${response.isLoading}")
                    }
                }
            }
        }
    }
}