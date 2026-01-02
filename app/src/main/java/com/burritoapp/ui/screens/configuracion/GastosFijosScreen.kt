package com.burritoapp.ui.screens.configuracion

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
import com.burritoapp.data.entity.GastoFijo
import com.burritoapp.ui.viewmodel.GastoFijoViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastosFijosScreen(
    viewModel: GastoFijoViewModel = viewModel()
) {
    val gastosFijos by viewModel.gastosFijos.collectAsState()
    val totalGastosMes by viewModel.totalGastosMes.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var gastoToEdit by remember { mutableStateOf<GastoFijo?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gastos Fijos Mensuales") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    gastoToEdit = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar gasto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Tarjeta de total
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
                        text = "Total Gastos Mensuales",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatCurrency(totalGastosMes),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de gastos
            if (gastosFijos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay gastos fijos registrados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(gastosFijos) { gasto ->
                        GastoFijoItem(
                            gasto = gasto,
                            onEdit = {
                                gastoToEdit = gasto
                                showDialog = true
                            },
                            onDelete = {
                                viewModel.deleteGastoFijo(gasto.id)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Dialog para agregar/editar
    if (showDialog) {
        GastoFijoDialog(
            gasto = gastoToEdit,
            onDismiss = { showDialog = false },
            onSave = { nombre, monto ->
                if (gastoToEdit == null) {
                    viewModel.insertGastoFijo(nombre, monto)
                } else {
                    viewModel.updateGastoFijo(gastoToEdit!!.id, nombre, monto)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun GastoFijoItem(
    gasto: GastoFijo,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gasto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(gasto.montoMensual),
                    style = MaterialTheme.typography.bodyLarge,
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
fun GastoFijoDialog(
    gasto: GastoFijo?,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var nombre by remember { mutableStateOf(gasto?.nombre ?: "") }
    var monto by remember { mutableStateOf(gasto?.montoMensual?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (gasto == null) "Agregar Gasto Fijo" else "Editar Gasto Fijo")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del gasto") },
                    placeholder = { Text("Ej: Gas, Agua, Luz") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    label = { Text("Monto mensual") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("$") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull()
                    if (nombre.isNotBlank() && montoDouble != null && montoDouble > 0) {
                        onSave(nombre, montoDouble)
                    }
                }
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

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return format.format(amount)
}
