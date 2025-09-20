package com.passioagogo.market.presentation.view.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.presentation.view.templates.PAEditInfoProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    onBackClick: () -> Unit,
){
    val editedProduct = remember {
        mutableStateOf(PAProductBean())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.label_item)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(5.dp)
                .fillMaxSize()
        ) {
            PAEditInfoProduct(editedProduct)
        }
    }
}