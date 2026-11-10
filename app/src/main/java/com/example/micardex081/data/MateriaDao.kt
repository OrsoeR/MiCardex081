
package com.example.micardex081.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MateriaDao {

    @Query("SELECT * FROM materias ORDER BY id DESC")
    suspend fun obtenerTodas(): List<Materia>

    @Insert
    suspend fun insertar(materia: Materia)

    @Update
    suspend fun actualizar(materia: Materia)

    @Delete
    suspend fun eliminar(materia: Materia)

    @Query("SELECT * FROM materias WHERE semestre = :semestre AND tipo = 'MATERIA'")
    suspend fun obtenerPorSemestre(semestre: Int): List<Materia>
}
