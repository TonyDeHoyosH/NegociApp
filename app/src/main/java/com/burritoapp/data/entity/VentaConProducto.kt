package com.burritoapp.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class VentaConProducto(
    @Embedded val venta: Venta,
    
    @Relation(
        parentColumn = "productoId",
        entityColumn = "id"
    )
    val producto: Producto
)
