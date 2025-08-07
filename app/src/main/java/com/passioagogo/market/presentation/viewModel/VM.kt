package com.passioagogo.market.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.passioagogo.market.domain.PAConstants.TAG_PG
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.PAGetProductInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VM @Inject constructor(
    private val productInfo: PAGetProductInfoUseCase,

) : ViewModel(){
    private val _productData = MutableStateFlow(listOf<PAProductBean>())
    val productData: StateFlow<List<PAProductBean>> = _productData
    private val _loader = MutableStateFlow(false)
    val loader : StateFlow<Boolean> = _loader
    private val lastQuery = MutableStateFlow((listOf<DocumentSnapshot>()))
    val isEditor = MutableStateFlow(false)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun addNewProduct(
        infoProduct : PAProductBean,
    ){
        viewModelScope.launch(Dispatchers.IO){
            Log.i(TAG_PG,"viewmodel success: ${infoProduct.title}")
//            savelocalProduct(infoProduct).collect{
//                Log.i(TAG_PG,"local item: $it")
//            }
        }
    }

    fun getLocalProducts(){
        Log.i(TAG_PG,"call get")
        viewModelScope.launch {
            Log.i(TAG_PG,"on launch")
//            getAllLocalProducts().collect {
//                Log.i(TAG_PG,"get all local success: ${it}")
//            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun searchLocalProducts(
        searchTerm: String
    ) {
        Log.i(TAG_PG,"searchTerm: $searchTerm ")
        _searchQuery.value = searchTerm
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
//                .flatMapLatest { query->
//                    Log.i(TAG_PG,"flat query: $query")
//                    searchProductLocal(query)
//                }
                .collect { result ->
                    Log.i(TAG_PG,"search success: ${result}")
                }
        }
    }

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
                    }
                    is PADomainState.Loading -> {
                        _loader.value = true
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

        }
    }

    fun addNewProduct(
        infoProduct : PAProductBean,
        success:() -> Unit
    ){
        viewModelScope.launch{

        }
    }

    fun searchItems(
        filter: String = "store",
        value: String = "sexshop",
        limit: Long ?= null,
    ){
        viewModelScope.launch {

        }
    }
}