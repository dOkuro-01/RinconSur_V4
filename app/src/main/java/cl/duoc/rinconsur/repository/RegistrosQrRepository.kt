package cl.duoc.rinconsur.repository

import cl.duoc.rinconsur.repository.data.RegistroQrDao
import cl.duoc.rinconsur.viewmodels.Validaciones
import cl.duoc.rinconsur.repository.models.DatosQr
import cl.duoc.rinconsur.repository.models.Coordenadas
import cl.duoc.rinconsur.repository.models.RegistroQr

 
class RegistrosQrRepository(private val dao: RegistroQrDao) {
    val todos = dao.observarTodos()

    fun buscar(texto: String) = dao.buscarPorTexto(texto)

    suspend fun crear(datos: DatosQr, fecha: Long, ubicacion: Coordenadas?): Long {
        require(fecha > 0) { "Fecha inválida." }
        return dao.insertar(RegistroQr(
            centroComercial = datos.centroComercial,
            tienda = datos.tienda,
            seccion = datos.seccion,
            sucursal = datos.sucursal,
            fechaEscaneo = fecha,
            latitud = ubicacion?.latitud,
            longitud = ubicacion?.longitud
        ))
    }

    suspend fun actualizarSeccion(id: Long, nuevaSeccion: String): Boolean {
        val seccionLimpia = nuevaSeccion.trim()
        require(Validaciones.seccionValida(seccionLimpia)) { "La sección debe tener entre 2 y 60 caracteres." }
        val registro = dao.buscarPorId(id) ?: return false
        return dao.actualizar(registro.copy(seccion = seccionLimpia)) > 0
    }

    suspend fun eliminar(id: Long): Boolean {
        val registro = dao.buscarPorId(id) ?: return false
        return dao.eliminar(registro) > 0
    }
}
