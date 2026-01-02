package com.burritoapp.ui.screens.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burritoapp.data.entity.EstadoVenta
import com.burritoapp.ui.screens.dashboard.VentaItem
import com.burritoapp.ui.viewmodel.VentaViewModel
import java.text.NumberFormat
import java.util.*

enum class FiltroVenta {
    TODAS,
    PAGADAS,
    PENDIENTES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentasScreen(
    ventaViewModel: VentaViewModel = viewModel()
) {
    val ventasDelDia by ventaViewModel.ventasDelDia.collectAsState()
    var filtroSeleccionado by remember { mutableStateOf(FiltroVenta.TODAS) }
    
    // Filtrar ventas según el filtro seleccionado
    val ventasFiltradas = remember(ventasDelDia, filtroSeleccionado) {
        when (filtroSeleccionado) {
            FiltroVenta.TODAS -> ventasDelDia
            FiltroVenta.PAGADAS -> ventasDelDia.filter { it.venta.estaPagada() }
            FiltroVenta.PENDIENTES -> ventasDelDia.filter { !it.venta.estaPagada() }
        }
    }
    
    // Calcular totales
    val totalFiltrado = ventasFiltradas.sumOf { it.venta.montoTotal() }
    val cantidadFiltrada = ventasFiltradas.size
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ventas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtros (tabs)
            TabRow(
                selectedTabIndex = filtroSeleccionado.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                FiltroVenta.values().forEach { filtro ->
                    Tab(
                        selected = filtroSeleccionado == filtro,
                        onClick = { filtroSeleccionado = filtro },
                        text = {
                            Text(
                                text = when (filtro) {
                                    FiltroVenta.TODAS -> "Todas"
                                    FiltroVenta.PAGADAS -> "Pagadas"
                                    FiltroVenta.PENDIENTES -> {
                                        val pendientes = ventasDelDia.count { !it.venta.estaPagada() }
                                        if (pendientes > 0) {
                                            "Pendientes ($pendientes)"
                                        } else {
                                            "Pendientes"
                                        }
                                    }
                                },
                                fontWeight = if (filtroSeleccionado == filtro) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    )
                }
            }
            
            // Totales
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total ${
                                when (filtroSeleccionado) {
                                    FiltroVenta.TODAS -> "del día"
                                    FiltroVenta.PAGADAS -> "pagado"
                                    FiltroVenta.PENDIENTES -> "pendiente"
                                }
                            }:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$cantidadFiltrada venta${if (cantidadFiltrada != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = formatCurrency(totalFiltrado),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Lista de ventas filtradas
            if (ventasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (filtroSeleccionado) {
                                FiltroVenta.TODAS -> "📋"
                                FiltroVenta.PAGADAS -> "✅"
                                FiltroVenta.PENDIENTES -> "⏳"
                            },
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = when (filtroSeleccionado) {
                                FiltroVenta.TODAS -> "No hay ventas registradas"
                                FiltroVenta.PAGADAS -> "No hay ventas pagadas"
                                FiltroVenta.PENDIENTES -> "No hay ventas pendientes"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ventasFiltradas) { ventaConProducto ->
                        VentaItem(
                            ventaConProducto = ventaConProducto,
                            onMarcarPagada = { ventaId, estado ->
                                ventaViewModel.marcarComoPagada(ventaId, estado)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return format.format(amount)
}
