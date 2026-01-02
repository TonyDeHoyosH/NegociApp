package com.burritoapp.data.dao

import androidx.room.*
import com.burritoapp.data.entity.ConfiguracionSueldo
import com.burritoapp.data.entity.ConfiguracionGeneral
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {
    
    // Configuración de Sueldos
    @Query("SELECT * FROM configuracion_sueldo WHERE id = 1")
    fun getConfiguracionSueldo(): Flow<ConfiguracionSueldo?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguracionSueldo(config: ConfiguracionSueldo)
    
    // Configuración General
    @Query("SELECT * FROM configuracion_general WHERE id = 1")
    fun getConfiguracionGeneral(): Flow<ConfiguracionGeneral?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguracionGeneral(config: ConfiguracionGeneral)
}
