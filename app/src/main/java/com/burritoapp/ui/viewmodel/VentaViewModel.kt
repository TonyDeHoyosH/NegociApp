package com.burritoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.burritoapp.data.database.AppDatabase
import com.burritoapp.data.entity.Venta
import com.burritoapp.data.entity.VentaConProducto
import com.burritoapp.data.entity.EstadoVenta
import com.burritoapp.data.entity.Producto
import com.burritoapp.data.model.ResumenVentas
import com.burritoapp.data.repository.VentaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VentaViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: VentaRepository
    
    val ventasDelDia: StateFlow<List<VentaConProducto>>
    val ventasPendientes: StateFlow<List<VentaConProducto>>
    val resumenDelDia: StateFlow<ResumenVentas?>
    
    // Filtro por rango de fechas
    private val _ventasRango = MutableStateFlow<List<VentaConProducto>>(emptyList())
    val ventasRango: StateFlow<List<VentaConProducto>> = _ventasRango

    init {
        val ventaDao = AppDatabase.getDatabase(application).ventaDao()
        val productoDao = AppDatabase.getDatabase(application).productoDao()
        val gastoFijoDao = AppDatabase.getDatabase(application).gastoFijoDao()
        val configuracionDao = AppDatabase.getDatabase(application).configuracionDao()

        repository = VentaRepository(ventaDao, productoDao, gastoFijoDao, configuracionDao)

        val fechaHoy = Producto.getTodayDate()

        ventasDelDia = repository.getVentasDelDia(fechaHoy)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        ventasPendientes = repository.getVentasPendientes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Calcular resumen automáticamente cuando cambian las ventas
        resumenDelDia = ventasDelDia.map {
            repository.getResumenVentasDia(fechaHoy)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun registrarVenta(
        productoId: Int,
        cantidad: Int,
        precioSugerido: Double,
        precioReal: Double,
        nota: String,
        estado: EstadoVenta,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val venta = Venta(
                productoId = productoId,
                fecha = Producto.getTodayDate(),
                cantidad = cantidad,
                precioSugerido = precioSugerido,
                precioReal = precioReal,
                nota = nota,
                estado = estado
            )
            repository.insertVenta(venta)
            onSuccess()
        }
    }

    fun actualizarVenta(venta: Venta) {
        viewModelScope.launch {
            repository.updateVenta(venta)
        }
    }

    fun eliminarVenta(venta: Venta) {
        viewModelScope.launch {
            repository.deleteVenta(venta)
        }
    }

    fun marcarComoPagada(ventaId: Int, estado: EstadoVenta) {
        viewModelScope.launch {
            repository.marcarComoPagada(ventaId, estado)
        }
    }

    suspend fun getUnidadesDisponibles(productoId: Int, ventaEditandoId: Int? = null): Int {
        val fecha = com.burritoapp.data.entity.Producto.getTodayDate()
        return repository.getUnidadesDisponibles(productoId, fecha, ventaEditandoId)
    }

    fun cargarVentasRango(fechaInicio: String, fechaFin: String) {
        viewModelScope.launch {
            repository.getVentasRango(fechaInicio, fechaFin).collect {
                _ventasRango.value = it
            }
        }
    }
}
