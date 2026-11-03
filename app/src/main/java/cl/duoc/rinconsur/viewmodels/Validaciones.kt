package cl.duoc.rinconsur.viewmodels

 
object Validaciones {
    fun campoObligatorio(valor: String, mensaje: String): String? =
        if (valor.isBlank()) mensaje else null

    fun minutosCierreValidos(valor: String): Boolean = valor.toIntOrNull()?.let { it in 1..600 } == true
    
    fun rutDemo(rut: String): Boolean {
        val limpio = rut.replace(".", "").replace(" ", "").trim()
        return Regex("^[0-9]{7,8}-?[0-9Kk]$").matches(limpio)
    }
    fun seccionValida(seccion: String) = seccion.trim().length in 2..60
}
