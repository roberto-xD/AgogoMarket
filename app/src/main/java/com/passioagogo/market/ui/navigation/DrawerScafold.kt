package com.passioagogo.market.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.passioagogo.market.domain.auth.Profile
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.common.UserRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DrawerScreen(
    userName: String,
    userRol: String,
    sections: List<DrawerSection>,
    currentScreen: String,
    drawerState: DrawerState,
    navigateToSection: (route: NavigationRoutes) -> Unit,
    onSignOut: () -> Unit,
    content: @Composable () -> Unit,
){
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(
                    nombre = userName,
                    rol = userRol
                )
                HorizontalDivider()
                Column(Modifier.verticalScroll(rememberScrollState())){
                    sections.forEach { section ->
                        NavigationDrawerItem(
                            label = { Text(section.label) },
                            icon = { Icon(section.icon, contentDescription = null) },
                            selected = currentScreen == section.label,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navigateToSection(section.route)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        )
                    }
                    if(sections.isNotEmpty()) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onSignOut()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        },
    ){
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    },
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding)){
                content.invoke()
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
private fun Preview() {

    val admin = Profile(
        id = "123",
        nombre = "Roberto",
        rol = UserRole.ADMIN,
        locationId = "",
        activo = true
    )
    val promo = Profile(
        id = "123",
        nombre = "Emi",
        rol = UserRole.PROMOTOR,
        locationId = "",
        activo = true
    )
    val vendedor = Profile(
        id = "123",
        nombre = "Moni",
        rol = UserRole.VENDEDOR,
        locationId = "",
        activo = true
    )
    val cliente = Profile(
        id = "123",
        nombre = "Saul",
        rol = UserRole.CLIENTE,
        locationId = "",
        activo = true
    )
    val profile = cliente
    val sections = SessionState.Authenticated(profile).sections
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    DrawerScreen(
        drawerState = drawerState,
        userName = profile.nombre,
        userRol = profile.rol.name,
        currentScreen = "",
        sections = sections,
        navigateToSection = {},
        onSignOut = {}
    ) { }
}
