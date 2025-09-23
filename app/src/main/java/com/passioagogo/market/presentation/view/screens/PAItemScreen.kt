package com.passioagogo.market.presentation.view.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.presentation.view.components.PABottomSheetContainer
import com.passioagogo.market.presentation.view.templates.PAEditInfoProduct
import com.passioagogo.market.presentation.view.templates.ProductDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    onBackClick: () -> Unit,
){
    val editedProduct = remember {
        mutableStateOf(PAProductBean())
    }
    val context = LocalContext.current
    val showBottomSheet = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceBright),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.label_item),
                        fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(5.dp)
                .fillMaxSize()
        ) {
            PAEditInfoProduct(
                editedProduct = editedProduct,
                onImageClick = {
                    showBottomSheet.value = true
                    Toast
                        .makeText(context, "abrir imagen", Toast.LENGTH_SHORT)
                        .show()
                },
                onScanClick = {
                    Toast
                        .makeText(context, "abrir escaner", Toast.LENGTH_SHORT)
                        .show()
                }
            )
        }
        PABottomSheetContainer(
            showBottomSheet = showBottomSheet
        ) {
            ProductDetails(
                currentProduct = Producto(),
                changeView = {

                }
            )
        }
    }
}