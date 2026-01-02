package com.burritoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.burritoapp.data.database.AppDatabase
import com.burritoapp.data.model.ReporteSemanal
import com.burritoapp.data.model.ReporteProducto
import com.burritoapp.data.repository.ReporteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReporteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ReporteRepository
    
    private val _reportesSemanales = MutableStateFlow<List<ReporteSemanal>>(emptyList())
    val reportesSemanales: StateFlow<List<ReporteSemanal>> = _reportesSemanales
    private val _reportesProductos = MutableStateFlow<List<ReporteProducto>>(emptyList())
    val reportesProductos: StateFlow<List<ReporteProducto>> = _reportesProductos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        val ventaDao = AppDatabase.getDatabase(application).ventaDao()
        val productoDao = AppDatabase.getDatabase(application).productoDao()
        val gastoFijoDao = AppDatabase.getDatabase(application).gastoFijoDao()
        val configuracionDao = AppDatabase.getDatabase(application).configuracionDao()
        
        repository = ReporteRepository(ventaDao, productoDao, gastoFijoDao, configuracionDao)
        
        // Cargar reportes inicialmente
        cargarReportes()
    }

    fun cargarReportes() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val reportesSemanales = repository.getReporteUltimasSemanas(4)
                _reportesSemanales.value = reportesSemanales
                
                val reportesProductos = repository.getReporteProductos()
                _reportesProductos.value = reportesProductos
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Obtener crecimiento de la semana actual vs anterior
    fun getCrecimientoSemanal(): Double {
        val reportes = _reportesSemanales.value
        if (reportes.size < 2) return 0.0
        
        val semanaActual = reportes.last()
        val semanaAnterior = reportes[reportes.size - 2]
        
        return semanaActual.calcularCrecimiento(semanaAnterior)
    }

    // Obtener mejor producto
    fun getMejorProducto(): ReporteProducto? {
        return _reportesProductos.value.maxByOrNull { it.gananciaNeta }
    }
}
