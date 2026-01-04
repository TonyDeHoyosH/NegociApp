package com.burritoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.burritoapp.data.database.AppDatabase
import com.burritoapp.data.entity.Producto
import com.burritoapp.data.entity.MateriaPrima
import com.burritoapp.data.entity.ProductoConMateriaPrima
import com.burritoapp.data.entity.TipoMedicion
import com.burritoapp.data.entity.EstadoProducto
import com.burritoapp.data.model.CalculoPrecio
import com.burritoapp.data.repository.ProductoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ProductoRepository
    
    val productosSemanaActual: StateFlow<List<ProductoConMateriaPrima>>
    val productoDelDia: StateFlow<ProductoConMateriaPrima?>
    val productosSinProduccion: StateFlow<List<Producto>>
    
    init {
        val productoDao = AppDatabase.getDatabase(application).productoDao()
        val materiaPrimaDao = AppDatabase.getDatabase(application).materiaPrimaDao()
        val gastoFijoDao = AppDatabase.getDatabase(application).gastoFijoDao()
        val configuracionDao = AppDatabase.getDatabase(application).configuracionDao()
        val ventaDao = AppDatabase.getDatabase(application).ventaDao()
        
        repository = ProductoRepository(
            productoDao, 
            materiaPrimaDao, 
            gastoFijoDao, 
            configuracionDao,
            ventaDao
        )
        
        val semanaActual = Producto.getCurrentWeek()
        productosSemanaActual = repository.getProductosSemanaActual(semanaActual)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        
        val fechaHoy = Producto.getTodayDate()
        productoDelDia = repository.getProductoDelDia(fechaHoy)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
        
        productosSinProduccion = repository.getProductosSinProduccion()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
    
    fun crearProducto(nombre: String, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val producto = Producto(
                nombre = nombre,
                fechaCreacion = Producto.getTodayDate(),
                semana = Producto.getCurrentWeek(),
                estado = EstadoProducto.PLANEADO
            )
            val productoId = repository.insertProducto(producto)
            onSuccess(productoId)
        }
    }
    
    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            repository.updateProducto(producto)
        }
    }
    
    fun eliminarProducto(id: Int) {
        viewModelScope.launch {
            repository.deleteProducto(id)
        }
    }
    
    fun actualizarCantidadProducida(productoId: Int, cantidad: Int) {
        viewModelScope.launch {
            repository.registrarProduccionDelDia(productoId, cantidad)
            actualizarEstadoAutomatico(productoId)
        }
    }
    
    fun registrarProduccionDelDia(productoId: Int, cantidad: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.registrarProduccionDelDia(productoId, cantidad)
            actualizarEstadoAutomatico(productoId)
            onSuccess()
        }
    }
    
    fun actualizarProductoDelDia(productoId: Int, cantidad: Int, fecha: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.registrarProduccionDelDia(productoId, cantidad)
            actualizarEstadoAutomatico(productoId)
            onSuccess()
        }
    }
    
    suspend fun calcularPrecio(productoId: Int): CalculoPrecio? {
        return repository.calcularPrecio(productoId)
    }
    
    // Materia Prima - FIRMA ACTUALIZADA
    fun agregarMateriaPrima(
        productoId: Int,
        nombre: String,
        tipoMedicion: TipoMedicion,
        precioUnitario: Double?,
        precioPagado: Double?,
        cantidad: Double?,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val materiaPrima = MateriaPrima(
                productoId = productoId,
                nombre = nombre,
                tipoMedicion = tipoMedicion,
                precioUnitario = precioUnitario,
                precioPagado = precioPagado,
                cantidad = cantidad
            )
            
            if (materiaPrima.esValido()) {
                repository.insertMateriaPrima(materiaPrima)
                actualizarEstadoAutomatico(productoId)
                onSuccess()
            } else {
                onError("Debes llenar al menos 2 campos (Precio unitario, Precio pagado o Cantidad)")
            }
        }
    }
    
    fun actualizarMateriaPrima(materiaPrima: MateriaPrima) {
        viewModelScope.launch {
            if (materiaPrima.esValido()) {
                repository.updateMateriaPrima(materiaPrima)
            }
        }
    }
    
    fun eliminarMateriaPrima(materiaPrima: MateriaPrima) {
        viewModelScope.launch {
            repository.deleteMateriaPrima(materiaPrima)
            actualizarEstadoAutomatico(materiaPrima.productoId)
        }
    }
    
    // Estados de productos
    fun cambiarEstadoProducto(productoId: Int, nuevoEstado: EstadoProducto) {
        viewModelScope.launch {
            repository.cambiarEstadoProducto(productoId, nuevoEstado)
        }
    }
    
    fun actualizarEstadoAutomatico(productoId: Int) {
        viewModelScope.launch {
            repository.actualizarEstadoAutomatico(productoId)
        }
    }
    
    fun actualizarTodosLosEstados() {
        viewModelScope.launch {
            productosSemanaActual.value.forEach { productoConMP ->
                repository.actualizarEstadoAutomatico(productoConMP.producto.id)
            }
        }
    }
}
