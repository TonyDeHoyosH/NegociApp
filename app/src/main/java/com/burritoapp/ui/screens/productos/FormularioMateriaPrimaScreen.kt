package com.burritoapp.ui.screens.productos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import com.burritoapp.data.entity.ProductoConMateriaPrima
import com.burritoapp.ui.viewmodel.ProductoViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    var productoConMP by remember { mutableStateOf<ProductoConMateriaPrima?>(null) }
    var showDialogAgregar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Cargar producto con materia prima
    LaunchedEffect(productoId) {
        scope.launch {
            productoConMP = viewModel.productosSemanaActual
                .filterNotNull()
                .first()
                .find { it.producto.id == productoId }
        }
    }
    
    // Observar cambios en el producto para actualizar la UI en tiempo real
    val productosSemanaActual by viewModel.productosSemanaActual.collectAsState()
    LaunchedEffect(productosSemanaActual) {
        productoConMP = productosSemanaActual.find { it.producto.id == productoId }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Materia Prima")
                        Text(
                            text = nombreProducto,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
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
                Icon(Icons.Default.Add, contentDescription = "Agregar ingrediente")
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
            onAgregar = { nombre, precioKg, precioPagado, cantidadKg ->
                viewModel.agregarMateriaPrima(
                    productoId = productoId,
                    nombre = nombre,
                    precioKg = precioKg,
                    precioPagado = precioPagado,
                    cantidadKg = cantidadKg,
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
                Text(
                    text = materiaPrima.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Precio/kg: ${formatCurrency(materiaPrima.precioKg ?: 0.0)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Cantidad: ${String.format("%.2f", materiaPrima.cantidadKg ?: 0.0)} kg",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Total: ${formatCurrency(materiaPrima.precioPagado ?: 0.0)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarIngredienteDialog(
    onDismiss: () -> Unit,
    onAgregar: (String, Double?, Double?, Double?) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var precioKgStr by remember { mutableStateOf("") }
    var precioPagadoStr by remember { mutableStateOf("") }
    var cantidadKgStr by remember { mutableStateOf("") }
    
    // Calcular campos automáticamente
    LaunchedEffect(precioKgStr, precioPagadoStr, cantidadKgStr) {
        val precioKg = precioKgStr.toDoubleOrNull()
        val precioPagado = precioPagadoStr.toDoubleOrNull()
        val cantidadKg = cantidadKgStr.toDoubleOrNull()
        
        // Solo calcular si hay exactamente 2 campos con valor y el 3ro vacío
        val filledCount = listOf(precioKg, precioPagado, cantidadKg).count { it != null && it > 0 }
        
        if (filledCount == 2) {
            when {
                // Caso 1: Tengo precio/kg y cantidad → Calculo precio pagado
                precioKg != null && cantidadKg != null && precioPagadoStr.isEmpty() -> {
                    precioPagadoStr = String.format("%.2f", precioKg * cantidadKg)
                }
                // Caso 2: Tengo precio/kg y precio pagado → Calculo cantidad
                precioKg != null && precioPagado != null && cantidadKgStr.isEmpty() -> {
                    cantidadKgStr = String.format("%.2f", precioPagado / precioKg)
                }
                // Caso 3: Tengo precio pagado y cantidad → Calculo precio/kg
                precioPagado != null && cantidadKg != null && precioKgStr.isEmpty() -> {
                    precioKgStr = String.format("%.2f", precioPagado / cantidadKg)
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
                    text = "Llena al menos 2 campos y el tercero se calculará automáticamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del ingrediente") },
                    placeholder = { Text("Ej: Carne de pastor") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = precioKgStr,
                    onValueChange = { precioKgStr = it },
                    label = { Text("Precio por kg") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = precioPagadoStr,
                    onValueChange = { precioPagadoStr = it },
                    label = { Text("Precio pagado (total)") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = cantidadKgStr,
                    onValueChange = { cantidadKgStr = it },
                    label = { Text("Cantidad (kg)") },
                    placeholder = { Text("0.00") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val precioKg = precioKgStr.toDoubleOrNull()
                    val precioPagado = precioPagadoStr.toDoubleOrNull()
                    val cantidadKg = cantidadKgStr.toDoubleOrNull()
                    
                    if (nombre.isNotBlank()) {
                        onAgregar(nombre.trim(), precioKg, precioPagado, cantidadKg)
                    }
                }
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
