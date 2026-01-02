package com.burritoapp.data.dao

import androidx.room.*
import com.burritoapp.data.entity.MateriaPrima
import kotlinx.coroutines.flow.Flow

@Dao
interface MateriaPrimaDao {
    
    @Query("SELECT * FROM materia_prima WHERE productoId = :productoId")
    fun getMateriaPrimaByProducto(productoId: Int): Flow<List<MateriaPrima>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(materiaPrima: MateriaPrima): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materiaPrima: List<MateriaPrima>)
    
    @Update
    suspend fun update(materiaPrima: MateriaPrima)
    
    @Delete
    suspend fun delete(materiaPrima: MateriaPrima)
    
    @Query("DELETE FROM materia_prima WHERE productoId = :productoId")
    suspend fun deleteAllByProducto(productoId: Int)
}
