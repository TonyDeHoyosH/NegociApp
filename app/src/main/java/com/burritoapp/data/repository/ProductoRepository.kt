package com.burritoapp.data.repository

import com.burritoapp.data.dao.ProductoDao
import com.burritoapp.data.dao.MateriaPrimaDao
import com.burritoapp.data.dao.GastoFijoDao
import com.burritoapp.data.dao.ConfiguracionDao
import com.burritoapp.data.dao.VentaDao
import com.burritoapp.data.entity.Producto
import com.burritoapp.data.entity.MateriaPrima
import com.burritoapp.data.entity.ProductoConMateriaPrima
import com.burritoapp.data.entity.EstadoProducto
import com.burritoapp.data.model.CalculoPrecio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ProductoRepository(
    private val productoDao: ProductoDao,
    private val materiaPrimaDao: MateriaPrimaDao,
    private val gastoFijoDao: GastoFijoDao,
    private val configuracionDao: ConfiguracionDao,
    private val ventaDao: VentaDao
) {
    
    fun getProductosSemanaActual(semana: String): Flow<List<ProductoConMateriaPrima>> {
        return productoDao.getProductosSemanaActual(semana)
    }
    
    fun getProductoDelDia(fecha: String): Flow<ProductoConMateriaPrima?> {
        return productoDao.getProductoDelDia(fecha)
    }
    
    fun getProductosSinProduccion(): Flow<List<Producto>> {
        return productoDao.getProductosSinProduccion()
    }
    
    suspend fun getProductoConMateriaPrima(productoId: Int): ProductoConMateriaPrima? {
        return productoDao.getProductoConMateriaPrima(productoId)
    }
    
    suspend fun insertProducto(producto: Producto): Long {
        return productoDao.insert(producto)
    }
    
    suspend fun updateProducto(producto: Producto) {
        productoDao.update(producto)
    }
    
    suspend fun deleteProducto(id: Int) {
        productoDao.softDelete(id)
    }
    
    // NUEVO: Registrar producción del día
    suspend fun registrarProduccionDelDia(productoId: Int, cantidad: Int) {
        val fecha = Producto.getTodayDate()
        productoDao.updateProduccionDelDia(productoId, cantidad, fecha)
    }

    // Alias para compatibilidad con ViewModel
    suspend fun updateCantidadProducida(productoId: Int, cantidad: Int) {
        registrarProduccionDelDia(productoId, cantidad)
    }
    
    // NUEVO: Calcular precio mínimo y sugerido
    suspend fun calcularPrecio(productoId: Int): CalculoPrecio? {
        val productoConMP = productoDao.getProductoConMateriaPrima(productoId) ?: return null
        val cantidad = productoConMP.producto.cantidadProducida ?: return null
        
        // Obtener configuraciones
        val totalGastosMes = gastoFijoDao.getTotalGastosFijosMes() ?: 0.0
        val configSueldo = configuracionDao.getConfiguracionSueldo().first()
        val configGeneral = configuracionDao.getConfiguracionGeneral().first()
        
        if (configSueldo == null || configGeneral == null) return null
        
        // Calcular costos
        val costoMateriaPrima = productoConMP.costoTotalMateriaPrima()
        val gastosFijosDia = configGeneral.calcularGastoFijoDiario(totalGastosMes)
        val sueldosDia = configSueldo.sueldoTotalDiario()
        
        return CalculoPrecio(
            costoMateriaPrima = costoMateriaPrima,
            gastosFijosDia = gastosFijosDia,
            sueldosDia = sueldosDia,
            cantidadProducida = cantidad,
            porcentajeGanancia = configGeneral.porcentajeGanancia
        )
    }
    
    // Materia Prima
    suspend fun insertMateriaPrima(materiaPrima: MateriaPrima): Long {
        val materiaPrimaCalculada = materiaPrima.calcularCampoFaltante()
        return materiaPrimaDao.insert(materiaPrimaCalculada)
    }
    
    suspend fun insertAllMateriaPrima(materiasPrimas: List<MateriaPrima>) {
        val calculadas = materiasPrimas.map { it.calcularCampoFaltante() }
        materiaPrimaDao.insertAll(calculadas)
    }
    
    suspend fun updateMateriaPrima(materiaPrima: MateriaPrima) {
        val materiaPrimaCalculada = materiaPrima.calcularCampoFaltante()
        materiaPrimaDao.update(materiaPrimaCalculada)
    }
    
    suspend fun deleteMateriaPrima(materiaPrima: MateriaPrima) {
        materiaPrimaDao.delete(materiaPrima)
    }
    
    suspend fun deleteAllMateriaPrimaByProducto(productoId: Int) {
        materiaPrimaDao.deleteAllByProducto(productoId)
    }

    // Cambiar estado manualmente
    suspend fun cambiarEstadoProducto(productoId: Int, nuevoEstado: EstadoProducto) {
        val producto = productoDao.getProductoConMateriaPrima(productoId)?.producto
        if (producto != null) {
            productoDao.update(producto.copy(estado = nuevoEstado))
        }
    }

    // Actualizar estado automáticamente según los datos
    suspend fun actualizarEstadoAutomatico(productoId: Int) {
        val productoConMP = productoDao.getProductoConMateriaPrima(productoId) ?: return
        val producto = productoConMP.producto
        
        // No actualizar si está marcado como completado manualmente
        if (producto.estado == EstadoProducto.COMPLETADO) return
        
        // Calcular unidades vendidas
        val fecha = producto.fechaProduccion ?: producto.fechaCreacion
        val ventas = ventaDao.getVentasDelDia(fecha).first()
        val unidadesVendidas = ventas
            .filter { it.venta.productoId == productoId }
            .sumOf { it.venta.cantidad }
        
        // Calcular estado automático
        val nuevoEstado = producto.calcularEstadoAutomatico(
            tieneMateriaPrima = productoConMP.materiaPrima.isNotEmpty(),
            unidadesVendidas = unidadesVendidas
        )
        
        // Actualizar solo si cambió
        if (nuevoEstado != producto.estado) {
            productoDao.update(producto.copy(estado = nuevoEstado))
        }
    }
}
