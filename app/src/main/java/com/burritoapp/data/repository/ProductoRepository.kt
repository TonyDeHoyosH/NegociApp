package com.burritoapp.data.repository

import com.burritoapp.data.dao.ProductoDao
import com.burritoapp.data.dao.MateriaPrimaDao
import com.burritoapp.data.dao.GastoFijoDao
import com.burritoapp.data.dao.ConfiguracionDao
import com.burritoapp.data.entity.Producto
import com.burritoapp.data.entity.MateriaPrima
import com.burritoapp.data.entity.ProductoConMateriaPrima
import com.burritoapp.data.model.CalculoPrecio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ProductoRepository(
    private val productoDao: ProductoDao,
    private val materiaPrimaDao: MateriaPrimaDao,
    private val gastoFijoDao: GastoFijoDao,
    private val configuracionDao: ConfiguracionDao
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
}
