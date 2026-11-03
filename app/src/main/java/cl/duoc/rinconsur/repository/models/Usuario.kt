package cl.duoc.rinconsur.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

 
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val usuario: String,
    val password: String,
    val nombre: String
)
