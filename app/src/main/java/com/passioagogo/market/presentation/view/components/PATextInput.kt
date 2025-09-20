package com.passioagogo.market.presentation.view.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.passioagogo.market.presentation.view.components.items.CompactOutlinedTextField

@Composable
internal fun PATextInput(
    mutableInput : MutableState<String>,
    modifier : Modifier = Modifier,
    placeHolder : String ?= null,
    @DrawableRes trailingIcon : Int ? = null,
    onTrailingIconClick: (() -> Unit) ?= null,
    keyboardType : KeyboardType = KeyboardType.Text,
){
    CompactOutlinedTextField(
        modifier = modifier,
        value = mutableInput.value,
        onValueChange = {
            mutableInput.value = it
        },
        label = placeHolder,
        placeholder = placeHolder,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        keyboardType = keyboardType,
        maxLength = 15,
    )
//    OutlinedTextField(
//        modifier = modifier
//            .heightIn(min = 40.dp)
//            .padding(0.dp),
//        shape = RoundedCornerShape(10.dp),
//        value = mutableInput.value,
//        onValueChange = {
//            mutableInput.value = it
//        },
//        label = {
//            placeHolder?.let {
//                Text(
//                    modifier = Modifier
//                        .height(20.dp),
//                    text = it,
//                    fontSize = 12.sp
//                )
//            }
//        },
//        placeholder = {
//            placeHolder?.let {
//                Text(
//                    modifier = Modifier
//                        .height(20.dp),
//                    text = it,
//                    fontSize = 12.sp
//                )
//            }
//        },
//        supportingText = {
//            supportingText?.let {
//                Text(
//                    modifier = Modifier
//                        .height(16.dp),
//                    text = it
//                )
//            }
//        },
//        trailingIcon = {
//            trailingIcon?.let {
//                Icon(
//                    modifier = Modifier
//                        .size(25.dp)
//                        .clickable {
//                            onTrailingIconClick?.invoke()
//                        },
//                    painter = painterResource(it),
//                    contentDescription = null
//                )
//            }
//        },
//        colors = OutlinedTextFieldDefaults.colors(
//            // disabledTextColor =
//        ),
//        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
//        minLines = minLines
//
//    )
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