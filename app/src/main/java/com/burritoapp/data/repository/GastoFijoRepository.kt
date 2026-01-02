package com.burritoapp.data.repository

import com.burritoapp.data.dao.GastoFijoDao
import com.burritoapp.data.entity.GastoFijo
import kotlinx.coroutines.flow.Flow

class GastoFijoRepository(private val gastoFijoDao: GastoFijoDao) {
    
    val allGastosFijos: Flow<List<GastoFijo>> = gastoFijoDao.getAllGastosFijos()
    
    suspend fun getTotalGastosMes(): Double {
        return gastoFijoDao.getTotalGastosFijosMes() ?: 0.0
    }
    
    suspend fun insert(gastoFijo: GastoFijo) {
        gastoFijoDao.insert(gastoFijo)
    }
    
    suspend fun update(gastoFijo: GastoFijo) {
        gastoFijoDao.update(gastoFijo)
    }
    
    suspend fun delete(id: Int) {
        gastoFijoDao.softDelete(id)
    }
}
