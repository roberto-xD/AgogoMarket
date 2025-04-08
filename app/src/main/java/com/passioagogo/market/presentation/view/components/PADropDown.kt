package com.passioagogo.market.presentation.view.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.passioagogo.market.R

@Composable
internal fun PADropDown(
    modifier : Modifier = Modifier,
    placeHolder : String ?= null,
    list: List<String>,
    onValueChange: (String)->Unit,
){
    val isExpanded = remember{
        mutableStateOf(false)
    }
    val items = remember {
        mutableStateOf( list )
    }
    val item = remember {
        mutableStateOf("")
    }
    val outlinedTextFieldSize = remember { mutableStateOf(Size.Zero) }
    Column(
        modifier = modifier
    ) {
        Box {
            DropdownMenu(
                modifier = Modifier
                    .width(with(LocalDensity.current) { outlinedTextFieldSize.value.width.toDp() }),
                expanded = isExpanded.value,
                onDismissRequest = {
                    isExpanded.value = false
                }
            ) {
                items.value.forEach {   label ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label
                            )
                        },
                        onClick = {
                            isExpanded.value = false
                            item.value = label
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    outlinedTextFieldSize.value = coordinates.size.toSize()
                }
                .wrapContentHeight(align = Alignment.CenterVertically)
                .clickable(enabled = true) {
                    isExpanded.value = isExpanded.value.not()
                }
            ,
            shape = RoundedCornerShape(10.dp),
            value = item.value,
            onValueChange = {
                onValueChange.invoke(it)
            },
            label = {
                placeHolder?.let {
                    Text(
                        text = it
                    )
                }
            },
            placeholder = {
                placeHolder?.let {
                    Text(
                        text = it
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                // disabledTextColor =
            ),
            enabled = false,
            trailingIcon = {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = R.drawable.chevron_down),
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
@Preview(showBackground = true,showSystemUi = true)
private fun Preview(){
    val context = LocalContext.current
    PADropDown(
        placeHolder = "Unidad",
        list = listOf("a","b","c","d")
    ){
        Toast
            .makeText(context, "dato: $it", Toast.LENGTH_SHORT)
            .show()
    }
}