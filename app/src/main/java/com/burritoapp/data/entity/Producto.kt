package com.burritoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val nombre: String,
    val fechaCreacion: String,             // Cuándo se creó el producto
    val fechaProduccion: String? = null,   // NUEVO: Cuándo se produjo (puede ser diferente)
    val semana: String,
    val activo: Boolean = true,
    val cantidadProducida: Int? = null
) {
    companion object {
        fun getCurrentWeek(): String {
            val now = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("YYYY-'W'ww")
            return now.format(formatter)
        }
        
        fun getTodayDate(): String {
            return LocalDate.now().toString()
        }
    }
    
    // Calcula si es el producto del día de hoy
    fun esProductoDelDia(): Boolean {
        return fechaProduccion == getTodayDate()
    }
}
