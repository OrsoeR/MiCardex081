
package com.example.micardex081.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materias")
data class Materia(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombreMateria: String,
    val semestre: Int,
    val normal: Double? = null,
    val repeticion: Double? = null,
    val especial: Double? = null,
    val tipo: String = "MATERIA"
)
