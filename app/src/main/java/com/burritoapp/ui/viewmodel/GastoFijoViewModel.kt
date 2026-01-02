package com.burritoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.burritoapp.data.database.AppDatabase
import com.burritoapp.data.entity.GastoFijo
import com.burritoapp.data.repository.GastoFijoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GastoFijoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: GastoFijoRepository
    
    val gastosFijos: StateFlow<List<GastoFijo>>
    val totalGastosMes: StateFlow<Double>
    
    init {
        val gastoFijoDao = AppDatabase.getDatabase(application).gastoFijoDao()
        repository = GastoFijoRepository(gastoFijoDao)
        
        gastosFijos = repository.allGastosFijos
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        
        totalGastosMes = gastosFijos.map { gastos ->
            gastos.sumOf { it.montoMensual }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    }
    
    fun insertGastoFijo(nombre: String, monto: Double) {
        viewModelScope.launch {
            val gastoFijo = GastoFijo(
                nombre = nombre,
                montoMensual = monto
            )
            repository.insert(gastoFijo)
        }
    }
    
    fun updateGastoFijo(id: Int, nombre: String, monto: Double) {
        viewModelScope.launch {
            val gastoFijo = GastoFijo(
                id = id,
                nombre = nombre,
                montoMensual = monto
            )
            repository.update(gastoFijo)
        }
    }
    
    fun deleteGastoFijo(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
