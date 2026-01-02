package com.burritoapp.data.dao

import androidx.room.*
import com.burritoapp.data.entity.Producto
import com.burritoapp.data.entity.ProductoConMateriaPrima
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    
    @Transaction
    @Query("SELECT * FROM productos WHERE semana = :semana AND activo = 1 ORDER BY fechaCreacion DESC")
    fun getProductosSemanaActual(semana: String): Flow<List<ProductoConMateriaPrima>>
    
    @Transaction
    @Query("SELECT * FROM productos WHERE id = :productoId")
    suspend fun getProductoConMateriaPrima(productoId: Int): ProductoConMateriaPrima?
    
    @Transaction
    @Query("SELECT * FROM productos WHERE fechaProduccion = :fecha AND activo = 1 LIMIT 1")
    fun getProductoDelDia(fecha: String): Flow<ProductoConMateriaPrima?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: Producto): Long
    
    @Update
    suspend fun update(producto: Producto)
    
    @Query("UPDATE productos SET activo = 0 WHERE id = :id")
    suspend fun softDelete(id: Int)
    
    // NUEVO: Actualizar producción del día
    @Query("UPDATE productos SET cantidadProducida = :cantidad, fechaProduccion = :fecha WHERE id = :productoId")
    suspend fun updateProduccionDelDia(productoId: Int, cantidad: Int, fecha: String)
    
    // NUEVO: Obtener productos sin cantidad producida
    @Query("SELECT * FROM productos WHERE cantidadProducida IS NULL AND activo = 1 ORDER BY fechaCreacion DESC")
    fun getProductosSinProduccion(): Flow<List<Producto>>
}
