package com.burritoapp.data.model

data class ReporteSemanal(
    val semana: String,              // "2025-W03"
    val fechaInicio: String,         // "2025-01-13"
    val fechaFin: String,            // "2025-01-19"
    val totalVendido: Double,
    val totalCostos: Double,
    val gananciaNeta: Double,
    val numeroVentas: Int,
    val productosVendidos: Int
) {
    // Calcula el crecimiento respecto a otra semana
    fun calcularCrecimiento(semanaAnterior: ReporteSemanal?): Double {
        if (semanaAnterior == null || semanaAnterior.gananciaNeta == 0.0) return 0.0
        return ((gananciaNeta - semanaAnterior.gananciaNeta) / semanaAnterior.gananciaNeta) * 100
    }
    
    // Formatea la semana para mostrar
    fun formatoSemana(): String {
        val numero = semana.substringAfter("W")
        return "Sem $numero"
    }
}
