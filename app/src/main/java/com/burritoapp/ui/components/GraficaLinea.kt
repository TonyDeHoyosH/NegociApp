package com.burritoapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*
import kotlin.math.max

@Composable
fun GraficaLinea(
    datos: List<Pair<String, Double>>,  // (label, valor)
    modifier: Modifier = Modifier,
    colorLinea: Color = MaterialTheme.colorScheme.primary,
    colorPuntos: Color = MaterialTheme.colorScheme.primary,
    mostrarValores: Boolean = true
) {
    if (datos.isEmpty()) {
        Box(
            modifier = modifier.height(200.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "No hay datos suficientes",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    val maxValor = datos.maxOfOrNull { it.second } ?: 1.0
    val minValor = datos.minOfOrNull { it.second } ?: 0.0
    val rangoValor = max(maxValor - minValor, 1.0)
    
    Column(modifier = modifier) {
        // Gráfica
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        ) {
            val width = size.width
            val height = size.height
            val spacingX = width / (datos.size - 1).coerceAtLeast(1)
            
            // Dibujar líneas
            val path = Path()
            datos.forEachIndexed { index, (_, valor) ->
                val x = index * spacingX
                val y = height - ((valor - minValor) / rangoValor * height).toFloat()
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                color = colorLinea,
                style = Stroke(width = 4.dp.toPx())
            )
            
            // Dibujar puntos
            datos.forEachIndexed { index, (_, valor) ->
                val x = index * spacingX
                val y = height - ((valor - minValor) / rangoValor * height).toFloat()
                
                // Punto
                drawCircle(
                    color = colorPuntos,
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )
                
                // Valor sobre el punto
                if (mostrarValores) {
                    drawContext.canvas.nativeCanvas.apply {
                        val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
                        val textoValor = formato.format(valor)
                        
                        drawText(
                            textoValor,
                            x,
                            y - 20.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = colorLinea.hashCode()
                                textSize = 12.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
        
        // Labels en el eje X
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            datos.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
