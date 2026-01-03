package com.burritoapp.ui.screens.productos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burritoapp.data.entity.MateriaPrima
import com.burritoapp.data.entity.TipoMedicion
import com.burritoapp.ui.viewmodel.ProductoViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioMateriaPrimaScreen(
    productoId: Int,
    nombreProducto: String,
    viewModel: ProductoViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var showDialogAgregar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val productoConMP by produceState<com.burritoapp.data.entity.ProductoConMateriaPrima?>(initialValue = null, productoId) {
        value = viewModel.productosSemanaActual.value.find { it.producto.id == productoId }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreProducto) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialogAgregar = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Ingrediente")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Mensaje de error
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Costo total
            productoConMP?.let { producto ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Costo Total Materia Prima",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatCurrency(producto.costoTotalMateriaPrima()),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tabla de ingredientes
                if (producto.materiaPrima.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📋",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Text(
                                text = "No hay ingredientes agregados",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Presiona + para agregar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(producto.materiaPrima) { mp ->
                            MateriaPrimaItem(
                                materiaPrima = mp,
                                onDelete = {
                                    viewModel.eliminarMateriaPrima(mp)
                                }
                            )
                        }
                    }
                    
                    // Botón de finalizar
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finalizar Producto")
                    }
                }
            }
        }
    }
    
    // Dialog para agregar ingrediente
    if (showDialogAgregar) {
        AgregarIngredienteDialog(
            onDismiss = { 
                showDialogAgregar = false
                errorMessage = null
            },
            onAgregar = { nombre, tipoMedicion, precioUnitario, precioPagado, cantidad ->
                viewModel.agregarMateriaPrima(
                    productoId = productoId,
                    nombre = nombre,
                    tipoMedicion = tipoMedicion,
                    precioUnitario = precioUnitario,
                    precioPagado = precioPagado,
                    cantidad = cantidad,
                    onSuccess = {
                        showDialogAgregar = false
                        errorMessage = null
                    },
                    onError = { error ->
                        errorMessage = error
                    }
                )
            }
        )
    }
}

@Composable
fun MateriaPrimaItem(
    materiaPrima: MateriaPrima,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = materiaPrima.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Chip con el tipo de medición
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = materiaPrima.tipoMedicion.getLabel(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Precio unitario: ${formatCurrency(materiaPrima.precioUnitario ?: 0.0)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Cantidad: ${materiaPrima.getDescripcionCantidad()}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Total: ${formatCurrency(materiaPrima.precioPagado ?: 0.0)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarIngredienteDialog(
    onDismiss: () -> Unit,
    onAgregar: (String, TipoMedicion, Double?, Double?, Double?) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tipoMedicion by remember { mutableStateOf(TipoMedicion.POR_KILO) }
    var precioUnitarioStr by remember { mutableStateOf("") }
    var precioPagadoStr by remember { mutableStateOf("") }
    var cantidadStr by remember { mutableStateOf("") }
    
    // Controlar qué campo fue editado último
    var ultimoCampoEditado by remember { mutableStateOf<String?>(null) }
    
    // Deshabilitar campos hasta que nombre y precio estén llenos
    val camposHabilitados = nombre.isNotBlank() && precioUnitarioStr.isNotBlank()
    
    // Calcular campos automáticamente solo cuando se edita precio pagado o cantidad
    LaunchedEffect(precioUnitarioStr, precioPagadoStr, cantidadStr, ultimoCampoEditado) {
        if (!camposHabilitados) return@LaunchedEffect
        
        val precioUnitario = precioUnitarioStr.toDoubleOrNull()
        val precioPagado = precioPagadoStr.toDoubleOrNull()
        val cantidad = cantidadStr.toDoubleOrNull()
        
        when (ultimoCampoEditado) {
            "cantidad" -> {
                // Usuario editó cantidad → Calcular precio pagado
                if (precioUnitario != null && cantidad != null && precioPagadoStr.isEmpty()) {
                    precioPagadoStr = String.format("%.2f", precioUnitario * cantidad)
                }
            }
            "precioPagado" -> {
                // Usuario editó precio pagado → Calcular cantidad
                if (precioUnitario != null && precioUnitario > 0 && precioPagado != null && cantidadStr.isEmpty()) {
                    cantidadStr = String.format("%.2f", precioPagado / precioUnitario)
                }
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Ingrediente") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Primero llena nombre y precio, luego elige llenar precio pagado o cantidad",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Nombre del ingrediente
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del ingrediente") },
                    placeholder = { Text("Ej: Carne de pastor") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Selector de tipo de medición
                Text(
                    text = "Tipo de medición:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipoMedicion.values().forEach { tipo ->
                        FilterChip(
                            selected = tipoMedicion == tipo,
                            onClick = { 
                                tipoMedicion = tipo
                                // Limpiar campos al cambiar tipo
                                precioUnitarioStr = ""
                                precioPagadoStr = ""
                                cantidadStr = ""
                                ultimoCampoEditado = null
                            },
                            label = { Text(tipo.getLabel()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Divider()
                
                // Precio unitario (siempre habilitado)
                OutlinedTextField(
                    value = precioUnitarioStr,
                    onValueChange = { 
                        precioUnitarioStr = it
                        // Limpiar los otros campos cuando se modifica el precio
                        precioPagadoStr = ""
                        cantidadStr = ""
                        ultimoCampoEditado = null
                    },
                    label = { Text(tipoMedicion.getLabelPrecio()) },
                    placeholder = { Text("0.00") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (!camposHabilitados && nombre.isNotBlank()) {
                    Text(
                        text = "⚠️ Llena el precio antes de continuar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Precio pagado
                OutlinedTextField(
                    value = precioPagadoStr,
                    onValueChange = { 
                        precioPagadoStr = it
                        ultimoCampoEditado = "precioPagado"
                    },
                    label = { Text("Precio pagado (total)") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = camposHabilitados
                )
                
                // Cantidad
                OutlinedTextField(
                    value = cantidadStr,
                    onValueChange = { 
                        cantidadStr = it
                        ultimoCampoEditado = "cantidad"
                    },
                    label = { Text(tipoMedicion.getLabelCantidad()) },
                    placeholder = { Text("0.00") },
                    suffix = { 
                        Text(
                            when (tipoMedicion) {
                                TipoMedicion.POR_KILO -> "kg"
                                TipoMedicion.POR_UNIDAD -> "unidades"
                            }
                        ) 
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = camposHabilitados
                )
                
                // Información sobre el autocálculo
                if (camposHabilitados) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "💡 Llena precio pagado O cantidad, el otro campo se calculará automáticamente",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val precioUnitario = precioUnitarioStr.toDoubleOrNull()
                    val precioPagado = precioPagadoStr.toDoubleOrNull()
                    val cantidad = cantidadStr.toDoubleOrNull()
                    
                    if (nombre.isNotBlank() && precioUnitario != null) {
                        onAgregar(nombre.trim(), tipoMedicion, precioUnitario, precioPagado, cantidad)
                    }
                },
                enabled = nombre.isNotBlank() && precioUnitarioStr.toDoubleOrNull() != null
            ) {
                Text("Agregar")
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
