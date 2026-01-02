package com.burritoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.burritoapp.data.database.AppDatabase
import com.burritoapp.data.entity.ConfiguracionSueldo
import com.burritoapp.data.entity.ConfiguracionGeneral
import com.burritoapp.data.repository.ConfiguracionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class ConfiguracionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ConfiguracionRepository
    
    val configuracionSueldo: StateFlow<ConfiguracionSueldo?>
    val configuracionGeneral: StateFlow<ConfiguracionGeneral?>
    
    init {
        val configuracionDao = AppDatabase.getDatabase(application).configuracionDao()
        repository = ConfiguracionRepository(configuracionDao)
        
        configuracionSueldo = repository.configuracionSueldo
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
        
        configuracionGeneral = repository.configuracionGeneral
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }
    
    fun updateSueldo(montoPorDia: Double, numeroPersonas: Int) {
        viewModelScope.launch {
            val config = ConfiguracionSueldo(
                montoPorDia = montoPorDia,
                numeroPersonas = numeroPersonas
            )
            repository.updateConfiguracionSueldo(config)
        }
    }
    
    fun updateConfiguracionGeneral(porcentajeGanancia: Double, diasTrabajadosMes: Int) {
        viewModelScope.launch {
            val mesActual = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val config = ConfiguracionGeneral(
                porcentajeGanancia = porcentajeGanancia,
                diasTrabajadosMes = diasTrabajadosMes,
                mesActual = mesActual
            )
            repository.updateConfiguracionGeneral(config)
        }
    }
}
