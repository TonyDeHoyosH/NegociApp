package com.burritoapp.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ProductoConMateriaPrima(
    @Embedded val producto: Producto,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "productoId"
    )
    val materiaPrima: List<MateriaPrima>
) {
    // Calcula el costo total de la materia prima
    fun costoTotalMateriaPrima(): Double {
        return materiaPrima.sumOf { it.precioPagado ?: 0.0 }
    }
    
    // Verifica si el producto está completo (tiene materia prima y cantidad producida)
    fun estaCompleto(): Boolean {
        return materiaPrima.isNotEmpty() && producto.cantidadProducida != null
    }
}
