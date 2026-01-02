package com.burritoapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ventas",
    foreignKeys = [
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productoId"), Index("fecha")]
)
data class Venta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val productoId: Int,
    val fecha: String,              // Fecha de la venta (yyyy-MM-dd)
    val cantidad: Int,              // Cantidad vendida
    val precioSugerido: Double,     // Precio que debería venderse
    val precioReal: Double,         // Precio al que realmente se vendió
    val nota: String,               // Nombre del cliente o nota
    val estado: EstadoVenta,        // PAGADO_EFECTIVO, PAGADO_TARJETA, PENDIENTE
    val timestamp: Long = System.currentTimeMillis()  // Para ordenar por más reciente
) {
    // Calcula el monto total de esta venta
    fun montoTotal(): Double = precioReal * cantidad
    
    // Verifica si está pagada
    fun estaPagada(): Boolean = estado != EstadoVenta.PENDIENTE
    
    // Calcula la diferencia con el precio sugerido
    fun diferenciaPrecio(): Double = precioReal - precioSugerido
}

enum class EstadoVenta {
    PAGADO_EFECTIVO,
    PAGADO_TARJETA,
    PENDIENTE;
    
    fun getIcono(): String = when (this) {
        PAGADO_EFECTIVO -> "💵"
        PAGADO_TARJETA -> "💳"
        PENDIENTE -> "⏳"
    }
    
    fun getNombre(): String = when (this) {
        PAGADO_EFECTIVO -> "Efectivo"
        PAGADO_TARJETA -> "Tarjeta"
        PENDIENTE -> "Pendiente"
    }
}
