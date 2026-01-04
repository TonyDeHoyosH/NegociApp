package com.burritoapp.ui.screens.reportes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burritoapp.data.model.CalculoPrecio
import com.burritoapp.data.model.ReporteProducto
import com.burritoapp.ui.components.DetalleCalculoPrecio
import com.burritoapp.ui.components.GraficaLinea
import com.burritoapp.ui.viewmodel.ProductoViewModel
import com.burritoapp.ui.viewmodel.ReporteViewModel
import com.burritoapp.data.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    viewModel: ReporteViewModel = viewModel(),
    productoViewModel: ProductoViewModel = viewModel()
) {
    val reportesSemanales by viewModel.reportesSemanales.collectAsState()
    val reportesProductos by viewModel.reportesProductos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val productoDelDia by productoViewModel.productoDelDia.collectAsState()
    
    val crecimiento = viewModel.getCrecimientoSemanal()
    val mejorProducto = viewModel.getMejorProducto()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
                actions = {
                    IconButton(onClick = { viewModel.cargarReportes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gráfica de ganancias semanales
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "📊 Ganancias Netas Semanales",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (reportesSemanales.isNotEmpty()) {
                                GraficaLinea(
                                    datos = reportesSemanales.map { 
                                        it.formatoSemana() to it.gananciaNeta 
                                    },
                                    mostrarValores = true
                                )
                                
                                Divider()
                                
                                // Comparativa con semana anterior
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (crecimiento >= 0) {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.errorContainer
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Crecimiento vs semana pasada:",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${if (crecimiento >= 0) "+" else ""}${String.format("%.1f", crecimiento)}%",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (crecimiento >= 0) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.error
                                            }
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "No hay datos de ventas aún. Comienza vendiendo para ver tus reportes.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Mejor producto
                mejorProducto?.let {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🏆 Mejor Producto",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = it.nombreProducto,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Divider()
                                
                                ReporteRow("Ganancia neta:", formatCurrency(it.gananciaNeta))
                                ReporteRow("Vendidos:", "${it.totalVendido}/${it.totalProducido} unidades")
                                ReporteRow("Eficiencia:", "${String.format("%.1f", it.porcentajeVendido)}% - ${it.eficienciaVenta()}")
                                ReporteRow("Precio promedio:", formatCurrency(it.precioPromedioVenta))
                            }
                        }
                    }
                }
                
                // Lista de todos los productos
                if (reportesProductos.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🌮 Rendimiento por Producto",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                reportesProductos.forEach { producto ->
                                    ProductoReporteItem(producto)
                                    if (producto != reportesProductos.last()) {
                                        Divider()
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Resumen general
                if (reportesSemanales.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "📈 Resumen de Últimas 4 Semanas",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                val totalVendido = reportesSemanales.sumOf { it.totalVendido }
                                val totalCostos = reportesSemanales.sumOf { it.totalCostos }
                                val gananciaTotal = reportesSemanales.sumOf { it.gananciaNeta }
                                val ventasTotales = reportesSemanales.sumOf { it.numeroVentas }
                                
                                ReporteRow("Total vendido:", formatCurrency(totalVendido))
                                ReporteRow("Total costos:", formatCurrency(totalCostos))
                                ReporteRow("Ganancia neta:", formatCurrency(gananciaTotal), destacado = true)
                                ReporteRow("Número de ventas:", ventasTotales.toString())
                            }
                        }
                    }
                }

                // Cálculo de precios del producto del día
                productoDelDia?.let { producto ->
                    if (producto.producto.cantidadProducida != null) {
                        item {
                            val calculo = remember { mutableStateOf<CalculoPrecio?>(null) }
                            val totalGastosMes = remember { mutableStateOf(0.0) }
                            val diasTrabajados = remember { mutableStateOf(0) }
                            val montoPorDia = remember { mutableStateOf(0.0) }
                            val numeroPersonas = remember { mutableStateOf(0) }
                            
                            LaunchedEffect(producto.producto.id) {
                                scope.launch {
                                    calculo.value = productoViewModel.calcularPrecio(producto.producto.id)
                                    
                                    // Obtener datos de configuración
                                    val gastoFijoDao = AppDatabase.getDatabase(context).gastoFijoDao()
                                    val configuracionDao = AppDatabase.getDatabase(context).configuracionDao()
                                    
                                    totalGastosMes.value = gastoFijoDao.getTotalGastosFijosMes() ?: 0.0
                                    
                                    configuracionDao.getConfiguracionGeneral().first()?.let { config ->
                                        diasTrabajados.value = config.diasTrabajadosMes
                                    }
                                    
                                    configuracionDao.getConfiguracionSueldo().first()?.let { sueldo ->
                                        montoPorDia.value = sueldo.montoPorDia
                                        numeroPersonas.value = sueldo.numeroPersonas
                                    }
                                }
                            }
                            
                            calculo.value?.let { calc ->
                                DetalleCalculoPrecio(
                                    nombreProducto = producto.producto.nombre,
                                    calculoPrecio = calc,
                                    totalGastosMensuales = totalGastosMes.value,
                                    diasTrabajadosMes = diasTrabajados.value,
                                    montoPorDia = montoPorDia.value,
                                    numeroPersonas = numeroPersonas.value
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReporteRow(
    label: String,
    valor: String,
    destacado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (destacado) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = valor,
            style = if (destacado) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            color = if (destacado) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun ProductoReporteItem(producto: ReporteProducto) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombreProducto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${producto.totalVendido}/${producto.totalProducido} vendidos (${String.format("%.0f", producto.porcentajeVendido)}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatCurrency(producto.gananciaNeta),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return format.format(amount)
}
