package com.burritoapp.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burritoapp.data.entity.Producto
import com.burritoapp.data.entity.VentaConProducto
import com.burritoapp.data.entity.EstadoVenta
import com.burritoapp.ui.components.IndicadorProgreso
import com.burritoapp.ui.components.NotificacionCard
import com.burritoapp.ui.components.TipoNotificacion
import com.burritoapp.ui.components.RegistrarProduccionDialog
import com.burritoapp.ui.viewmodel.ProductoViewModel
import com.burritoapp.ui.viewmodel.VentaViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    productoViewModel: ProductoViewModel = viewModel(),
    ventaViewModel: VentaViewModel = viewModel()
) {
    val productosSinProduccion by productoViewModel.productosSinProduccion.collectAsState()
    val productoDelDia by productoViewModel.productoDelDia.collectAsState()
    val ventasDelDia by ventaViewModel.ventasDelDia.collectAsState()
    val resumenDelDia by ventaViewModel.resumenDelDia.collectAsState()
    
    var showDialogRegistrar by remember { mutableStateOf(false) }
    var productoARegistrar by remember { mutableStateOf<Producto?>(null) }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notificaciones inteligentes
            if (productosSinProduccion.isNotEmpty()) {
                item {
                    NotificacionCard(
                        tipo = TipoNotificacion.ADVERTENCIA,
                        titulo = "⚠️ Producción Pendiente",
                        mensaje = "Tienes ${productosSinProduccion.size} producto(s) sin registrar cuántos salieron",
                        accion = {
                            Button(
                                onClick = {
                                    productoARegistrar = productosSinProduccion.first()
                                    showDialogRegistrar = true
                                }
                            ) {
                                Text("Registrar")
                            }
                        }
                    )
                }
            }

            // Notificación de ventas pendientes
            resumenDelDia?.let { resumen ->
                if (resumen.ventasPendientes > 0) {
                    item {
                        NotificacionCard(
                            tipo = TipoNotificacion.INFO,
                            titulo = "💰 Ventas Pendientes",
                            mensaje = "Tienes ${resumen.ventasPendientes} venta(s) pendiente(s) por ${formatCurrency(resumen.montoPendiente)}"
                        )
                    }
                }
            }
            
            // Resumen del día
            resumenDelDia?.let { resumen ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (resumen.seAlcanzoEquilibrio()) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "📊 Resumen del Día",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Divider()
                            
                            // Vendido hoy
                            ResumenRow(
                                label = "Vendido hoy:",
                                valor = formatCurrency(resumen.totalVendido),
                                destacado = true
                            )
                            
                            // Punto de equilibrio
                            ResumenRow(
                                label = "Punto de equilibrio:",
                                valor = formatCurrency(resumen.puntoEquilibrio)
                            )
                            
                            // Falta para equilibrio o ganancia
                            if (resumen.seAlcanzoEquilibrio()) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = "✅ ¡Punto de equilibrio alcanzado!",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "Ganancia neta: ${formatCurrency(resumen.gananciaNeta)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = "⚠️ Falta para equilibrio:",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = formatCurrency(resumen.faltaParaEquilibrio()),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "${String.format("%.1f", resumen.porcentajeLogrado)}% completado",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                            
                            // Información adicional
                            Divider()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                InfoChip("${resumen.cantidadVentas} ventas")
                                InfoChip("${resumen.unidadesVendidas} unidades")
                                if (resumen.ventasPendientes > 0) {
                                    InfoChip(
                                        "${resumen.ventasPendientes} pendientes",
                                        color = MaterialTheme.colorScheme.errorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Indicador de progreso hacia el equilibrio
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "📈 Progreso del Día",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            IndicadorProgreso(
                                progreso = (resumen.porcentajeLogrado / 100).toFloat().coerceIn(0f, 1f),
                                label = "Hacia el punto de equilibrio",
                                colorProgreso = if (resumen.seAlcanzoEquilibrio()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                            
                            if (productoDelDia?.producto?.cantidadProducida != null) {
                                val unidadesProducidas = productoDelDia!!.producto.cantidadProducida!!
                                val progresoUnidades = if (unidadesProducidas > 0) {
                                    (resumen.unidadesVendidas.toFloat() / unidadesProducidas.toFloat()).coerceIn(0f, 1f)
                                } else {
                                    0f
                                }
                                
                                IndicadorProgreso(
                                    progreso = progresoUnidades,
                                    label = "Unidades vendidas (${resumen.unidadesVendidas}/${unidadesProducidas})",
                                    colorProgreso = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
            
            // Producto del día
            productoDelDia?.let { producto ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🌮 Producto del Día",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = producto.producto.nombre,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            if (producto.producto.cantidadProducida != null) {
                                Text(
                                    text = "Producidos: ${producto.producto.cantidadProducida} unidades",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Costo M.P: ${formatCurrency(producto.costoTotalMateriaPrima())}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Lista de ventas del día
            if (ventasDelDia.isNotEmpty()) {
                item {
                    Text(
                        text = "Ventas del día:",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(ventasDelDia) { ventaConProducto ->
                    VentaItem(
                        ventaConProducto = ventaConProducto,
                        onMarcarPagada = { ventaId, estado ->
                            ventaViewModel.marcarComoPagada(ventaId, estado)
                        }
                    )
                }
            } else if (productoDelDia != null && productoDelDia!!.producto.cantidadProducida != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💰",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Text(
                                text = "No hay ventas aún",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Presiona el botón + para registrar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialog de registrar producción
    if (showDialogRegistrar && productoARegistrar != null) {
        RegistrarProduccionDialog(
            producto = productoARegistrar!!,
            onDismiss = { 
                showDialogRegistrar = false
                productoARegistrar = null
            },
            onRegistrar = { cantidad, onCalculoCompleto ->
                scope.launch {
                    productoViewModel.registrarProduccionDelDia(productoARegistrar!!.id, cantidad) {
                        scope.launch {
                            val calculo = productoViewModel.calcularPrecio(productoARegistrar!!.id)
                            onCalculoCompleto(calculo)
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun ResumenRow(
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
private fun InfoChip(
    texto: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun VentaItem(
    ventaConProducto: VentaConProducto,
    onMarcarPagada: (Int, EstadoVenta) -> Unit = { _, _ -> }
) {
    val venta = ventaConProducto.venta
    var showMenuEstado by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (venta.estado) {
                EstadoVenta.PAGADO_EFECTIVO, EstadoVenta.PAGADO_TARJETA -> 
                    MaterialTheme.colorScheme.surfaceVariant
                EstadoVenta.PENDIENTE -> 
                    MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Cantidad y nota
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${venta.cantidad}x",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = venta.nota.ifBlank { "Sin nota" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Precios
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatCurrency(venta.precioSugerido),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatCurrency(venta.precioReal),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (venta.diferenciaPrecio() >= 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
                
                // Monto total
                Text(
                    text = "Total: ${formatCurrency(venta.montoTotal())}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Estado
            Box {
                Card(
                    onClick = { 
                        if (venta.estado == EstadoVenta.PENDIENTE) {
                            showMenuEstado = true
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = when (venta.estado) {
                            EstadoVenta.PAGADO_EFECTIVO -> MaterialTheme.colorScheme.primaryContainer
                            EstadoVenta.PAGADO_TARJETA -> MaterialTheme.colorScheme.tertiaryContainer
                            EstadoVenta.PENDIENTE -> MaterialTheme.colorScheme.error
                        }
                    )
                ) {
                    Text(
                        text = "${venta.estado.getIcono()} ${venta.estado.getNombre()}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Menu para marcar como pagada
                DropdownMenu(
                    expanded = showMenuEstado,
                    onDismissRequest = { showMenuEstado = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("${EstadoVenta.PAGADO_EFECTIVO.getIcono()} ${EstadoVenta.PAGADO_EFECTIVO.getNombre()}") },
                        onClick = {
                            onMarcarPagada(venta.id, EstadoVenta.PAGADO_EFECTIVO)
                            showMenuEstado = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("${EstadoVenta.PAGADO_TARJETA.getIcono()} ${EstadoVenta.PAGADO_TARJETA.getNombre()}") },
                        onClick = {
                            onMarcarPagada(venta.id, EstadoVenta.PAGADO_TARJETA)
                            showMenuEstado = false
                        }
                    )
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return format.format(amount)
}
