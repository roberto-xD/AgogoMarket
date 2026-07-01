package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R

data class ClienteModel(
    val nombre: String = "Público en general",
    val telefono: String? = null,
    val direccion: String? = null,
    val instruccionesEntrega: String? = null,
)

@Composable
fun ClienteSection(
    clienteModel: ClienteModel,
    onDataChange: (ClienteModel) -> Unit,
) {
    Column (
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        PATextInput(
            value = clienteModel.nombre,
            modifier = Modifier
                .fillMaxWidth(),
            placeHolder = stringResource(id = R.string.label_client_name),
        ){
            onDataChange(clienteModel.copy(nombre = it))
        }
        PATextInput(
            value = clienteModel.telefono.orEmpty(),
            modifier = Modifier
                .fillMaxWidth(),
            placeHolder = stringResource(id = R.string.label_client_phone),
        ){
            onDataChange(clienteModel.copy(telefono = it))
        }
        PATextInput(
            value = clienteModel.direccion.orEmpty(),
            modifier = Modifier
                .fillMaxWidth(),
            placeHolder = stringResource(id = R.string.label_client_address),
            maxLines = 2
        ){
            onDataChange(clienteModel.copy(direccion = it))
        }
        clienteModel.direccion?.let {
            PATextInput(
                value = clienteModel.instruccionesEntrega.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth(),
                placeHolder = stringResource(id = R.string.label_cliente_delivery_detail),
                label = stringResource(id = R.string.label_client_delivery_instructions),
                maxLines = 2,
                minLines = 2
            ){
                onDataChange(clienteModel.copy(instruccionesEntrega = it))
            }
        }
    }
}