# NegociApp
Aplicación para la gestión inteligente de costos, precios y ventas para negocios pequeños de comida

NegociApp es una aplicación móvil Android diseñada para ayudar a pequeños negocios de comida —como un negocio de burritos— a calcular precios de venta reales, controlar costos, gestionar ventas diarias y analizar ganancias, todo basado en datos reales de producción y operación.

La app está pensada para escenarios donde:

Los productos cambian semanalmente.

Los costos varían según materia prima, sueldos y gastos fijos.

El precio óptimo solo puede calcularse después de conocer cuántos productos se produjeron ese día.

🎯 Objetivo principal

Evitar precios “al tanteo” y permitir tomar decisiones basadas en:

Costos reales por producto

Punto de equilibrio diario

Ganancia neta del negocio

Impacto de sueldos y gastos fijos en el precio final

🧩 Funcionalidades clave (MVP)
🛒 Productos y materia prima

CRUD de productos (uno por día).

Cada producto incluye una tabla de materia prima.

Autocálculo por regla de 3:

Precio por kilo + (precio pagado o cantidad comprada).

Cálculo automático del costo total del producto.

🧮 Cálculo de precios

Registro de producción diaria (cuántos productos salieron).

Cálculo de:

Precio mínimo unitario (punto de equilibrio).

Precio sugerido con porcentaje de ganancia.

Considera:

Materia prima

Gastos fijos prorrateados por día trabajado

Sueldos diarios por persona

💰 Ventas

Registro rápido de ventas:

Producto

Cantidad

Precio sugerido vs precio real

Nota (cliente)

Estado: efectivo, tarjeta o pendiente

Historial diario con ventas más recientes arriba.

Gestión de ventas pendientes.

📊 Dashboard

Resumen del día:

Total vendido

Ganancia neta

Cuánto falta para el punto de equilibrio

Producto del día

Lista de ventas del día

Acceso rápido a:

Registrar venta

Registrar producción diaria

⚙️ Configuración

CRUD de gastos fijos (gas, agua, luz, transporte).

Configuración de sueldos:

Monto diario

Número de personas

Porcentaje de ganancia del negocio.

Días trabajados del mes (editable).

📈 Reportes

Gráfica de ganancias netas por semana (últimas 4 semanas).

Punto de equilibrio por producto.

Comparativas de crecimiento semanal y mensual.

🏗️ Arquitectura y tecnologías

Lenguaje: Kotlin

UI: Jetpack Compose (Material 3)

Arquitectura: MVVM con separación clara de responsabilidades

Persistencia: Room

Navegación: Navigation Compose

Estado: ViewModel + StateFlow

Diseño: UX/UI minimalista

Theming: Colores y estilos centralizados (sin hardcode en vistas)

🚀 Enfoque del proyecto

NegociApp no busca ser un sistema contable complejo, sino una herramienta práctica, pensada desde la realidad de un negocio pequeño que produce, vende y cobra día a día.
