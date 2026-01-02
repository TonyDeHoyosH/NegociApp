package com.burritoapp.data.repository

import com.burritoapp.data.dao.VentaDao
import com.burritoapp.data.dao.ProductoDao
import com.burritoapp.data.dao.GastoFijoDao
import com.burritoapp.data.dao.ConfiguracionDao
import com.burritoapp.data.model.ReporteSemanal
import com.burritoapp.data.model.ReporteProducto
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class ReporteRepository(
    private val ventaDao: VentaDao,
    private val productoDao: ProductoDao,
    private val gastoFijoDao: GastoFijoDao,
    private val configuracionDao: ConfiguracionDao
) {
    
    // Generar reporte de las últimas N semanas
    suspend fun getReporteUltimasSemanas(numeroSemanas: Int = 4): List<ReporteSemanal> {
        val reportes = mutableListOf<ReporteSemanal>()
        val hoy = LocalDate.now()
        
        // Obtener configuraciones
        val totalGastosMes = gastoFijoDao.getTotalGastosFijosMes() ?: 0.0
        val configSueldo = configuracionDao.getConfiguracionSueldo().first()
        val configGeneral = configuracionDao.getConfiguracionGeneral().first()
        
        if (configSueldo == null || configGeneral == null) return emptyList()
        
        val gastosFijosDia = configGeneral.calcularGastoFijoDiario(totalGastosMes)
        val sueldosDia = configSueldo.sueldoTotalDiario()
        
        for (i in 0 until numeroSemanas) {
            val fecha = hoy.minusWeeks(i.toLong())
            val semana = obtenerNumeroSemana(fecha)
            val (fechaInicio, fechaFin) = obtenerRangoSemana(fecha)
            
            // Obtener ventas de la semana
            val ventasSemana = ventaDao.getVentasRango(fechaInicio, fechaFin).first()
            
            val totalVendido = ventasSemana
                .filter { it.venta.estaPagada() }
                .sumOf { it.venta.montoTotal() }
            
            val numeroVentas = ventasSemana.count { it.venta.estaPagada() }
            val productosVendidos = ventasSemana
                .filter { it.venta.estaPagada() }
                .sumOf { it.venta.cantidad }
            
            // Calcular costos de la semana (5 días de producción)
            val diasProduccion = contarDiasProduccion(fechaInicio, fechaFin)
            val costosTotales = (gastosFijosDia + sueldosDia) * diasProduccion
            
            // Obtener costos de materia prima de productos de esa semana
            val productosSemana = productoDao.getProductosSemanaActual(semana).first()
            val costoMateriaPrima = productosSemana.sumOf { it.costoTotalMateriaPrima() }
            
            val totalCostos = costosTotales + costoMateriaPrima
            val gananciaNeta = totalVendido - totalCostos
            
            reportes.add(
                ReporteSemanal(
                    semana = semana,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin,
                    totalVendido = totalVendido,
                    totalCostos = totalCostos,
                    gananciaNeta = gananciaNeta,
                    numeroVentas = numeroVentas,
                    productosVendidos = productosVendidos
                )
            )
        }
        
        return reportes.reversed() // Más antigua primero para la gráfica
    }
    
    // Generar reporte por producto
    suspend fun getReporteProductos(): List<ReporteProducto> {
        val reportes = mutableListOf<ReporteProducto>()
        
        // Obtener todos los productos con ventas
        val productosConVentas = mutableMapOf<String, MutableList<Pair<Int, List<com.burritoapp.data.entity.VentaConProducto>>>>()
        
        // Agrupar por nombre de producto (ya que puede haber varios del mismo tipo en diferentes semanas)
        val fechaInicio = LocalDate.now().minusMonths(1).toString()
        val fechaFin = LocalDate.now().toString()
        val todasVentas = ventaDao.getVentasRango(fechaInicio, fechaFin).first()
        
        todasVentas.groupBy { it.producto.nombre }.forEach { (nombre, ventas) ->
            val productosIds = ventas.map { it.producto.id }.distinct()
            
            var totalProducido = 0
            var costoTotal = 0.0
            
            for (productoId in productosIds) {
                val producto = productoDao.getProductoConMateriaPrima(productoId)
                producto?.let {
                    totalProducido += it.producto.cantidadProducida ?: 0
                    costoTotal += it.costoTotalMateriaPrima()
                }
            }
            
            val ventasPagadas = ventas.filter { it.venta.estaPagada() }
            val totalVendido = ventasPagadas.sumOf { it.venta.cantidad }
            val ingresoTotal = ventasPagadas.sumOf { it.venta.montoTotal() }
            val porcentajeVendido = if (totalProducido > 0) {
                (totalVendido.toDouble() / totalProducido.toDouble()) * 100
            } else {
                0.0
            }
            
            val precioPromedio = if (totalVendido > 0) {
                ingresoTotal / totalVendido
            } else {
                0.0
            }
            
            reportes.add(
                ReporteProducto(
                    nombreProducto = nombre,
                    vecesProducido = productosIds.size,
                    totalProducido = totalProducido,
                    totalVendido = totalVendido,
                    porcentajeVendido = porcentajeVendido,
                    ingresoTotal = ingresoTotal,
                    costoTotal = costoTotal,
                    gananciaNeta = ingresoTotal - costoTotal,
                    precioPromedioVenta = precioPromedio
                )
            )
        }
        
        return reportes.sortedByDescending { it.gananciaNeta }
    }
    
    private fun obtenerNumeroSemana(fecha: LocalDate): String {
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekOfYear = fecha.get(weekFields.weekOfWeekBasedYear())
        val year = fecha.get(weekFields.weekBasedYear())
        return String.format("%d-W%02d", year, weekOfYear)
    }
    
    private fun obtenerRangoSemana(fecha: LocalDate): Pair<String, String> {
        val weekFields = WeekFields.of(Locale.getDefault())
        val inicio = fecha.with(weekFields.dayOfWeek(), 1)
        val fin = fecha.with(weekFields.dayOfWeek(), 7)
        return Pair(inicio.toString(), fin.toString())
    }
    
    private suspend fun contarDiasProduccion(fechaInicio: String, fechaFin: String): Int {
        // Contar días únicos con producción en el rango
        val ventas = ventaDao.getVentasRango(fechaInicio, fechaFin).first()
        return ventas.map { it.venta.fecha }.distinct().size.coerceAtMost(5)
    }
}
