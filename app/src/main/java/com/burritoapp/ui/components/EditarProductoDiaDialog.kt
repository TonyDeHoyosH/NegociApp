package com.burritoapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burritoapp.data.entity.ProductoConMateriaPrima

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarProductoDiaDialog(
    productoActual: ProductoConMateriaPrima,
    productosDisponibles: List<ProductoConMateriaPrima>,
    onDismiss: () -> Unit,
    onGuardar: (productoId: Int, cantidad: Int) -> Unit
) {
    var productoSeleccionado by remember { mutableStateOf(productoActual) }
    var cantidadStr by remember { 
        mutableStateOf(productoActual.producto.cantidadProducida?.toString() ?: "") 
    }
    var mostrarListaProductos by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Producto del Día",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de producto
                Text(
                    text = "Producto:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                Card(
                    onClick = { mostrarListaProductos = !mostrarListaProductos },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = productoSeleccionado.producto.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (mostrarListaProductos) "▲" else "▼",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                // Lista desplegable de productos
                if (mostrarListaProductos) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        LazyColumn {
                            items(productosDisponibles) { producto ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            productoSeleccionado = producto
                                            cantidadStr = producto.producto.cantidadProducida?.toString() ?: ""
                                            mostrarListaProductos = false
                                        },
                                    color = if (producto.producto.id == productoSeleccionado.producto.id) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = producto.producto.nombre,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Materia prima: $${String.format("%.2f", producto.costoTotalMateriaPrima())}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (producto != productosDisponibles.last()) {
                                    Divider()
                                }
                            }
                        }
                    }
                }
                
                Divider()
                
                // Campo de cantidad producida
                Text(
                    text = "Cantidad producida:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = cantidadStr,
                    onValueChange = { cantidadStr = it },
                    label = { Text("Cantidad") },
                    placeholder = { Text("Ej: 13") },
                    suffix = { Text("unidades") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = cantidadStr.isNotEmpty() && (cantidadStr.toIntOrNull() ?: 0) <= 0
                )
                
                if (cantidadStr.isNotEmpty() && (cantidadStr.toIntOrNull() ?: 0) <= 0) {
                    Text(
                        text = "⚠️ La cantidad debe ser mayor a 0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cantidad = cantidadStr.toIntOrNull()
                    if (cantidad != null && cantidad > 0) {
                        onGuardar(productoSeleccionado.producto.id, cantidad)
                    }
                },
                enabled = cantidadStr.toIntOrNull()?.let { it > 0 } ?: false
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
