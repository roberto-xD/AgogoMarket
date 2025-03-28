package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun GenericInput(
    placeHolder : String ?= null,
    value: (String)->Unit
){

    val inputValue = remember {
        mutableStateOf("")
    }
    OutlinedTextField(
        shape = RoundedCornerShape(10.dp),
        value = inputValue.value,
        onValueChange = {
            inputValue.value = it
            value(inputValue.value)
        },
        label = {
            Text(
                text = placeHolder.orEmpty()
            )
        },
        placeholder = {
            Text(
                text = placeHolder.orEmpty()
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            // disabledTextColor =
        )
    )
}

@Composable
@Preview
private fun Preview(

){
    GenericInput(
        placeHolder = "este es un place holder",
    ){}
}