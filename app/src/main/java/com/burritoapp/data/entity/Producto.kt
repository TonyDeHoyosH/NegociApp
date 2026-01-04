package com.burritoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class EstadoProducto {
    PLANEADO,
    COMPRADO,
    PRODUCIDO,
    COMPLETADO;
    
    fun getIcono(): String = when (this) {
        PLANEADO -> "📝"
        COMPRADO -> "🛒"
        PRODUCIDO -> "✅"
        COMPLETADO -> "🏁"
    }
    
    fun getNombre(): String = when (this) {
        PLANEADO -> "Planeado"
        COMPRADO -> "Comprado"
        PRODUCIDO -> "Producido"
        COMPLETADO -> "Completado"
    }
    
    fun getColor(): androidx.compose.ui.graphics.Color {
        return when (this) {
            PLANEADO -> androidx.compose.ui.graphics.Color(0xFFB0BEC5)      // Gris
            COMPRADO -> androidx.compose.ui.graphics.Color(0xFF2196F3)      // Azul
            PRODUCIDO -> androidx.compose.ui.graphics.Color(0xFF4CAF50)     // Verde
            COMPLETADO -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)    // Gris oscuro
        }
    }
}

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val nombre: String,
    val fechaCreacion: String,
    val fechaProduccion: String? = null,
    val semana: String,
    val activo: Boolean = true,
    val cantidadProducida: Int? = null,
    val estado: EstadoProducto = EstadoProducto.PLANEADO
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
    
    fun esProductoDelDia(): Boolean {
        return fechaProduccion == getTodayDate()
    }
    
    // Determina automáticamente el estado basado en los datos
    fun calcularEstadoAutomatico(
        tieneMateriaPrima: Boolean,
        unidadesVendidas: Int
    ): EstadoProducto {
        return when {
            // Si ya está completado manualmente, mantenerlo
            estado == EstadoProducto.COMPLETADO -> EstadoProducto.COMPLETADO
            
            // Si no tiene materia prima
            !tieneMateriaPrima -> EstadoProducto.PLANEADO
            
            // Si tiene materia prima pero no cantidad producida
            cantidadProducida == null -> EstadoProducto.COMPRADO
            
            // Si se vendió todo
            cantidadProducida != null && unidadesVendidas >= cantidadProducida -> EstadoProducto.COMPLETADO
            
            // Si tiene cantidad producida
            cantidadProducida != null -> EstadoProducto.PRODUCIDO
            
            else -> EstadoProducto.PLANEADO
        }
    }
}
