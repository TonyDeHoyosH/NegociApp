package com.burritoapp.ui.navigation

sealed class Screen(val route: String, val title: String, val icon: String) {
    object Dashboard : Screen("dashboard", "Inicio", "📊")
    object Productos : Screen("productos", "Productos", "🌮")
    object Ventas : Screen("ventas", "Ventas", "💰")
    object Configuracion : Screen("configuracion", "Configuración", "⚙️")
    object Reportes : Screen("reportes", "Reportes", "📈")
    
    // Pantallas secundarias (no aparecen en navbar)
    object GastosFijos : Screen("gastos_fijos", "Gastos Fijos", "")
    object FormularioMateriaPrima : Screen("formulario_materia_prima/{productoId}/{nombreProducto}", "Materia Prima", "") {
        fun createRoute(productoId: Int, nombreProducto: String) = 
            "formulario_materia_prima/$productoId/$nombreProducto"
    }
}
