package com.burritoapp.ui.screens.productos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burritoapp.data.entity.ProductoConMateriaPrima
import com.burritoapp.ui.components.EditarCantidadDialog
import com.burritoapp.ui.viewmodel.ProductoViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    viewModel: ProductoViewModel = viewModel(),
    onNavigateToFormularioMateriaPrima: (Int, String) -> Unit = { _, _ -> }
) {
    val productos by viewModel.productosSemanaActual.collectAsState()
    var showDialogNuevoProducto by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos de la Semana") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialogNuevoProducto = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo producto")
            }
        }
    ) { padding ->
        if (productos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🌮",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "No hay productos esta semana",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Presiona + para agregar uno",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productos) { productoConMP ->
                    var showDialogEditarCantidad by remember { mutableStateOf(false) }
                    
                    ProductoItem(
                        productoConMP = productoConMP,
                        onEdit = {
                            onNavigateToFormularioMateriaPrima(
                                productoConMP.producto.id,
                                productoConMP.producto.nombre
                            )
                        },
                        onDelete = {
                            viewModel.eliminarProducto(productoConMP.producto.id)
                        },
                        onEditarCantidad = {
                            showDialogEditarCantidad = true
                        }
                    )
                    
                    // Dialog de editar cantidad
                    if (showDialogEditarCantidad) {
                        EditarCantidadDialog(
                            nombreProducto = productoConMP.producto.nombre,
                            cantidadActual = productoConMP.producto.cantidadProducida,
                            onDismiss = { showDialogEditarCantidad = false },
                            onGuardar = { cantidad ->
                                viewModel.actualizarCantidadProducida(productoConMP.producto.id, cantidad)
                                showDialogEditarCantidad = false
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Dialog para crear nuevo producto
    if (showDialogNuevoProducto) {
        NuevoProductoDialog(
            onDismiss = { showDialogNuevoProducto = false },
            onCreate = { nombreProducto ->
                viewModel.crearProducto(nombreProducto) { productoId ->
                    showDialogNuevoProducto = false
                    onNavigateToFormularioMateriaPrima(productoId.toInt(), nombreProducto)
                }
            }
        )
    }
}

@Composable
fun ProductoItem(
    productoConMP: ProductoConMateriaPrima,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEditarCantidad: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = productoConMP.producto.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Costo M.P: ${formatCurrency(productoConMP.costoTotalMateriaPrima())}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de cantidad producida
                    if (productoConMP.producto.cantidadProducida != null) {
                        OutlinedButton(
                            onClick = onEditarCantidad,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${productoConMP.producto.cantidadProducida}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "unidades",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onEditarCantidad,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Sin registrar",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            if (productoConMP.materiaPrima.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Ingredientes: ${productoConMP.materiaPrima.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoProductoDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var nombreProducto by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Producto") },
        text = {
            OutlinedTextField(
                value = nombreProducto,
                onValueChange = { nombreProducto = it },
                label = { Text("Nombre del producto") },
                placeholder = { Text("Ej: Burrito de Pastor") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombreProducto.isNotBlank()) {
                        onCreate(nombreProducto.trim())
                    }
                }
            ) {
                Text("Crear")
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
