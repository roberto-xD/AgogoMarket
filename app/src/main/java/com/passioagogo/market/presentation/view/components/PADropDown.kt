package com.passioagogo.market.presentation.view.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toSize
import com.passioagogo.market.R

@Composable
internal fun PADropDown(
    modifier : Modifier = Modifier,
    placeHolder : String ?= null,
    item: String,
    items: List<String> ?= null,
    onAddNewClick: (()->Unit) ?= null,
    onSelectValue: ((String) -> Unit) ?= null,
){
    val isExpanded = remember{
        mutableStateOf(false)
    }
    val outlinedTextFieldSize = remember { mutableStateOf(Size.Zero) }
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
        ) {
            DropdownMenu(
                modifier = Modifier
                    .width(with(LocalDensity.current) { outlinedTextFieldSize.value.width.toDp() }),
                expanded = isExpanded.value,
                onDismissRequest = {
                    isExpanded.value = false
                }
            ) {
                items?.forEach {   label ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label
                            )
                        },
                        onClick = {
                            isExpanded.value = false
                            onSelectValue?.invoke(label)
                        }
                    )
                }
                onAddNewClick?.let {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "+ Añadir nuevo"
                            )
                        },
                        onClick = {
                            isExpanded.value = false
                            it.invoke()
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .clickable(
                    enabled = true,
                    onClick = {
                        isExpanded.value = true
                    }
                )
        ){
            PAOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        outlinedTextFieldSize.value = coordinates.size.toSize()
                    }
                    .wrapContentHeight(align = Alignment.CenterVertically)
                ,
                value = item,
                label = placeHolder,
                placeholder = placeHolder,
                enabled = false,
                trailingIcon = R.drawable.chevron_down,
                onTrailingIconClick = {
                    isExpanded.value = true
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true,showSystemUi = true)
private fun Preview(){
    val context = LocalContext.current
    PADropDown(
        item = "cuac",
        placeHolder = "Unidad",
        items = listOf("a","b","c","d")
    ){
        Toast
            .makeText(context, "dato: $it", Toast.LENGTH_SHORT)
            .show()
    }
}