package com.example.MiCardex081.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MateriaDao {

    @Query("SELECT * FROM materias ORDER BY semestre, nombreMateria")
    suspend fun obtenerTodas(): List<Materia>

    @Insert
    suspend fun insertar(materia: Materia)

    @Update
    suspend fun actualizar(materia: Materia)

    @Delete
    suspend fun eliminar(materia: Materia)

    @Query("SELECT * FROM materias WHERE semestre = :numeroSemestre ORDER BY nombreMateria")
    suspend fun obtenerPorSemestre(numeroSemestre: Int): List<Materia>
}