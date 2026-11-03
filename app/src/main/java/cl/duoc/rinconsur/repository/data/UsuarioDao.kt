package cl.duoc.rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Query
import cl.duoc.rinconsur.repository.models.Usuario

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE usuario = :usuario AND password = :password LIMIT 1")
    suspend fun login(usuario: String, password: String): Usuario?
}
