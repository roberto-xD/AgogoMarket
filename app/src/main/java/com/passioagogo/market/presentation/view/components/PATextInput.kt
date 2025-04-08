package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun PATextInput(
    value : String = "",
    modifier : Modifier = Modifier,
    placeHolder : String ?= null,
    keyboardType : KeyboardType = KeyboardType.Text,
    minLines : Int = 1,
    onValueChange: (String)->Unit
){
    val inputValue = remember {
        mutableStateOf(value)
    }
    OutlinedTextField(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        value = inputValue.value,
        onValueChange = {
            inputValue.value = it
            onValueChange(inputValue.value)
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
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        minLines = minLines
    )
}

@Composable
@Preview
private fun Preview(

){
    PATextInput(
        value = "",
        placeHolder = "este es un place holder",
    ){}
}