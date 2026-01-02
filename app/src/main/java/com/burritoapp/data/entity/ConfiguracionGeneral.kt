package com.burritoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion_general")
data class ConfiguracionGeneral(
    @PrimaryKey
    val id: Int = 1,                    // Solo habrá 1 registro
    
    val porcentajeGanancia: Double,     // Ej: 0.25 (25%)
    val diasTrabajadosMes: Int,         // Ej: 22 días
    val mesActual: String               // Ej: "2025-01" para identificar el mes
) {
    // Calcula el costo diario de gastos fijos
    fun calcularGastoFijoDiario(totalGastosFijosMes: Double): Double {
        return if (diasTrabajadosMes > 0) {
            totalGastosFijosMes / diasTrabajadosMes
        } else {
            0.0
        }
    }
}
