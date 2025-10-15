package com.passioagogo.market.presentation.view.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passioagogo.market.R
import com.passioagogo.market.ui.theme.abelRegular
import com.passioagogo.market.ui.theme.onSurfaceLight
import com.passioagogo.market.ui.theme.outlineLight
import com.passioagogo.market.ui.theme.primaryLight

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PAOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean = true,
    label: String ?= null,
    placeholder: String ?= null,
    @DrawableRes trailingIcon : Int ? = null,
    onTrailingIconClick: (() -> Unit) ?= null,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    maxLines: Int = 1,
    maxLength: Int = 999,
    onValueChange: ((String) -> Unit) ?= null,
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val outlineColor by animateColorAsState(
        targetValue = when {
            isFocused -> primaryLight
            value.isNotEmpty() -> outlineLight
            else -> outlineLight
        },
        animationSpec = tween(durationMillis = 200)
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { focusRequester.requestFocus() }
    ) {
        // Container para el label que mantiene el espacio
        Box(modifier = Modifier.height(15.dp)) {
            AnimatedContent(
                targetState = isFocused || value.isNotEmpty(),
                transitionSpec = {
                    if (targetState) {
                        // Aparece el label
                        fadeIn() + slideInVertically { -it } with
                                fadeOut() + slideOutVertically { it }
                    } else {
                        // Desaparece el label
                        fadeIn() + slideInVertically { it } with
                                fadeOut() + slideOutVertically { -it }
                    }.using(SizeTransform(clip = false))
                }
            ) { showLabel ->
                if (showLabel) {
                    label?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = abelRegular,
                                color = if (isFocused) primaryLight
                                else onSurfaceLight
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = outlineColor,
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    // Placeholder
                    Column {
                        AnimatedVisibility(
                            visible = value.isEmpty() && !isFocused && placeholder != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                text = placeholder ?: "",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = abelRegular,
                                    fontSize = 14.sp,
                                    color = outlineLight
                                )
                            )
                        }
                    }
                    // TextField principal
                    BasicTextField(
                        value = value,
                        onValueChange = {
                            if (it.length <= maxLength){
                                when(keyboardType){
                                    KeyboardType.Number -> if (it.all { char -> char.isDigit() }) onValueChange?.invoke(it)
                                    else -> onValueChange?.invoke(it)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = abelRegular,
                            fontSize = 14.sp,
                            color = onSurfaceLight
                        ),
                        cursorBrush = SolidColor(primaryLight),
                        decorationBox = { innerTextField ->
                            innerTextField()
                        },
                        enabled = enabled,
                        minLines = minLines,
                        maxLines = maxLines,
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
                    )
                }
                trailingIcon?.let {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                enabled = true,
                                onClick = {
                                    onTrailingIconClick?.invoke()
                                }
                            ),
                        painter = painterResource(it),
                        contentDescription = null
                    )
                }
            }

        }
    }
}

@Composable
@Preview
private fun Cuac (){
    val value = remember { mutableStateOf("") }
    PAOutlinedTextField(
        value = value.value,
        onValueChange = {
            value.value = it
        },
        trailingIcon = R.drawable.barcode_scann,
        label = "Cuac",
        placeholder = "placeholder",
        maxLength = 8,
        onTrailingIconClick = {},
        keyboardType = KeyboardType.Number
    )
}