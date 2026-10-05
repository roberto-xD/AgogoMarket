package com.passioagogo.market.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.passioagogo.market.BuildConfig
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.ui.auth.LoginScreen
import com.passioagogo.market.ui.common.toMessage
import com.passioagogo.market.ui.navigation.DeepLinkDestino
import com.passioagogo.market.ui.navigation.DrawerScreen
import com.passioagogo.market.ui.navigation.NavigationHostAll
import com.passioagogo.market.ui.navigation.NavigationHostAdmin
import com.passioagogo.market.ui.navigation.NavigationHostClient
import com.passioagogo.market.ui.navigation.NavigationHostStaff
import com.passioagogo.market.ui.navigation.NavigationRoutes
import com.passioagogo.market.ui.session.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    deepLink: DeepLinkDestino? = null,
    viewModel: SessionViewModel = hiltViewModel(),
){
    val session by viewModel.sessionState.collectAsState()
    val contexto = LocalContext.current
    var lastAuthenticated by remember { mutableStateOf<SessionState.Authenticated?>(null) }

    LaunchedEffect(session) {
        when (session) {
            is SessionState.Authenticated -> lastAuthenticated = session as SessionState.Authenticated
            SessionState.NotAuthenticated, SessionState.Inactive -> lastAuthenticated = null
            else -> Unit
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        session is SessionState.Authenticated
    ) {
        val permiso = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
        LaunchedEffect(Unit) {
            val context = contexto
            val concedido = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!concedido) permiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    when (val state = session){
        is SessionState.NotAuthenticated -> LoginScreen()
        is SessionState.Inactive -> MessageScreen(
            title = "Cuenta desactivada",
            message = "Tu acceso fue revocado. Contacta a un administrador.",
            actionLabel = "Cerrar sesión",
            onAction = viewModel::onSignOut,
        )
        is SessionState.Error -> MessageScreen(
            title = "No pudimos cargar tu perfil",
            message = state.error.toMessage(),
            actionLabel = "Reintentar",
            onAction = viewModel::onRetry,
        )
        is SessionState.Initializing -> {
            if(lastAuthenticated == null){
                LoadingScreen()
            }
        }
        is SessionState.Authenticated -> AppScaffold(
            session = state,
            onSignOut = viewModel::onSignOut,
            deepLink = deepLink
        )
    }
}

@Composable
private fun AppScaffold(
    session: SessionState.Authenticated,
    onSignOut: () -> Unit,
    deepLink: DeepLinkDestino? = null,
){
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination
    val startRoute = remember {
        mutableStateOf(
            when{
                session.isAdmin -> NavigationRoutes.MainScreen
                else -> NavigationRoutes.Catalog(true)
            }
        )
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val confirmarSalida = remember { mutableStateOf(false) }
    val enRaiz = navController.previousBackStackEntry == null

    fun onBack(){
        if(drawerState.isOpen){
            scope.launch { drawerState.close() }
        }else if(enRaiz){
            confirmarSalida.value = true
        } else{
            navController.popBackStack()
        }
    }
    BackHandler(enabled = true) {
        onBack()
    }

    LaunchedEffect(deepLink) {
        if (deepLink == null) return@LaunchedEffect

    }

    DrawerScreen(
        userName = session.profile.nombre,
        userRol = session.profile.rol.name,
        sections = session.sections,
        onSignOut = onSignOut,
        currentScreen = session.sections.find { it.route == currentDestination }?.label.orEmpty(),
        drawerState = drawerState,
        navigateToSection = { route ->
            navController.navigate(route)
        },
        content = {
            if (BuildConfig.ES_SANDBOX) {
                SandboxBanner()
            }
            NavHost(
                navController = navController,
                startDestination = startRoute.value
            ){
                NavigationHostAll(
                    navController = navController,
                    onBack = ::onBack
                )
                when{
                    session.isAdmin -> {
                        NavigationHostAdmin(
                            navController = navController,
                            onBack = ::onBack
                        )
                    }
                    session.isPromotor || session.isCliente ->{
                        NavigationHostClient(
                            navController = navController,
                            onBack = ::onBack
                        )
                    }
                    session.isStaff -> {
                        NavigationHostStaff(
                            navController = navController,
                            onBack = ::onBack
                        )
                    }
                }
            }
        }
    )
    if (confirmarSalida.value) {
        val actividad = LocalActivity.current
        AlertDialog(
            onDismissRequest = { confirmarSalida.value = false },
            title = { Text("Cerrar la aplicación") },
            text = { Text("¿Seguro que quieres cerrar la aplicación?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida.value = false
                    actividad?.finish()
                }) { Text("Cerrar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida.value = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}