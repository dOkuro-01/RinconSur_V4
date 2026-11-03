package cl.duoc.rinconsur.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cabinas", primaryKeys = ["sucursal", "numero"])
data class Cabina(
    val sucursal: String,
    val numero: Int,
    val estado: String = "LIBRE", 
    val inicio: Long? = null,
    val turnoId: Long? = null
)

