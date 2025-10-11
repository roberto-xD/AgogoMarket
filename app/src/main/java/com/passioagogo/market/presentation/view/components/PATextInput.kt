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

@Composable
internal fun PATextInput(
    value: String,
    modifier : Modifier = Modifier,
    placeHolder : String ?= null,
    @DrawableRes trailingIcon : Int ? = null,
    onTrailingIconClick: (() -> Unit) ?= null,
    enabled: Boolean = true,
    keyboardType : KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    maxLines: Int = 1,
    maxLength: Int = 35,
    onValueChange: (String) -> Unit,
){
    PAOutlinedTextField(
        modifier = modifier
            .padding(top = 3.dp),
        value = value,
        onValueChange = onValueChange,
        label = placeHolder,
        placeholder = placeHolder,
        enabled = enabled,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        keyboardType = keyboardType,
        minLines = minLines,
        maxLength = maxLength,
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