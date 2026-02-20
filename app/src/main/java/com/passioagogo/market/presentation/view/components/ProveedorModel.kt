package com.passioagogo.market.presentation.view.components

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R

data class ProveedorModel(
    val nombre: String = "",
    val contacto: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val direccion: String? = null,
    val nacional: Boolean = true
) {
    fun validar(): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (nombre.isBlank()) {
            errores["nombre"] = "El nombre es requerido"
        }

        if (!telefono.isNullOrBlank() && !telefono.all { it.isDigit() }) {
            errores["telefono"] = "Solo se permiten números"
        } else if (!telefono.isNullOrBlank() && telefono.length < 10) {
            errores["telefono"] = "Mínimo 10 dígitos"
        }

        if (!email.isNullOrBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errores["email"] = "Correo no válido"
        }

        return errores
    }

    fun esValido(): Boolean = validar().isEmpty()
}

@Composable
fun ProveedorSection(
    proveedorModel: ProveedorModel,
    onDataChange: (ProveedorModel) -> Unit,
){

    Column (
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            PATextInput(
                value = proveedorModel.nombre.orEmpty(),
                modifier = Modifier
                    .weight(1f),
                placeHolder = stringResource(id = R.string.label_provider_name),
                maxLines = 1,
                minLenght = 1
            ){
                onDataChange(proveedorModel.copy(nombre = it))
            }
            PATextInput(
                value = proveedorModel.contacto.orEmpty(),
                modifier = Modifier
                    .weight(1f),
                placeHolder = stringResource(id = R.string.label_provider_contact),
                maxLines = 1
            ){
                onDataChange(proveedorModel.copy(contacto = it))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PATextInput(
                value = proveedorModel.telefono.orEmpty(),
                modifier = Modifier
                    .weight(1f),
                placeHolder = stringResource(id = R.string.label_provider_phone),
                maxLines = 1,
                maxLength = 10,
                minLenght = 10,
                keyboardType = KeyboardType.Phone
            ){
                onDataChange(proveedorModel.copy(telefono = it))
            }
            PATextInput(
                value = proveedorModel.email.orEmpty(),
                modifier = Modifier
                    .weight(1f),
                placeHolder = stringResource(id = R.string.label_provider_email),
                maxLines = 1,
                keyboardType = KeyboardType.Email
            ){
                onDataChange(proveedorModel.copy(email = it))
            }
        }

        PATextInput(
            value = proveedorModel.direccion.orEmpty(),
            modifier = Modifier
                .fillMaxWidth(),
            placeHolder = stringResource(id = R.string.label_provider_address),
            maxLines = 2
        ){
            onDataChange(proveedorModel.copy(direccion = it))
        }

    }
}


@Composable
@Preview
private fun preview(){
    ProveedorSection(
        proveedorModel = ProveedorModel(
            nombre = "Empresas patito",
            contacto = "Fulanito de tal",
            direccion = "av siempre viva 123",
            email = "fulanito@cuac.com",
            telefono = "5512345678",
            nacional = false
        ),
    ) {

    }
}
