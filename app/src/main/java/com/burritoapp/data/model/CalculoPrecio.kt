package com.burritoapp.data.model

data class CalculoPrecio(
    val costoMateriaPrima: Double,
    val gastosFijosDia: Double,
    val sueldosDia: Double,
    val cantidadProducida: Int,
    val porcentajeGanancia: Double  // Ej: 0.25 para 25%
) {
    // Costo total para producir todos los productos del día
    val costoTotal: Double = costoMateriaPrima + gastosFijosDia + sueldosDia
    
    // Precio mínimo unitario (punto de equilibrio)
    val precioMinimo: Double = if (cantidadProducida > 0) {
        costoTotal / cantidadProducida
    } else {
        0.0
    }
    
    // Precio sugerido de venta (incluye ganancia)
    val precioSugerido: Double = precioMinimo * (1 + porcentajeGanancia)
    
    // Ganancia neta si se vende todo al precio sugerido
    val gananciaNeta: Double = (precioSugerido * cantidadProducida) - costoTotal
    
    // Porcentaje de sueldo sobre el precio sugerido
    val porcentajeSueldo: Double = if (precioSugerido > 0) {
        (sueldosDia / cantidadProducida) / precioSugerido
    } else {
        0.0
    }
}
