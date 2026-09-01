package com.passioagogo.market.ui.inventory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.ui.inventory.requests.TransferRequestsScreen
import com.passioagogo.market.ui.inventory.transfers.TransfersListScreen

@Composable
fun InventoryHomeScreen(
    session: SessionState.Authenticated,
    onOpenTransfer: (String) -> Unit,
    onCreateTransfer: () -> Unit,
    onOpenStockTake: () -> Unit,
    onOpenSolicitud: (String) -> Unit,
    onNuevaSolicitud: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Existencias") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Transferencias") },
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Solicitudes") },
            )
        }
        when (selectedTab) {
            0 -> InventoryScreen(session = session, onOpenStockTake = onOpenStockTake)
            1 -> TransfersListScreen(
                onOpenTransfer = onOpenTransfer,
                onCreateTransfer = onCreateTransfer,
            )
            2 -> TransferRequestsScreen(
                onOpenRequest = onOpenSolicitud,
                onNewRequest = onNuevaSolicitud,
            )
        }
    }
}
