package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.presentation.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun PADrawerSheet(
    isOpend: Boolean = false,
    addItem: () -> Unit,
    search: () -> Unit,
    navigate: (route: String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(
        initialValue = if(isOpend) DrawerValue.Open else DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Image(
                        painter = painterResource(
                            id = R.drawable.branding_passion_27
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8F)
                            .padding(top = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        contentDescription = null
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "Inicio") },
                        selected = false,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.empty_dashboard),
                                modifier = Modifier
                                    .size(24.dp),
                                contentDescription = null
                            )
                        },
                        onClick = { navigate.invoke(Routes.Dashboard.Name) }
                    )

                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "Nuevo Producto") },
                        selected = false,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.edit_square),
                                modifier = Modifier
                                    .size(24.dp),
                                contentDescription = null
                            )
                        },
                        onClick = addItem
                    )

                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "Importar") },
                        selected = false,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.upload_file),
                                modifier = Modifier
                                    .size(24.dp),
                                contentDescription = null
                            )
                        },
                        onClick = { navigate.invoke(Routes.Import.Name) }
                    )

                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "Galeria") },
                        selected = false,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.gallery_thumbnail),
                                modifier = Modifier
                                    .size(24.dp),
                                contentDescription = null
                            )
                        },
                        onClick = { navigate.invoke(Routes.ImageGallery.Name) }
                    )

                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "Registrar venta") },
                        selected = false,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.shopping_bag_speed),
                                modifier = Modifier
                                    .size(24.dp),
                                contentDescription = null
                            )
                        },
                        onClick = { navigate.invoke(Routes.SellProcess.Name) }
                    )

                    /*HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "Iniciar sesión") },
                        selected = false,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.folder_upload_24),
                                contentDescription = null
                            )
                        },
                        onClick = { navigate.invoke(Routes.Login.Name) }
                    )*/
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                PAToolbar(
                    onLeftClick = {
                        scope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    },
                    onRightClick = addItem,
                    onCenterClick = search
                )
            },
            content = { paddingValues ->
                content.invoke(paddingValues)
            }
        )
    }
}

@Composable
@Preview
private fun Preview() {
    PADrawerSheet(
        isOpend = true,
        addItem = {},
        navigate = {},
        content = {},
        search = {}
    )
}