package cl.duoc.rinconsur.repository.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.rinconsur.repository.models.*

@Database(entities = [Cabina::class, Turno::class, Alerta::class, Configuracion::class, RegistroQr::class, Usuario::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ProbadoresDao
    abstract fun registroQrDao(): RegistroQrDao
    abstract fun usuarioDao(): UsuarioDao
    companion object {
        private fun crearUsuarioDemo(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT OR IGNORE INTO usuarios (usuario, password, nombre) VALUES ('trabajador', '1234', 'Trabajador')")
        }
        
        private val DATOS_INICIALES = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) { crearUsuarioDemo(db) }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS usuarios (usuario TEXT NOT NULL PRIMARY KEY, password TEXT NOT NULL, nombre TEXT NOT NULL)")
                crearUsuarioDemo(db)
            }
        }
         
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `registros_qr` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `centroComercial` TEXT NOT NULL,
                    `tienda` TEXT NOT NULL,
                    `seccion` TEXT NOT NULL,
                    `sucursal` TEXT NOT NULL,
                    `fechaEscaneo` INTEGER NOT NULL,
                    `latitud` REAL,
                    `longitud` REAL
                )""".trimIndent())
            }
        }

        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,
                "rincon_sur_demo.db").addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(DATOS_INICIALES).build().also { instance = it }
        }
    }
}

