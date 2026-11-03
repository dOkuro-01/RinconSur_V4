package cl.duoc.rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.rinconsur.repository.models.RegistroQr
import kotlinx.coroutines.flow.Flow

 
@Dao
interface RegistroQrDao {
    @Insert suspend fun insertar(registro: RegistroQr): Long 

    @Query("SELECT * FROM registros_qr ORDER BY fechaEscaneo DESC, id DESC")
    fun observarTodos(): Flow<List<RegistroQr>> 

    @Query("SELECT * FROM registros_qr WHERE centroComercial LIKE '%' || :texto || '%' " +
           "OR tienda LIKE '%' || :texto || '%' OR seccion LIKE '%' || :texto || '%' " +
           "OR sucursal LIKE '%' || :texto || '%' ORDER BY fechaEscaneo DESC, id DESC")
    fun buscarPorTexto(texto: String): Flow<List<RegistroQr>> 

    @Query("SELECT * FROM registros_qr WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): RegistroQr?

    @Update suspend fun actualizar(registro: RegistroQr): Int 
    @Delete suspend fun eliminar(registro: RegistroQr): Int 
}
