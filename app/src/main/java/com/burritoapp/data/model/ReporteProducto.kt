package com.burritoapp.data.model

data class ReporteProducto(
    val nombreProducto: String,
    val vecesProducido: Int,
    val totalProducido: Int,        // Total de unidades producidas
    val totalVendido: Int,          // Total de unidades vendidas
    val porcentajeVendido: Double,  // % de lo producido que se vendió
    val ingresoTotal: Double,
    val costoTotal: Double,
    val gananciaNeta: Double,
    val precioPromedioVenta: Double
) {
    fun eficienciaVenta(): String {
        return when {
            porcentajeVendido >= 90 -> "Excelente"
            porcentajeVendido >= 70 -> "Bueno"
            porcentajeVendido >= 50 -> "Regular"
            else -> "Bajo"
        }
    }
}
