package cl.duoc.rinconsur.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch




private const val URL_API = "http://10.0.2.2:8080/"
private const val ACTIVAR_ENVIO = true

data class EventoApi(val tipo: String, val detalle: String)
data class SaludApi(val estado: String, val servicio: String)

interface ApiServicio {
    @GET("api/health") suspend fun salud(): SaludApi
    @POST("api/eventos") suspend fun guardarEvento(@Body evento: EventoApi): EventoApi
}

object RegistroApi {
    private val api: ApiServicio by lazy {
        Retrofit.Builder().baseUrl(URL_API).addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiServicio::class.java)
    }
    fun anotar(tipo: String, detalle: String) {
        if (!ACTIVAR_ENVIO) return
        CoroutineScope(Dispatchers.IO).launch {
            try { api.guardarEvento(EventoApi(tipo, detalle)) } catch (_: Exception) {   }
        }
    }
    suspend fun probarConexion(): String = try {
        val datos = api.salud()
        "API conectada: ${datos.servicio} (${datos.estado})"
    } catch (e: Exception) {
        "Sin conexión con API. El modo local sigue funcionando."
    }
}
