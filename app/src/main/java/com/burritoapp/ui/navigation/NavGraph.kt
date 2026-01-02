package com.burritoapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.burritoapp.ui.components.BotonFlotanteVenta
import com.burritoapp.ui.components.RegistrarVentaDialog
import com.burritoapp.ui.screens.configuracion.ConfiguracionScreen
import com.burritoapp.ui.screens.configuracion.GastosFijosScreen
import com.burritoapp.ui.screens.dashboard.DashboardScreen
import com.burritoapp.ui.screens.productos.ProductosScreen
import com.burritoapp.ui.screens.productos.FormularioMateriaPrimaScreen
import com.burritoapp.ui.screens.ventas.VentasScreen
import com.burritoapp.ui.screens.reportes.ReportesScreen
import com.burritoapp.ui.viewmodel.ProductoViewModel
import com.burritoapp.ui.viewmodel.VentaViewModel
import kotlinx.coroutines.launch

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val screens = listOf(
        Screen.Dashboard,
        Screen.Productos,
        Screen.Ventas,
        Screen.Configuracion,
        Screen.Reportes
    )
    
    // ViewModels necesarios
    val productoViewModel: ProductoViewModel = viewModel()
    val ventaViewModel: VentaViewModel = viewModel()
    
    val productoDelDia by productoViewModel.productoDelDia.collectAsState()
    var mostrarDialogVenta by remember { mutableStateOf(false) }
    var precioSugeridoVenta by remember { mutableStateOf(0.0) }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { 
                        it.route == screen.route 
                    } == true
                    
                    NavigationBarItem(
                        icon = {
                            Text(
                                text = screen.icon,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            // Obtener la ruta actual
            val currentRoute = navBackStackEntry?.destination?.route
            
            // Solo mostrar en Dashboard y Ventas
            val mostrarBoton = currentRoute in listOf(
                Screen.Dashboard.route,
                Screen.Ventas.route
            )
            
            // Solo mostrar si hay producto del día con producción registrada
            if (mostrarBoton && productoDelDia != null && productoDelDia!!.producto.cantidadProducida != null) {
                BotonFlotanteVenta(
                    onClick = {
                        scope.launch {
                            val calculo = productoViewModel.calcularPrecio(productoDelDia!!.producto.id)
                            precioSugeridoVenta = calculo?.precioSugerido ?: 0.0
                            mostrarDialogVenta = true
                        }
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    productoViewModel = productoViewModel,
                    ventaViewModel = ventaViewModel
                )
            }
            
            composable(Screen.Productos.route) {
                ProductosScreen(
                    viewModel = productoViewModel,
                    onNavigateToFormularioMateriaPrima = { productoId, nombreProducto ->
                        navController.navigate(
                            Screen.FormularioMateriaPrima.createRoute(productoId, nombreProducto)
                        )
                    }
                )
            }
            
            composable(Screen.Ventas.route) {
                VentasScreen(ventaViewModel = ventaViewModel)
            }
            
            composable(Screen.Configuracion.route) {
                ConfiguracionScreen(
                    onNavigateToGastosFijos = {
                        navController.navigate(Screen.GastosFijos.route)
                    }
                )
            }
            
            composable(Screen.Reportes.route) {
                ReportesScreen()
            }
            
            // Pantallas secundarias
            composable(Screen.GastosFijos.route) {
                GastosFijosScreen()
            }
            
            composable(
                route = Screen.FormularioMateriaPrima.route,
                arguments = listOf(
                    navArgument("productoId") { type = NavType.IntType },
                    navArgument("nombreProducto") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0
                val nombreProducto = backStackEntry.arguments?.getString("nombreProducto") ?: ""
                
                FormularioMateriaPrimaScreen(
                    productoId = productoId,
                    nombreProducto = nombreProducto,
                    viewModel = productoViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
    
    // Dialog de registrar venta
    if (mostrarDialogVenta && productoDelDia != null) {
        RegistrarVentaDialog(
            productoDelDia = productoDelDia!!,
            precioSugerido = precioSugeridoVenta,
            onDismiss = { mostrarDialogVenta = false },
            onRegistrar = { cantidad, precioReal, nota, estado ->
                ventaViewModel.registrarVenta(
                    productoId = productoDelDia!!.producto.id,
                    cantidad = cantidad,
                    precioSugerido = precioSugeridoVenta,
                    precioReal = precioReal,
                    nota = nota,
                    estado = estado,
                    onSuccess = {
                        mostrarDialogVenta = false
                    }
                )
            }
        )
    }
}
