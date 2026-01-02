package com.burritoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burritoapp.data.entity.Producto
import com.burritoapp.data.model.CalculoPrecio

@Composable
fun RegistrarProduccionDialog(
    producto: Producto,
    onDismiss: () -> Unit,
    onRegistrar: (Int, (CalculoPrecio?) -> Unit) -> Unit
) {
    var cantidadStr by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Producción") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Producto: ${producto.nombre}",
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    value = cantidadStr,
                    onValueChange = { cantidadStr = it },
                    label = { Text("Cantidad Producida") },
                    placeholder = { Text("Ej: 15") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cantidad = cantidadStr.toIntOrNull()
                    if (cantidad != null && cantidad > 0) {
                        onRegistrar(cantidad) { _ ->
                            // Una vez registrado y calculado, cerramos el diálogo
                            onDismiss()
                        }
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
