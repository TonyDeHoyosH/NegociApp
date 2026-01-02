package com.burritoapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "materia_prima",
    foreignKeys = [
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE  // Si se borra el producto, se borra su materia prima
        )
    ],
    indices = [Index("productoId")]  // Índice para mejorar performance
)
data class MateriaPrima(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val productoId: Int,              // ID del producto al que pertenece
    val nombre: String,               // "Carne de pastor", "Tortillas", etc.
    val precioKg: Double?,            // Precio por kilogramo (puede ser null inicialmente)
    val precioPagado: Double?,        // Precio total pagado (puede ser null inicialmente)
    val cantidadKg: Double?           // Cantidad en kilogramos (puede ser null inicialmente)
) {
    // Calcula el campo faltante usando regla de 3
    fun calcularCampoFaltante(): MateriaPrima {
        return when {
            // Caso 1: Tengo precio/kg y cantidad → Calculo precio pagado
            precioKg != null && cantidadKg != null && precioPagado == null -> {
                copy(precioPagado = precioKg * cantidadKg)
            }
            // Caso 2: Tengo precio/kg y precio pagado → Calculo cantidad
            precioKg != null && precioPagado != null && cantidadKg == null -> {
                copy(cantidadKg = precioPagado / precioKg)
            }
            // Caso 3: Tengo precio pagado y cantidad → Calculo precio/kg
            precioPagado != null && cantidadKg != null && precioKg == null -> {
                copy(precioKg = precioPagado / cantidadKg)
            }
            // Ya tiene todos los campos o no se puede calcular
            else -> this
        }
    }
    
    // Valida que al menos 2 campos estén llenos
    fun esValido(): Boolean {
        val camposLlenos = listOfNotNull(precioKg, precioPagado, cantidadKg).size
        return camposLlenos >= 2 && nombre.isNotBlank()
    }
}
