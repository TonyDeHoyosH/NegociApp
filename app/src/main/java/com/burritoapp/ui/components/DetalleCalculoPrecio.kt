package com.burritoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.burritoapp.data.model.CalculoPrecio
import java.text.NumberFormat
import java.util.*

@Composable
fun DetalleCalculoPrecio(
    nombreProducto: String,
    calculoPrecio: CalculoPrecio,
    totalGastosMensuales: Double,
    diasTrabajadosMes: Int,
    montoPorDia: Double,
    numeroPersonas: Int
) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado siempre visible
            Row {
                Text(
                    text = "🧮 Cálculo de Precios",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(if (expandido) "▲" else "▼")
            }

            Text(
                text = nombreProducto,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Contenido expandible
            AnimatedVisibility(visible = expandido) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Divider()
                    
                    // Sección 1: Gastos Fijos Mensuales
                    Text(
                        text = "1️⃣ Gastos Fijos Mensuales",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DetalleRow("Total gastos mensuales:", formatCurrency(totalGastosMensuales))
                            DetalleRow("Días trabajados este mes:", "$diasTrabajadosMes días")
                            Divider()
                            DetalleRow(
                                "Gasto fijo por día:",
                                formatCurrency(calculoPrecio.gastosFijosDia),
                                destacado = true
                            )
                            Text(
                                text = "Cálculo: ${formatCurrency(totalGastosMensuales)} ÷ $diasTrabajadosMes = ${formatCurrency(calculoPrecio.gastosFijosDia)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    
                    // Sección 2: Costos del Producto
                    Text(
                        text = "2️⃣ Costos del Producto del Día",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DetalleRow("Materia prima:", formatCurrency(calculoPrecio.costoMateriaPrima))
                            DetalleRow("Gasto fijo del día:", formatCurrency(calculoPrecio.gastosFijosDia))
                            DetalleRow("Sueldos del día:", formatCurrency(calculoPrecio.sueldosDia))
                            Text(
                                text = "($numeroPersonas personas × ${formatCurrency(montoPorDia)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Divider()
                            DetalleRow(
                                "Costo total del día:",
                                formatCurrency(calculoPrecio.costoTotal),
                                destacado = true
                            )
                        }
                    }
                    
                    // Sección 3: Cálculo de Precios
                    Text(
                        text = "3️⃣ Precios por Unidad",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DetalleRow("Unidades producidas:", "${calculoPrecio.cantidadProducida}")
                            Divider()
                            DetalleRow(
                                "Precio mínimo (punto de equilibrio):",
                                formatCurrency(calculoPrecio.precioMinimo)
                            )
                            Text(
                                text = "${formatCurrency(calculoPrecio.costoTotal)} ÷ ${calculoPrecio.cantidadProducida} = ${formatCurrency(calculoPrecio.precioMinimo)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            DetalleRow(
                                "Porcentaje de ganancia:",
                                "${(calculoPrecio.porcentajeGanancia * 100).toInt()}%"
                            )
                            DetalleRow(
                                "Precio sugerido de venta:",
                                formatCurrency(calculoPrecio.precioSugerido),
                                destacado = true
                            )
                            Text(
                                text = "${formatCurrency(calculoPrecio.precioMinimo)} × ${1 + calculoPrecio.porcentajeGanancia} = ${formatCurrency(calculoPrecio.precioSugerido)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    // Sección 4: Ganancias
                    Text(
                        text = "4️⃣ Proyección de Ganancias",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DetalleRow(
                                "Si vendes todo al precio sugerido:",
                                formatCurrency(calculoPrecio.precioSugerido * calculoPrecio.cantidadProducida)
                            )
                            DetalleRow(
                                "Menos costos totales:",
                                "- ${formatCurrency(calculoPrecio.costoTotal)}"
                            )
                            Divider()
                            DetalleRow(
                                "Ganancia neta proyectada:",
                                formatCurrency(calculoPrecio.gananciaNeta),
                                destacado = true
                            )
                        }
                    }
                    
                    // Información adicional
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
                                text = "💡 Información Adicional",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Porcentaje de sueldo en el precio: ${String.format("%.1f", calculoPrecio.porcentajeSueldo * 100)}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• Vender por debajo de ${formatCurrency(calculoPrecio.precioMinimo)} genera pérdidas",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• El punto de equilibrio se alcanza vendiendo todas las unidades al precio mínimo",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleRow(
    label: String,
    valor: String,
    destacado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (destacado) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = valor,
            style = if (destacado) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            color = if (destacado) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return format.format(amount)
}
