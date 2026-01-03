package com.burritoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burritoapp.data.entity.ProductoConMateriaPrima
import com.burritoapp.data.entity.EstadoVenta
import com.burritoapp.data.entity.Venta
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarVentaDialog(
    productoDelDia: ProductoConMateriaPrima,
    precioSugerido: Double,
    unidadesDisponibles: Int,
    ventaAEditar: Venta? = null,
    onDismiss: () -> Unit,
    onRegistrar: (Int, Double, String, EstadoVenta) -> Unit
) {
    var cantidadStr by remember { 
        mutableStateOf(ventaAEditar?.cantidad?.toString() ?: "1") 
    }
    var precioRealStr by remember { 
        mutableStateOf(
            ventaAEditar?.precioReal?.let { String.format("%.2f", it) } 
                ?: String.format("%.2f", precioSugerido)
        )
    }
    var nota by remember { mutableStateOf(ventaAEditar?.nota ?: "") }
    var estadoSeleccionado by remember { 
        mutableStateOf(ventaAEditar?.estado ?: EstadoVenta.PAGADO_EFECTIVO) 
    }
    
    val cantidad = cantidadStr.toIntOrNull() ?: 0
    val precioReal = precioRealStr.toDoubleOrNull() ?: 0.0
    val montoTotal = cantidad * precioReal
    
    // Validación de unidades disponibles
    val cantidadExcedida = cantidad > unidadesDisponibles
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (ventaAEditar == null) "Nueva Venta" else "Editar Venta",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Producto del día con unidades disponibles
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Producto:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = productoDelDia.producto.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Unidades disponibles:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$unidadesDisponibles",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (unidadesDisponibles > 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
                
                // Advertencia si no hay unidades
                if (unidadesDisponibles <= 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "⚠️ No hay unidades disponibles. Edita la cantidad producida primero.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                // Cantidad
                OutlinedTextField(
                    value = cantidadStr,
                    onValueChange = { cantidadStr = it },
                    label = { Text("Cantidad") },
                    placeholder = { Text("1") },
                    suffix = { Text("unidades") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = cantidadExcedida || (cantidadStr.isNotEmpty() && cantidad <= 0),
                    supportingText = {
                        if (cantidadExcedida) {
                            Text(
                                text = "⚠️ Solo hay $unidadesDisponibles unidades disponibles",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (cantidadStr.isNotEmpty() && cantidad <= 0) {
                            Text(
                                text = "⚠️ La cantidad debe ser mayor a 0",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    enabled = unidadesDisponibles > 0
                )
                
                // Precio sugerido vs precio real
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Precio Sugerido: ${formatCurrency(precioSugerido)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = precioRealStr,
                        onValueChange = { precioRealStr = it },
                        label = { Text("Precio Real (unitario)") },
                        placeholder = { Text(String.format("%.2f", precioSugerido)) },
                        prefix = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        isError = precioRealStr.isNotEmpty() && precioReal <= 0,
                        supportingText = {
                            val diferencia = precioReal - precioSugerido
                            when {
                                precioRealStr.isNotEmpty() && precioReal <= 0 -> {
                                    Text(
                                        text = "⚠️ El precio debe ser mayor a 0",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                diferencia != 0.0 -> {
                                    val color = if (diferencia > 0) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                    Text(
                                        text = if (diferencia > 0) {
                                            "↑ ${formatCurrency(diferencia)} más que el sugerido"
                                        } else {
                                            "↓ ${formatCurrency(-diferencia)} menos que el sugerido"
                                        },
                                        color = color
                                    )
                                }
                            }
                        },
                        enabled = unidadesDisponibles > 0
                    )
                }
                
                // Nota/Cliente
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota / Cliente") },
                    placeholder = { Text("Ej: Juan López") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    enabled = unidadesDisponibles > 0
                )
                
                // Estado de pago
                Text(
                    text = "Estado de Pago",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EstadoVenta.values().forEach { estado ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = estadoSeleccionado == estado,
                                onClick = { estadoSeleccionado = estado },
                                enabled = unidadesDisponibles > 0
                            )
                            Text(
                                text = "${estado.getIcono()} ${estado.getNombre()}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                                color = if (unidadesDisponibles > 0) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
                
                Divider()
                
                // Monto total
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Monto Total:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatCurrency(montoTotal),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cantidad > 0 && cantidad <= unidadesDisponibles && precioReal > 0) {
                        onRegistrar(cantidad, precioReal, nota.trim(), estadoSeleccionado)
                    }
                },
                enabled = cantidad > 0 && 
                         cantidad <= unidadesDisponibles && 
                         precioReal > 0 &&
                         unidadesDisponibles > 0
            ) {
                Text(if (ventaAEditar == null) "Registrar Venta" else "Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return format.format(amount)
}
