
package com.example.micardex081.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Materia::class],
    version = 2,
    exportSchema = false
)
abstract class CardexDatabase : RoomDatabase() {

    abstract fun materiaDao(): MateriaDao

    companion object {
        @Volatile
        private var INSTANCE: CardexDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE materias ADD COLUMN tipo TEXT NOT NULL DEFAULT 'MATERIA'"
                )
            }
        }

        fun obtenerInstancia(context: Context): CardexDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CardexDatabase::class.java,
                    "cardex_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
