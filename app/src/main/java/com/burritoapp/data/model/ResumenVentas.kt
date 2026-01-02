package com.burritoapp.data.model

data class ResumenVentas(
    val totalVendido: Double,           // Suma de todas las ventas
    val cantidadVentas: Int,            // Número de transacciones
    val unidadesVendidas: Int,          // Total de unidades vendidas
    val ventasPendientes: Int,          // Número de ventas pendientes
    val montoPendiente: Double,         // Monto total pendiente de cobro
    val puntoEquilibrio: Double,        // Costo total del día
    val gananciaNeta: Double,           // Ganancia después de costos
    val porcentajeLogrado: Double       // % de punto de equilibrio alcanzado
) {
    // Calcula cuánto falta para llegar al punto de equilibrio
    fun faltaParaEquilibrio(): Double {
        val falta = puntoEquilibrio - totalVendido
        return if (falta > 0) falta else 0.0
    }
    
    // Verifica si ya se alcanzó el punto de equilibrio
    fun seAlcanzoEquilibrio(): Boolean = totalVendido >= puntoEquilibrio
}
