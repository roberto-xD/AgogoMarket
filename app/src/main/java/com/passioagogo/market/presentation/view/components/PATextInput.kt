package com.passioagogo.market.presentation.view.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class InputStatus{
    REPOSO,
    OK,
    ERROR;
}

@Composable
internal fun PATextInput(
    value: String,
    modifier : Modifier = Modifier,
    placeHolder : String ?= null,
    label: String ?= null,
    @DrawableRes trailingIcon : Int ? = null,
    onTrailingIconClick: (() -> Unit) ?= null,
    enabled: Boolean = true,
    keyboardType : KeyboardType = KeyboardType.Text,
    isAmount: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1,
    minLenght: Int ?= null,
    maxLength: Int = 35,
    onValueChange: (String) -> Unit,
){
    PAOutlinedTextField(
        modifier = modifier
            .padding(top = 3.dp),
        value = value,
        onValueChange = onValueChange,
        label = label ?: placeHolder,
        placeholder = placeHolder,
        enabled = enabled,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        keyboardType = keyboardType,
        isAmount = isAmount,
        maxLength = maxLength,
        minLenght = minLenght,
        minLines = minLines,
        maxLines = maxLines,
    )
}

@Composable
@Preview(showSystemUi = true)
private fun Preview(

){
    val cuac = remember { mutableStateOf("")}
    PATextInput(
        value =  cuac.value,
        placeHolder = "este es un place holder",
    ){}
}