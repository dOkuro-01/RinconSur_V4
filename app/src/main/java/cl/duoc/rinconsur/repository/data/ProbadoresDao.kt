package cl.duoc.rinconsur.repository.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import cl.duoc.rinconsur.repository.models.*

@Dao
interface ProbadoresDao {
    @Query("SELECT * FROM cabinas ORDER BY sucursal, numero") fun cabinasFlow(): Flow<List<Cabina>>
    @Query("SELECT * FROM turnos ORDER BY solicita, id") fun turnosFlow(): Flow<List<Turno>>
    @Query("SELECT * FROM alertas ORDER BY creada DESC, id DESC") fun alertasFlow(): Flow<List<Alerta>>
    @Query("SELECT * FROM configuraciones ORDER BY sucursal") fun configuracionesFlow(): Flow<List<Configuracion>>
    @Query("SELECT * FROM cabinas WHERE sucursal = :sucursal ORDER BY numero") suspend fun cabinas(sucursal: String): List<Cabina>
    @Query("SELECT * FROM turnos WHERE sucursal = :sucursal ORDER BY solicita, id") suspend fun turnos(sucursal: String): List<Turno>
    @Query("SELECT * FROM turnos WHERE id = :id LIMIT 1") suspend fun turno(id: Long): Turno?
    @Query("SELECT * FROM alertas WHERE turnoId = :turnoId AND tipo = :tipo LIMIT 1") suspend fun alertaPorTurno(turnoId: Long, tipo: String): Alerta?
    @Query("SELECT * FROM configuraciones WHERE sucursal = :sucursal LIMIT 1") suspend fun config(sucursal: String): Configuracion?
    @Query("SELECT * FROM alertas WHERE resuelta = 0") suspend fun alertasNoResueltas(): List<Alerta>
    @Query("SELECT COUNT(*) FROM cabinas") suspend fun cuentaCabinas(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun guardarCabina(cabina: Cabina)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun guardarConfiguracion(config: Configuracion)
    @Insert suspend fun crearTurno(turno: Turno): Long
    @Update suspend fun actualizarTurno(turno: Turno)
    @Insert suspend fun crearAlerta(alerta: Alerta): Long
    @Update suspend fun actualizarAlerta(alerta: Alerta)
    @Query("UPDATE alertas SET resuelta = 1 WHERE sucursal = :sucursal AND cabina = :cabina AND resuelta = 0")
    suspend fun resolverAlertasCabina(sucursal: String, cabina: Int)
    @Query("DELETE FROM cabinas") suspend fun borrarCabinas()
    @Query("DELETE FROM turnos") suspend fun borrarTurnos()
    @Query("DELETE FROM alertas") suspend fun borrarAlertas()
    @Query("DELETE FROM configuraciones") suspend fun borrarConfiguraciones()
}

