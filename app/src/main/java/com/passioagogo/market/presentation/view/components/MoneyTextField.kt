package com.passioagogo.market.presentation.view.components

import android.R.attr.maxLines
import android.R.attr.minLines
import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passioagogo.market.ui.theme.abelRegular
import com.passioagogo.market.ui.theme.errorLight
import com.passioagogo.market.ui.theme.onSurfaceLight
import com.passioagogo.market.ui.theme.outlineLight
import com.passioagogo.market.ui.theme.primaryLight
import com.passioagogo.market.ui.utils.PAConstants.TAG_PG

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MoneyTextField(
    modifier: Modifier = Modifier,
    value: Double,
    enabled: Boolean = true,
    label: String ?= null,
    placeholder: String? = null,
    @DrawableRes trailingIcon : Int ? = null,
    onTrailingIconClick: (() -> Unit) ?= null,
    minLenght: Int ?= null,
    maxLength: Int = 999,
    onValueChange: (Double) -> Unit,
) {
    // Estado local para mantener el texto mientras se edita
    var textValue by remember(value) {
        mutableStateOf(
            if (value == 0.0) "" else {
                // Formatea con 2 decimales si ya tiene valor
                String.format("%.2f", value)
            }
        )
    }

    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val status = remember { mutableStateOf< InputStatus>(InputStatus.REPOSO) }
    val interactionSource = remember { MutableInteractionSource() }

    val outlineColor by animateColorAsState(
        targetValue = when {
            status.value == InputStatus.ERROR -> errorLight
            status.value == InputStatus.REPOSO -> outlineLight
            isFocused -> primaryLight
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
                targetState = isFocused,
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
                            visible = value == 0.0 && !isFocused && placeholder != null,
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
                        value = textValue,
                        onValueChange = { newValue ->
                            Log.i(TAG_PG,"on change: $newValue")
                            // Permite solo números, punto y máximo 2 decimales
                            val filtered = newValue.filter { it.isDigit() || it == '.' }

                            // Valida formato decimal
                            val parts = filtered.split(".")
                            val isValid = when {
                                parts[0].length > maxLength -> false // Más de 999 caracteres
                                parts.size > 2 -> false // Más de un punto
                                parts.size == 2 && parts[1].length > 2 -> false // Más de 2 decimales
                                else -> true
                            }

                            if (isValid) {
                                textValue = filtered
                                // Convierte a Double y notifica cambio
                                val doubleValue = filtered.toDoubleOrNull() ?: 0.0
                                onValueChange(doubleValue)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                                // Formatea al perder el foco
                                if (!focusState.isFocused && textValue.isNotEmpty()) {
                                    val doubleValue = textValue.toDoubleOrNull() ?: 0.0
                                    textValue = String.format("%.2f", doubleValue)
                                }
                            },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = abelRegular,
                            fontSize = 14.sp,
                            color = onSurfaceLight
                        ),
                        cursorBrush = SolidColor(primaryLight),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        decorationBox = { innerTextField ->
                            Row {
                                Text(
                                    text = if(isFocused && placeholder != null) "$" else "",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = abelRegular,
                                        fontSize = 14.sp,
                                    ),
                                    color = if (textValue.isEmpty()) Color.Gray else Color.Black
                                )
                                innerTextField()
                            }
                        },
                        enabled = enabled,
                        minLines = 1,
                        maxLines = 1,
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
private fun preview(){
    MoneyTextField(
        value = 0.0,
        placeholder = "Precio de venta",
        onValueChange = {}
    )
}