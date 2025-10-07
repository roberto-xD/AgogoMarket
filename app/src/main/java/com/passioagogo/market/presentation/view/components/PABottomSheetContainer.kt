package com.passioagogo.market.presentation.view.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PABottomSheetContainer(
    showBottomSheet: MutableState<Boolean>,
    showFullScreen: Boolean = false,
    content: @Composable () -> Unit
){
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = showFullScreen
    )

    if(showBottomSheet.value){
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet.value = false
            },
            sheetState = sheetState
        ){
            content.invoke()
        }
    }
}