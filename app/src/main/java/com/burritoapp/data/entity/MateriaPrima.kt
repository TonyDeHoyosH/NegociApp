package com.burritoapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

enum class TipoMedicion {
    POR_KILO,
    POR_UNIDAD;
    
    fun getLabel(): String = when (this) {
        POR_KILO -> "Por Kilo"
        POR_UNIDAD -> "Por Unidad"
    }
    
    fun getLabelPrecio(): String = when (this) {
        POR_KILO -> "Precio por kg"
        POR_UNIDAD -> "Precio por unidad"
    }
    
    fun getLabelCantidad(): String = when (this) {
        POR_KILO -> "Cantidad (kg)"
        POR_UNIDAD -> "Cantidad (unidades)"
    }
}

@Entity(
    tableName = "materia_prima",
    foreignKeys = [
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productoId")]
)
data class MateriaPrima(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val productoId: Int,
    val nombre: String,
    val tipoMedicion: TipoMedicion = TipoMedicion.POR_KILO,
    val precioUnitario: Double?,      // Precio por kg o por unidad según tipo
    val precioPagado: Double?,
    val cantidad: Double?             // Cantidad en kg o unidades según tipo
) {
    // Calcula el campo faltante usando regla de 3
    fun calcularCampoFaltante(): MateriaPrima {
        return when {
            // Caso 1: Tengo precio unitario y cantidad → Calculo precio pagado
            precioUnitario != null && cantidad != null && precioPagado == null -> {
                copy(precioPagado = precioUnitario * cantidad)
            }
            // Caso 2: Tengo precio unitario y precio pagado → Calculo cantidad
            precioUnitario != null && precioUnitario > 0 && precioPagado != null && cantidad == null -> {
                copy(cantidad = precioPagado / precioUnitario)
            }
            // Caso 3: Tengo precio pagado y cantidad → Calculo precio unitario
            precioPagado != null && cantidad != null && cantidad > 0 && precioUnitario == null -> {
                copy(precioUnitario = precioPagado / cantidad)
            }
            else -> this
        }
    }
    
    fun esValido(): Boolean {
        val camposLlenos = listOfNotNull(precioUnitario, precioPagado, cantidad).size
        return camposLlenos >= 2 && nombre.isNotBlank()
    }
    
    // Obtiene el texto descriptivo para mostrar
    fun getDescripcionCantidad(): String {
        return when (tipoMedicion) {
            TipoMedicion.POR_KILO -> "${String.format("%.2f", cantidad ?: 0.0)} kg"
            TipoMedicion.POR_UNIDAD -> "${cantidad?.toInt() ?: 0} unidades"
        }
    }
}
