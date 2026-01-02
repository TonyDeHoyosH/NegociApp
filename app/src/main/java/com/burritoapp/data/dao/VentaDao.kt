package com.burritoapp.data.dao

import androidx.room.*
import com.burritoapp.data.entity.Venta
import com.burritoapp.data.entity.VentaConProducto
import com.burritoapp.data.entity.EstadoVenta
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {
    
    // Obtener todas las ventas del día con producto
    @Transaction
    @Query("SELECT * FROM ventas WHERE fecha = :fecha ORDER BY timestamp DESC")
    fun getVentasDelDia(fecha: String): Flow<List<VentaConProducto>>
    
    // Obtener ventas pendientes
    @Transaction
    @Query("SELECT * FROM ventas WHERE estado = 'PENDIENTE' ORDER BY timestamp DESC")
    fun getVentasPendientes(): Flow<List<VentaConProducto>>
    
    // Obtener ventas de un rango de fechas
    @Transaction
    @Query("SELECT * FROM ventas WHERE fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY timestamp DESC")
    fun getVentasRango(fechaInicio: String, fechaFin: String): Flow<List<VentaConProducto>>
    
    // Calcular total vendido del día
    @Query("SELECT COALESCE(SUM(precioReal * cantidad), 0) FROM ventas WHERE fecha = :fecha")
    suspend fun getTotalVendidoDia(fecha: String): Double
    
    // Calcular total vendido del día (solo pagadas)
    @Query("SELECT COALESCE(SUM(precioReal * cantidad), 0) FROM ventas WHERE fecha = :fecha AND estado != 'PENDIENTE'")
    suspend fun getTotalVendidoPagadoDia(fecha: String): Double
    
    // Contar ventas del día
    @Query("SELECT COUNT(*) FROM ventas WHERE fecha = :fecha")
    suspend fun getContadorVentasDia(fecha: String): Int
    
    // Contar ventas pendientes
    @Query("SELECT COUNT(*) FROM ventas WHERE estado = 'PENDIENTE'")
    suspend fun getContadorVentasPendientes(): Int
    
    // Sumar monto pendiente
    @Query("SELECT COALESCE(SUM(precioReal * cantidad), 0) FROM ventas WHERE estado = 'PENDIENTE'")
    suspend fun getMontoPendiente(): Double
    
    // Sumar unidades vendidas del día
    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM ventas WHERE fecha = :fecha")
    suspend fun getUnidadesVendidasDia(fecha: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(venta: Venta): Long
    
    @Update
    suspend fun update(venta: Venta)
    
    @Delete
    suspend fun delete(venta: Venta)
    
    // Marcar venta como pagada
    @Query("UPDATE ventas SET estado = :estado WHERE id = :ventaId")
    suspend fun marcarComoPagada(ventaId: Int, estado: EstadoVenta)
}
