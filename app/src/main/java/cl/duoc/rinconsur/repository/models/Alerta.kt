package cl.duoc.rinconsur.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alertas")
data class Alerta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sucursal: String,
    val cabina: Int? = null,
    val turnoId: Long? = null,
    val tipo: String,
    val texto: String,
    val creada: Long,
    val resuelta: Boolean = false
)

