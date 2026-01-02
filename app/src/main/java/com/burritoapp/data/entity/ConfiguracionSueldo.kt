package com.burritoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion_sueldo")
data class ConfiguracionSueldo(
    @PrimaryKey
    val id: Int = 1,              // Solo habrá 1 registro
    
    val montoPorDia: Double,      // Ej: 100.0 (pesos por día por persona)
    val numeroPersonas: Int       // Ej: 2 personas
) {
    // Calcula el sueldo total por día
    fun sueldoTotalDiario(): Double = montoPorDia * numeroPersonas
    
    // Calcula el sueldo total por semana (5 días)
    fun sueldoTotalSemanal(): Double = sueldoTotalDiario() * 5
}
