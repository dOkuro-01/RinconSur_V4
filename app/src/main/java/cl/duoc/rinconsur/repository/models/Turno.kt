package cl.duoc.rinconsur.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "turnos")
data class Turno(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rutHash: String, 
    val sucursal: String,
    val solicita: Long,
    val estado: String = "ESPERA", 
    val cabina: Int? = null,
    val ingreso: Long? = null,
    val salida: Long? = null
)

