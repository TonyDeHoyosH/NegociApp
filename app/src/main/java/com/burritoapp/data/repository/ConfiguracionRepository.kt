package com.burritoapp.data.repository

import com.burritoapp.data.dao.ConfiguracionDao
import com.burritoapp.data.entity.ConfiguracionSueldo
import com.burritoapp.data.entity.ConfiguracionGeneral
import kotlinx.coroutines.flow.Flow

class ConfiguracionRepository(private val configuracionDao: ConfiguracionDao) {
    
    val configuracionSueldo: Flow<ConfiguracionSueldo?> = 
        configuracionDao.getConfiguracionSueldo()
    
    val configuracionGeneral: Flow<ConfiguracionGeneral?> = 
        configuracionDao.getConfiguracionGeneral()
    
    suspend fun updateConfiguracionSueldo(config: ConfiguracionSueldo) {
        configuracionDao.insertConfiguracionSueldo(config)
    }
    
    suspend fun updateConfiguracionGeneral(config: ConfiguracionGeneral) {
        configuracionDao.insertConfiguracionGeneral(config)
    }
}
