package com.example.rest.data

import kotlin.random.Random

/**
 * Modelo de datos dummy para las noticias/artículos recomendados
 */
data class ArticuloMock(
    val id: Int,
    val titulo: String,
    val tema: String,
    val fuente: String,
    val tiempoLectura: String
)

/**
 * Generador de contenido simulado (Mock) basado en los temas de interés elegidos por el usuario.
 */
object GeneradorContenidoMock {

    // Plantillas de frases motivacionales donde se reemplazará [TEMA]
    private val plantillasFrases = listOf(
        "Sigue aprendiendo sobre [TEMA], cada día te acerca a tu mejor versión.",
        "No abandones tu pasión por [TEMA], los grandes cambios empiezan con pasos pequeños.",
        "Reserva hoy 10 minutos para disfrutar de [TEMA], tu mente te lo va a agradecer.",
        "El mundo de [TEMA] es fascinante, ¡sigue explorando!",
        "Tu interés por [TEMA] dice mucho de tu curiosidad. ¡Sigue así!"
    )

    // Base de datos local simulada de artículos
    private val baseDeNoticias = listOf(
        ArticuloMock(1, "10 rutinas para empezar a correr", "Deporte", "HealthRunner", "5 min"),
        ArticuloMock(2, "Los beneficios de la calistenia", "Deporte", "FitLife", "7 min"),
        ArticuloMock(3, "La evolución del Rock en los 80s", "Música", "SoundMag", "10 min"),
        ArticuloMock(4, "Aprender a tocar guitarra", "Música", "AcordesYa", "6 min"),
        ArticuloMock(5, "Técnicas de pintura al óleo", "Arte", "CreativeArts", "8 min"),
        ArticuloMock(6, "La Inteligencia Artificial en 2026", "Tecnología", "TechRadar", "12 min"),
        ArticuloMock(7, "Nuevos horizontes en computación cuántica", "Tecnología", "FutureTech", "9 min"),
        ArticuloMock(8, "Cómo invertir en bolsa sin riesgo extremo", "Negocios", "FinanzasHoy", "15 min"),
        ArticuloMock(9, "Reseña: La mejor película del año", "Cine", "MovieTime", "5 min"),
        ArticuloMock(10, "Alimentos que benefician tu memoria", "Salud", "NutriLife", "4 min"),
        ArticuloMock(11, "Lanzamientos Indie más esperados", "Videojuegos", "GamerZone", "6 min")
    )

    /**
     * Genera una sola frase motivacional aleatoria usando uno de los temas elegidos al azar.
     * Si la lista está vacía, devuelve una frase genérica.
     */
    fun generarFraseMotivacional(temasElegidos: List<String>): String {
        if (temasElegidos.isEmpty()) {
            return "Organiza tu tiempo de la mejor manera con Rest Cycle."
        }
        val temaAlAzar = temasElegidos.random()
        val plantillaAlAzar = plantillasFrases.random()
        return plantillaAlAzar.replace("[TEMA]", temaAlAzar)
    }

    /**
     * Devuelve una lista de artículos que coinciden con los temas elegidos por el usuario.
     */
    fun obtenerNoticiasParaTemas(temasElegidos: List<String>): List<ArticuloMock> {
        if (temasElegidos.isEmpty()) return emptyList()
        
        // Filtramos buscando coincidencias exactas (ignorando mayúsculas)
        return baseDeNoticias.filter { articulo ->
            temasElegidos.any { tema -> tema.equals(articulo.tema, ignoreCase = true) }
        }.shuffled() // Los mezclamos un poco para dar variedad
    }
}
