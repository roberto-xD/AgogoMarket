package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun PACustomAlertDialog(
    tittle: String,
    showDialog : MutableState<Boolean> = remember { mutableStateOf(false) },
    onConfirmClick : (input : String) -> Unit,
){
    val inputText : MutableState<String> = remember { mutableStateOf("") }

    if(showDialog.value){
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(tittle) },
            text = {
                PATextInput(
                    mutableInput = inputText,
                    modifier = Modifier
                        .fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmClick.invoke(inputText.value)
                        showDialog.value = false
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog.value = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}