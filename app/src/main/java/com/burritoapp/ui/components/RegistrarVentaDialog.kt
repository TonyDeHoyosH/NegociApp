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
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarVentaDialog(
    productoDelDia: ProductoConMateriaPrima,
    precioSugerido: Double,
    onDismiss: () -> Unit,
    onRegistrar: (Int, Double, String, EstadoVenta) -> Unit
) {
    var cantidadStr by remember { mutableStateOf("1") }
    var precioRealStr by remember { mutableStateOf(String.format("%.2f", precioSugerido)) }
    var nota by remember { mutableStateOf("") }
    var estadoSeleccionado by remember { mutableStateOf(EstadoVenta.PAGADO_EFECTIVO) }
    
    // Calcular monto total
    val cantidad = cantidadStr.toIntOrNull() ?: 0
    val precioReal = precioRealStr.toDoubleOrNull() ?: 0.0
    val montoTotal = cantidad * precioReal
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nueva Venta",
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
                // Producto del día
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
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
                    modifier = Modifier.fillMaxWidth()
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
                        supportingText = {
                            val diferencia = precioReal - precioSugerido
                            if (diferencia != 0.0) {
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
                    )
                }
                
                // Nota/Cliente
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota / Cliente") },
                    placeholder = { Text("Ej: Juan López") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
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
                                onClick = { estadoSeleccionado = estado }
                            )
                            Text(
                                text = "${estado.getIcono()} ${estado.getNombre()}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
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
                    if (cantidad > 0 && precioReal > 0) {
                        onRegistrar(cantidad, precioReal, nota.trim(), estadoSeleccionado)
                    }
                }
            ) {
                Text("Registrar Venta")
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
