package com.passioagogo.market.presentation.view.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.presentation.view.components.items.CompactOutlinedTextField

@Composable
internal fun PATextInput(
    mutableInput : MutableState<String>,
    modifier : Modifier = Modifier,
    placeHolder : String ?= null,
    @DrawableRes trailingIcon : Int ? = null,
    onTrailingIconClick: (() -> Unit) ?= null,
    enabled: Boolean = true,
    keyboardType : KeyboardType = KeyboardType.Text,
    maxLines: Int = 1,
    maxLength: Int = 15,
){
    CompactOutlinedTextField(
        modifier = modifier
            .padding(top = 3.dp),
        value = mutableInput.value,
        onValueChange = {
            mutableInput.value = it
        },
        label = placeHolder,
        placeholder = placeHolder,
        enabled = enabled,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        keyboardType = keyboardType,
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
        mutableInput =  cuac,
        placeHolder = "este es un place holder",
    )
}