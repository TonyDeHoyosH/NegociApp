package com.burritoapp.data.repository

import com.burritoapp.data.dao.VentaDao
import com.burritoapp.data.dao.ProductoDao
import com.burritoapp.data.dao.GastoFijoDao
import com.burritoapp.data.dao.ConfiguracionDao
import com.burritoapp.data.entity.Venta
import com.burritoapp.data.entity.VentaConProducto
import com.burritoapp.data.entity.EstadoVenta
import com.burritoapp.data.entity.Producto
import com.burritoapp.data.model.ResumenVentas
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class VentaRepository(
    private val ventaDao: VentaDao,
    private val productoDao: ProductoDao,
    private val gastoFijoDao: GastoFijoDao,
    private val configuracionDao: ConfiguracionDao
) {
    
    fun getVentasDelDia(fecha: String): Flow<List<VentaConProducto>> {
        return ventaDao.getVentasDelDia(fecha)
    }
    
    fun getVentasPendientes(): Flow<List<VentaConProducto>> {
        return ventaDao.getVentasPendientes()
    }
    
    fun getVentasRango(fechaInicio: String, fechaFin: String): Flow<List<VentaConProducto>> {
        return ventaDao.getVentasRango(fechaInicio, fechaFin)
    }
    
    suspend fun insertVenta(venta: Venta): Long {
        return ventaDao.insert(venta)
    }
    
    suspend fun updateVenta(venta: Venta) {
        ventaDao.update(venta)
    }
    
    suspend fun deleteVenta(venta: Venta) {
        ventaDao.delete(venta)
    }
    
    suspend fun marcarComoPagada(ventaId: Int, estado: EstadoVenta) {
        ventaDao.marcarComoPagada(ventaId, estado)
    }
    
    // Calcular resumen de ventas del día
    suspend fun getResumenVentasDia(fecha: String): ResumenVentas? {
        // Obtener el producto del día
        val productoDelDia = productoDao.getProductoConMateriaPrima(
            productoDao.getProductoDelDia(fecha).first()?.producto?.id ?: return null
        ) ?: return null
        
        // Obtener configuraciones
        val totalGastosMes = gastoFijoDao.getTotalGastosFijosMes() ?: 0.0
        val configSueldo = configuracionDao.getConfiguracionSueldo().first() ?: return null
        val configGeneral = configuracionDao.getConfiguracionGeneral().first() ?: return null
        
        // Calcular punto de equilibrio
        val costoMateriaPrima = productoDelDia.costoTotalMateriaPrima()
        val gastosFijosDia = configGeneral.calcularGastoFijoDiario(totalGastosMes)
        val sueldosDia = configSueldo.sueldoTotalDiario()
        val puntoEquilibrio = costoMateriaPrima + gastosFijosDia + sueldosDia
        
        // Obtener datos de ventas
        val totalVendido = ventaDao.getTotalVendidoPagadoDia(fecha)
        val cantidadVentas = ventaDao.getContadorVentasDia(fecha)
        val unidadesVendidas = ventaDao.getUnidadesVendidasDia(fecha)
        val ventasPendientes = ventaDao.getContadorVentasPendientes()
        val montoPendiente = ventaDao.getMontoPendiente()
        
        // Calcular ganancia neta
        val gananciaNeta = totalVendido - puntoEquilibrio
        
        // Calcular porcentaje logrado
        val porcentajeLogrado = if (puntoEquilibrio > 0) {
            (totalVendido / puntoEquilibrio) * 100
        } else {
            0.0
        }
        
        return ResumenVentas(
            totalVendido = totalVendido,
            cantidadVentas = cantidadVentas,
            unidadesVendidas = unidadesVendidas,
            ventasPendientes = ventasPendientes,
            montoPendiente = montoPendiente,
            puntoEquilibrio = puntoEquilibrio,
            gananciaNeta = gananciaNeta,
            porcentajeLogrado = porcentajeLogrado
        )
    }
}
