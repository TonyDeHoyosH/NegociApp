package com.burritoapp.ui.screens.configuracion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burritoapp.ui.viewmodel.ConfiguracionViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    viewModel: ConfiguracionViewModel = viewModel(),
    onNavigateToGastosFijos: () -> Unit = {}
) {
    val configuracionSueldo by viewModel.configuracionSueldo.collectAsState()
    val configuracionGeneral by viewModel.configuracionGeneral.collectAsState()
    
    var montoPorDia by remember { mutableStateOf("") }
    var numeroPersonas by remember { mutableStateOf("") }
    var porcentajeGanancia by remember { mutableStateOf("") }
    var diasTrabajadosMes by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(configuracionSueldo) {
        configuracionSueldo?.let {
            montoPorDia = it.montoPorDia.toString()
            numeroPersonas = it.numeroPersonas.toString()
        }
    }
    
    LaunchedEffect(configuracionGeneral) {
        configuracionGeneral?.let {
            porcentajeGanancia = (it.porcentajeGanancia * 100).toString()
            diasTrabajadosMes = it.diasTrabajadosMes.toString()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración General") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de navegación a Gastos Fijos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGastosFijos() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "💵 Gastos Fijos Mensuales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Gas, Agua, Luz, Transporte",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ir a Gastos Fijos"
                    )
                }
            }
            
            // Sección de Sueldos
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "💰 Configuración de Sueldos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Divider()
                    
                    OutlinedTextField(
                        value = montoPorDia,
                        onValueChange = { montoPorDia = it },
                        label = { Text("Sueldo por día (por persona)") },
                        placeholder = { Text("100.00") },
                        prefix = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        isError = montoPorDia.isNotEmpty() && (montoPorDia.toDoubleOrNull() ?: 0.0) <= 0
                    )
                    
                    OutlinedTextField(
                        value = numeroPersonas,
                        onValueChange = { numeroPersonas = it },
                        label = { Text("Número de personas") },
                        placeholder = { Text("2") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = numeroPersonas.isNotEmpty() && (numeroPersonas.toIntOrNull() ?: 0) < 1
                    )
                    
                    configuracionSueldo?.let { config ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Cálculos Automáticos",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Sueldo total diario: ${formatCurrency(config.sueldoTotalDiario())}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Sueldo total semanal: ${formatCurrency(config.sueldoTotalSemanal())}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                // Validaciones
                                when {
                                    montoPorDia.isBlank() || numeroPersonas.isBlank() -> {
                                        snackbarHostState.showSnackbar(
                                            message = "⚠️ Los campos no pueden estar vacíos",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    (montoPorDia.toDoubleOrNull() ?: 0.0) <= 0 -> {
                                        snackbarHostState.showSnackbar(
                                            message = "⚠️ El sueldo debe ser mayor a 0",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    (numeroPersonas.toIntOrNull() ?: 0) < 1 -> {
                                        snackbarHostState.showSnackbar(
                                            message = "⚠️ Debe haber al menos 1 persona",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    else -> {
                                        val monto = montoPorDia.toDouble()
                                        val personas = numeroPersonas.toInt()
                                        viewModel.updateSueldo(monto, personas)
                                        snackbarHostState.showSnackbar(
                                            message = "✅ Información guardada correctamente",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Sueldos")
                    }
                }
            }
            
            // Sección de Configuración General
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚙️ Configuración del Negocio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Divider()
                    
                    OutlinedTextField(
                        value = porcentajeGanancia,
                        onValueChange = { porcentajeGanancia = it },
                        label = { Text("Porcentaje de ganancia") },
                        placeholder = { Text("25") },
                        suffix = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text("Este porcentaje se suma al precio mínimo del producto")
                        },
                        isError = porcentajeGanancia.isNotEmpty() && 
                                 ((porcentajeGanancia.toDoubleOrNull() ?: 0.0) <= 0 || 
                                  (porcentajeGanancia.toDoubleOrNull() ?: 0.0) > 100)
                    )
                    
                    OutlinedTextField(
                        value = diasTrabajadosMes,
                        onValueChange = { diasTrabajadosMes = it },
                        label = { Text("Días trabajados este mes") },
                        placeholder = { Text("22") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text("Los gastos fijos mensuales se dividen entre estos días")
                        },
                        isError = diasTrabajadosMes.isNotEmpty() && 
                                 ((diasTrabajadosMes.toIntOrNull() ?: 0) <= 0 || 
                                  (diasTrabajadosMes.toIntOrNull() ?: 0) > 31)
                    )
                    
                    Button(
                        onClick = {
                            scope.launch {
                                // Validaciones
                                when {
                                    porcentajeGanancia.isBlank() || diasTrabajadosMes.isBlank() -> {
                                        snackbarHostState.showSnackbar(
                                            message = "⚠️ Los campos no pueden estar vacíos",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    (porcentajeGanancia.toDoubleOrNull() ?: 0.0) <= 0 -> {
                                        snackbarHostState.showSnackbar(
                                            message = "⚠️ El porcentaje debe ser mayor a 0",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    (porcentajeGanancia.toDoubleOrNull() ?: 0.0) > 100 -> {
                                        snackbarHostState.showSnackbar(
                                            message = "⚠️ El porcentaje no puede ser mayor a 100",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    (diasTrabajadosMes.toIntOrNull() ?: 0) <= 0 -> {
                                        snackbarHostState.showSnackbar(
                                            message = "⚠️ Debe haber al menos 1 día trabajado",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    (diasTrabajadosMes.toIntOrNull() ?: 0) > 31 -> {
                                        snackbarHostState.showSnackbar(
                                            message = "⚠️ No puede haber más de 31 días en un mes",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    else -> {
                                        val porcentaje = porcentajeGanancia.toDouble()
                                        val dias = diasTrabajadosMes.toInt()
                                        viewModel.updateConfiguracionGeneral(porcentaje / 100, dias)
                                        snackbarHostState.showSnackbar(
                                            message = "✅ Información guardada correctamente",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Configuración")
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
