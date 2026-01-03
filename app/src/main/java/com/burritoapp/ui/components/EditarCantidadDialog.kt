package com.burritoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarCantidadDialog(
    nombreProducto: String,
    cantidadActual: Int?,
    onDismiss: () -> Unit,
    onGuardar: (Int) -> Unit
) {
    var cantidadStr by remember { mutableStateOf(cantidadActual?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Cantidad Producida",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = nombreProducto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                OutlinedTextField(
                    value = cantidadStr,
                    onValueChange = { cantidadStr = it },
                    label = { Text("Cantidad producida") },
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
                        onGuardar(cantidad)
                    }
                },
                enabled = cantidadStr.toIntOrNull()?.let { it > 0 } ?: false
            ) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
