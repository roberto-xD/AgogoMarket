package com.passioagogo.market.presentation.view.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R

@Composable
fun SearchInput(
    modifier: Modifier = Modifier,
    doSearch: (String) -> Unit
) {
    val input = remember {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .background(colorResource(id = R.color.gray))
            .padding(5.dp)
    ) {
        TextField(
            value = input.value,
            onValueChange = {
                input.value = it
            },
            modifier = modifier.fillMaxWidth(),
            placeholder = { Text(text = "Buscar producto") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "",
                    modifier = Modifier.clickable {
                        keyboardController?.hide()
                        doSearch(input.value)
                    }
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(
                onSearch = {

                    keyboardController?.hide()
                    doSearch(input.value)
                }
            ),
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = Color.Blue,
            )
        )
    }
}

@Composable
@Preview
private fun Preview() {
    SearchInput() {

    }
}