package cl.duoc.rinconsur.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuraciones")
data class Configuracion(
    @PrimaryKey val sucursal: String,
    val abierta: Boolean = true,
    val cierre: Long? = null,
    val colacion: Boolean = false
)


