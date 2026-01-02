package com.burritoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos_fijos")
data class GastoFijo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val nombre: String,           // "Gas", "Agua", "Luz", "Transporte"
    val montoMensual: Double,     // Monto que se paga al mes
    val activo: Boolean = true    // Para soft-delete
)
