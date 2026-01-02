package com.burritoapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.burritoapp.data.dao.*
import com.burritoapp.data.entity.*

@Database(
    entities = [
        GastoFijo::class,
        ConfiguracionSueldo::class,
        ConfiguracionGeneral::class,
        Producto::class,
        MateriaPrima::class,
        Venta::class
    ],
    version = 4,  // INCREMENTA LA VERSIÓN
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun gastoFijoDao(): GastoFijoDao
    abstract fun configuracionDao(): ConfiguracionDao
    abstract fun productoDao(): ProductoDao
    abstract fun materiaPrimaDao(): MateriaPrimaDao
    abstract fun ventaDao(): VentaDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "burrito_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
