package com.burritoapp.data.dao

import androidx.room.*
import com.burritoapp.data.entity.GastoFijo
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoFijoDao {
    
    @Query("SELECT * FROM gastos_fijos WHERE activo = 1 ORDER BY nombre ASC")
    fun getAllGastosFijos(): Flow<List<GastoFijo>>
    
    @Query("SELECT SUM(montoMensual) FROM gastos_fijos WHERE activo = 1")
    suspend fun getTotalGastosFijosMes(): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gastoFijo: GastoFijo)
    
    @Update
    suspend fun update(gastoFijo: GastoFijo)
    
    @Query("UPDATE gastos_fijos SET activo = 0 WHERE id = :id")
    suspend fun softDelete(id: Int)
}
