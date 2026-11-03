package cl.duoc.rinconsur.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

 
@Entity(tableName = "registros_qr")
data class RegistroQr(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val centroComercial: String,
    val tienda: String,
    val seccion: String,
    val sucursal: String,
    val fechaEscaneo: Long,
    val latitud: Double? = null,
    val longitud: Double? = null
)

 
data class DatosQr(
    val centroComercial: String,
    val tienda: String,
    val seccion: String,
    val sucursal: String
)

data class Coordenadas(val latitud: Double, val longitud: Double)
