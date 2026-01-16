package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R

data class ProveedorModel(
    val nombre: String = "",
    val contacto: String ?= null,
    val telefono: String ?= null,
    val email: String ?= null,
    val direccion: String ?= null,
    val nacional: Boolean = true
)

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
                maxLines = 1
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
                maxLength = 10
            ){
                onDataChange(proveedorModel.copy(telefono = it))
            }
            PATextInput(
                value = proveedorModel.email.orEmpty(),
                modifier = Modifier
                    .weight(1f),
                placeHolder = stringResource(id = R.string.label_provider_email),
                maxLines = 1
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
            telefono = "55 1234 5678",
            nacional = false
        ),
    ) {

    }
}