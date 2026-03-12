package com.example.rest.data

/**
 * Modelo de datos para guardar la información de cada tema: noticias, datos y frases motivacionales.
 */
data class TemaContenido(
    val noticias: List<String>,
    val datos: List<String>,
    val motivacion: List<String>
)

/**
 * Modelo de datos dummy para las noticias/artículos recomendados que consume la UI.
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

    // Base de datos estática
    private val baseDatosTemas: Map<String, TemaContenido> = mapOf(
        "deporte" to TemaContenido(
            noticias = listOf(
                "Calendario Juegos Olímpicos Invierno Milano Cortina 2026 publicado",
                "Colombia confirma 25 jugadores para Mundial FIFA 2026",
                "Equipo Bogotá inicia pretemporada ciclismo y atletismo 2026",
                "Millonarios vs Nacional final liga colombiana marzo 2026",
                "Daniel Arribas regresa ciclismo tras caída Vuelta Algarve",
                "Miami Heat rompe récord Kobe Bryant triple-dobles temporada",
                "Campeonatos Nacionales Ciclismo Ruta Zipaquirá 12-15 marzo",
                "Colombia vence Marruecos 3-1 Copa Davis qualifiers",
                "Primer colombiano clasifica esquí alpino Olímpicos Invierno",
                "48 torneos mundiales programados primer semestre 2026",
                "Selección Colombia femenina gana Sudamericano sub-20",
                "Vuelta a Colombia 2026 ruta oficial anunciada",
                "Atalanta contrata delantero colombiano por 18M euros",
                "Nairo Quintana anuncia regreso profesional Tour Colombia",
                "Bogotá Marathon 2026 registra récord 25K corredores"
            ),
            datos = listOf(
                "Primer gol Mundial: Lucien Laurent 19s Uruguay-Francia 1930",
                "Stephen Curry: MVP unánime 131/131 votos 2016",
                "Alan Shepard jugó golf en la Luna Apollo 14 1971",
                "Ajedrez: único deporte sin límite edad COI",
                "Tenis: Isner-Mahut 11h5m Wimbledon 2010 183 juegos",
                "Primeras Olimpiadas: 776AC solo hombres desnudos",
                "Natación: 2.5B personas practican mundialmente",
                "Gol más rápido final Mundial: Zidane 7min Portugal 2006",
                "Usain Bolt: 100m 9.58s récord mundial 2009",
                "Manute Bol: 3837 bloqueos vs 1597 canastas NBA",
                "Barry Bonds: 762 home runs récord MLB",
                "NFL: Patriots vs Dolphins 61 puntos diferencia 82-3",
                "Ping-pong diplomacia: Nixon-China 1971",
                "Maratón Grecia: 42.195km muerte Feidípides",
                "Boxeo: Canelo 62-2-2 récord invicto 12 divisiones"
            ),
            motivacion = listOf(
                "Cada gota sudor deportivo construye campeones",
                "Dolor temporal, orgullo deportivo permanente",
                "Disciplina deportiva vence talento inconsistente",
                "No esperes condiciones perfectas, crea tu campo",
                "Fracaso deportivo: información para ganar mañana",
                "Mentalidad campeona empieza entrenamiento mental",
                "Rutina diaria separa buenos de extraordinarios",
                "Compite contigo mismo, otros son distracción",
                "Lesiones enseñan apreciar cada día entrenando",
                "Equipo gana partidos, cultura gana campeonatos",
                "Paciencia deportiva: plantar hoy, cosechar años",
                "Nunca demasiado tarde empezar transformar cuerpo",
                "Visualiza medalla antes entrenar cada mañana",
                "Esfuerzo silencioso produce resultados ruidosos",
                "Campeones entrenan hábitos, no solo eventos"
            )
        ),
        "música" to TemaContenido( // Se usa "música" con tilde también para la UI ('música', 'musica')
            noticias = listOf(
                "Taylor Swift anuncia 15 shows Latinoamérica 2026",
                "Ultra Music Festival Bogotá edición 10 años",
                "Bad Bunny supera 10B streams Spotify 2026",
                "Coldplay confirma Estadio Atanasio Girardot junio",
                "Karol G estrena colaboración Rosalía 'X Si'",
                "Premios Heat 2026 Bogotá 20 categorías nuevas",
                "IA Suno genera #1 Billboard primera vez",
                "Rock al Parque 2026 line-up 80% confirmado",
                "Vinilos venden +CDs primera vez 25 años",
                "K-pop BLACKPINK Colombia Movistar Arena",
                "Orquesta Filarmónica Bogotá gira Carnegie Hall",
                "Shakira regresa Formula 1 Miami Super Bowl",
                "Apple Music supera Spotify Latinoamérica usuarios",
                "Conciertos metaverso venden 2M boletos 2026",
                "J Balvin anuncia retiro temporal música 2027"
            ),
            datos = listOf(
                "Canción más cara: 'My Way' Sinatra 4M regalías",
                "Beethoven escribió 9 sinfonías completamente sordo",
                "Primer concierto rock: Alan Freed 1952 Cleveland",
                "Mozart: 626 obras compuestas en 35 años vida",
                "Guitarra eléctrica inventada 1931 Les Paul",
                "Thriller MJ: 70M copias disco más vendido",
                "Primera transmisión radio musical: 1906 Massachusetts",
                "Sinfonía más corta mundo: 1′21″ John Cage",
                "Bob Dylan rechazado 38 sellos antes éxito",
                "MP3 inventado 1993 Fraunhofer Institute Alemania",
                "Concierto más grande: Rod Stewart Copacabana 3.5M",
                "Piano creado 1700 Bartolomeo Cristofori Italia",
                "Ópera más larga: 'Light in the Piazza' 17h",
                "Primer sampler electrónico: 1965 EMS VCS3",
                "Beatles grabaron 213 canciones oficiales"
            ),
            motivacion = listOf(
                "Tu música interior merece escucharse mundialmente",
                "Cada nota desafinada lleva melodía perfecta",
                "Silencios entre notas crean sinfonías eternas",
                "Música transforma dolor en melodías inmortales",
                "Toca corazón, mundo bailará tu ritmo",
                "Armonía vida encontrada primera nota correcta",
                "Melodías olvidadas regresan recuerdos más fuertes",
                "Tu voz única melodía mundo necesitaba",
                "Ritmo corazón nunca miente, síguelo siempre",
                "Canciones alma perduran más cuerpos físicos",
                "Componer vida requiere misma pasión música",
                "Notas discordantes crean acordes más bellos",
                "Música verdadero lenguaje universal humanidad",
                "Cada concierto vida, público merece mejor tú",
                "Herencia musical supera cualquier fortuna material"
            )
        ),
        "arte" to TemaContenido(
            noticias = listOf(
                "Museo Botero Bogotá exhibe 50 obras Picasso",
                "Street Art Festival Medellín transforma 15 barrios",
                "IA Midjourney vende obra 1.2M USD Sotheby",
                "Bienal de Arte Bogotá 2026 92 países",
                "Graffiti Fernando Botero autorizado Chapinero",
                "Galería Mamm Bogotá expone Basquiat inédito",
                "NFT arte latinoamericano supera 500M ventas",
                "Carlos Saúl mural 25m Bogotá centro",
                "Fotografía Colombiana MoMA Nueva York 2026",
                "Arte indígena Amazonía tendencia Christie 8M",
                "David Manzur 95 años retrospectiva MAMBO",
                "Débora Arango dibujos eróticos subasta récord",
                "Beatriz González instala escultura pública 20m",
                "Luis Caballero pinturas MAMBO celebra centenario",
                "Arte cinético Alexandre Pollones Bogotá 2026"
            ),
            datos = listOf(
                "Mona Lisa: sonrisa asimétrica 30% más izquierda",
                "Van Gogh vendió SOLO 'Los Girasoles' vida",
                "Guernica Picasso: 3.5m x 7.8m 27 pies",
                "David Miguel Ángel: 5.17m 17 años tallar",
                "Noche Estrellada: 100 movimientos pincel minuto",
                "Última Cena: perspectiva desaparece 15 años",
                "Pintura rupestre: 40K años Chauvet Francia",
                "Escultura más grande: Cristo Redentor 38m",
                "Picasso dibujó 50K obras vida entera",
                "Mural Diego Rivera: 1.6K m² Detroit",
                "Salvador Dalí derretir relojes subconsciente",
                "Frida Kahlo: 143 pinturas, 55 autorretratos",
                "Banksy: 'Girl Balloon' autodestruyó 50% subasta",
                "Andy Warhol: 100 obras Campbell's Soup",
                "Claude Monet: 250 lienzos nenúfares"
            ),
            motivacion = listOf(
                "Cada trazo imperfecto construye obra maestra",
                "Arte verdadero nace incomodidad creativa",
                "Lienzo vacío invita posibilidades infinitas",
                "Colores alma expresan palabras no dichas",
                "Musa interior siempre lista colaborar contigo",
                "Pinceladas caos organizan universo personal",
                "Galería mejor: imaginación mente despierta",
                "Esculturas vida talladas decisiones diarias",
                "Paleta colores limitada, visión ilimitada",
                "Cuadro terminado nunca, artista evoluciona",
                "Sombras definen mejor luces brillantes",
                "Líneas rectas aburren, curvas inspiran",
                "Óleo seco permite nuevos comienzos siempre",
                "Composición perfecta nace experimentación libre",
                "Patrocinador mejor arte: curiosidad incansable"
            )
        ),
        "tecnología" to TemaContenido(
            noticias = listOf(
                "Apple Vision Pro 2 lanza Colombia abril 2026",
                "GroqChip procesa 2K tokens/seg IA local",
                "Starlink cobertura 100% Colombia 500K usuarios",
                "Quantum Bogotá: 50 qubits latinoamericanos",
                "Tesla Cybercab autónomo Bogotá-Cali 2h",
                "6G Corea del Sur 10TB/s velocidad récord",
                "FoldMate: celular plegable 20 pulgadas",
                "Neuralink implanta 10K pacientes Colombia",
                "OnePlus 14: carga 0-100% 8 minutos",
                "Rabbit R2 vende 1M unidades primer día",
                "Figure 02 robot humanoide fábricas Bogotá",
                "Apple Intelligence iOS 20 Colombia español",
                "Samsung pantalla microLED 200 pulgadas",
                "xAI Memphis Supercluster 100K H100 GPUs",
                "Nothing Phone 3: Glyph definitivo 120Hz"
            ),
            datos = listOf(
                "1KB=1024 bytes, 1MB=1024KB, 1TB=1024GB",
                "Primer email 1971: 'QWERTYUIOP' Ray Tomlinson",
                "Internet pesa 50g si materializara datos",
                "Google procesa 8.5B búsquedas DIARIAS",
                "1ms ping Asia-Europa fibra óptica récord",
                "Bitcoin minado: 19.7M/21M bloques 94%",
                "IPv6 direcciones: 340 undecillones direcciones",
                "Transistor 1947 Bell Labs 3 personas",
                "2.1K satélites Starlink órbita marzo 2026",
                "ChatGPT usuarios: 300M mensuales activos",
                "5nm proceso: 100M transistores/mm² Apple",
                "WiFi 7: 46Gbps velocidad teórica máxima",
                "SSD más rápido: 14K MB/s secuencial",
                "CPU 128 núcleos AMD Threadripper 8000",
                "Quantum supremacy Google: 200s tarea 10K años"
            ),
            motivacion = listOf(
                "Código imperfecto corriendo cambia industria",
                "Byte datos transforma industrias enteras",
                "Algoritmo mejor: curiosidad + iteración",
                "Compilar errores construye software legendario",
                "1 línea código mueve millones usuarios",
                "Hardware limita, imaginación programa infinito",
                "Debugging: arte convertir caos orden",
                "API abierta inspira ecosistema innovación",
                "Cache miss enseña importancia paciencia",
                "Versionado Git: historia evolución digital",
                "Cloud computing: libertad arquitectura infinita",
                "Machine learning: enseñar máquinas pensar",
                "Blockchain: confianza matemática reemplaza fe",
                "Responsive design: arte servir todos dispositivos",
                "Open source: dar recibe multiplicado"
            )
        ),
        "negocios" to TemaContenido(
            noticias = listOf(
                "Nubank Colombia 5M clientes primer año",
                "Rappi valuation 8B USD ronda Series J",
                "MercadoLibre abre 3 centros distribución Bogotá",
                "Éxito Group adquiere Carulla 100% control",
                "Startup colombiana AI fintech levanta 50M USD",
                "Bancolombia lanza wallet cripto stablecoin",
                "Falabella entra darkstores Bogotá 24/7",
                "Grupo Nutresa vende 8 plantas optimización",
                "Cemex Colombia cemento verde 0 emisiones",
                "Startup Proptech Bogotá levanta 25M USD",
                "Grupo Aval entra mercado seguros salud",
                "Tiendanube Colombia 200K tiendas activas",
                "Claro Shop e-commerce 1B ventas 2025",
                "Startup AgroTech Boyacá 100M inversión",
                "Corficolombiana lanza fondo inmobiliario 300M"
            ),
            datos = listOf(
                "80% startups fracasan 20% sobreviven 5 años",
                "Amazon perdió 8K millones primer año",
                "Walmart: 10.5K tiendas 24 países",
                "Coca-Cola: 200 países 1.9B servings/día",
                "Apple market cap: 3.4T USD marzo 2026",
                "1% tiempo empleados genera 50% resultados",
                "Pareto 80/20 aplica 90% negocios",
                "Customer Acquisition Cost promedio 200-300USD",
                "LTV cliente promedio SaaS: 1000USD",
                "Churn rate saludable: <5% mensual",
                "MRR crecimiento 20% mensual escala",
                "CAC payback <12 meses saludable",
                "Gross margin SaaS: 70-80% objetivo",
                "1 cliente feliz = 3 referrals gratis",
                "98% compañías Fortune 500 usan CRM"
            ),
            motivacion = listOf(
                "Rechazo puerta nueva oportunidad negocio",
                "Flujo caja rey, ganancias van segundo",
                "Cliente NO interrumpido vale millones",
                "Pivotear rápido salva compañías lentas",
                "Networking: dar primero, recibir después",
                "Falla rápido, aprende más, gana antes",
                "Contrato verbal vale MILLONES promesas",
                "Competencia copia, tú inventas mercados",
                "Cashflow positivo: oxígeno empresarial",
                "Cliente leal paga 10x adquisición",
                "No competencia: mercado esperando inventes",
                "Dueños solucionan, empleados esperan",
                "Impuestos optimizados legalmente: inteligencia",
                "Alianzas estratégicas multiplican ingresos",
                "Visión 10 años ejecuta hoy primer paso"
            )
        ),
        "cine" to TemaContenido(
            noticias = listOf(
                "Avatar 3 rompe récords preventa Colombia",
                "Dune Messiah inicia filmación Wadi Rum",
                "Marvel anuncia 8 películas Fase 7 2026",
                "Star Wars Rey película Bogotá locaciones",
                "Oppenheimer 2 desarrollo Christopher Nolan",
                "Bogotá Cine Festival 45 películas latinas",
                "Misión Imposible 9 rueda Colombia",
                "Pixar Inside Out 3 confirmada 2027",
                "DC Batman reboot James Gunn 2026",
                "A24 Hereditary secuela Cannes premiere",
                "Fast XI locaciones Cartagena 3 meses",
                "Festival Cine Cartagena 65 edición",
                "Netflix estrena 12 colombianos 2026",
                "Top Gun 3 Maverick regresa 2027",
                "Festival Cine de La Habana Bogotá 2026"
            ),
            datos = listOf(
                "Citizen Kane: mejor película IMDb todos tiempos",
                "Padrino: 3h22m más larga saga mafia",
                "Titanic: 200M extras extras producción",
                "Avatar: 5 años post-producción Pandora",
                "Star Wars: 9 sagas 45 años universo",
                "Pixar Toy Story: primer full CGI 1995",
                "Oscar mejor película: 3h30m más larga",
                "Cannes Palma de Oro: 1.000m euros",
                "Hollywood: 700 películas/año 500K empleos",
                "CGI: 50% presupuesto moderno blockbusters",
                "IMAX: 10x píxeles pantalla convencional",
                "Dolby Atmos: 128 canales audio",
                "4DX: 21 efectos sensoriales cine",
                "ScreenX: 270° proyección panorámica",
                "Cine mudo: The Kid Charlie Chaplin 1921"
            ),
            motivacion = listOf(
                "Escena eliminada enseña más que éxito",
                "Claqueta marca inicio segunda oportunidad",
                "Foco fuera: arte verdadero vida real",
                "Guion imperfecto filmado cambia industria",
                "Locación perfecta: visión interior director",
                "Dailies malos construyen rough cut genial",
                "Continuidad narrativa vida supera ficción",
                "Color grading alma: post-producción transforma",
                "Foley perfecto: sonido crea universos",
                "Steadicam estable: caminar fe dirección",
                "Montaje transforma caos escenas maestras",
                "ADR salva actuaciones imperfectas magia",
                "VFX construye mundos imposibles pantalla",
                "Colorista pinta emociones pantalla grande",
                "Final cut definitivo: arte entrega público"
            )
        ),
        "salud" to TemaContenido(
            noticias = listOf(
                "Colombia aprueba Ozempic genérico farmacias",
                "Estudio: 8h sueño reduce 40% infartos",
                "Médicos Bogotá detectan virus respiratorio nuevo",
                "Sistema salud Bogotá implanta IA diagnóstico",
                "Caminar 10K pasos/día baja 25% diabetes",
                "Vitamina D inyecciones mensuales Colombia",
                "Programa nacional control estrés laboral",
                "Sleep tech clínicas sueño Bogotá 24h",
                "Estudio mediterráneo: aceite oliva 30% menos cáncer",
                "App salud mental MinSalud 2M descargas",
                "Clínicas odontología láser sin dolor",
                "Terapia génica leucemia 95% éxito niños",
                "Programa nacional hipertensión 5M pacientes",
                "Colchón inteligente respiración Bogotá hospitales",
                "Estudio ayuno intermitente 16:8 baja 12% peso"
            ),
            datos = listOf(
                "Corazón late 100K veces/día 35M/año",
                "85% enfermedades crónicas prevenibles hábitos",
                "Sueño REM: 2h/día consolidar memoria",
                "Meditación 10min/día baja 44% cortisol",
                "Agua: 60% peso cuerpo adulto promedio",
                "Vitamina C: 100mg/día previene resfriados",
                "Caminar 30min/día = 3 años vida extra",
                "Omega-3: 40% menos depresión clínica",
                "Respiración nasal 20% oxígeno más sangre",
                "Postura erguida: 30% menos dolor espalda",
                "Sol 15min/día: 10K UI vitamina D",
                "Fibra 30g/día: 25% menos cáncer colon",
                "Cafeína 400mg/día: óptimo cognitivo físico",
                "Sueño 7-9h: 50% mejor toma decisiones",
                "Hidratación: 2% deshidratación = 20% fatiga"
            ),
            motivacion = listOf(
                "Cada respiración consciente elige vida plena",
                "Cuerpo templo, mantenlo sagrado diariamente",
                "Peso número, salud transformación completa",
                "Dolor temporal construye fortaleza permanente",
                "Nutrición alimenta cuerpo, disciplina alimenta alma",
                "Movimiento medicina más antigua conocida",
                "Descanso activo recupera más que sueño pasivo",
                "Salud mental músculo entrenado diariamente",
                "Hidratación simple, impacto exponencial bienestar",
                "Postura poder: espalda recta confianza alta",
                "Respiración diafragmática desactiva estrés instantáneo",
                "Sueño reparador éxito diurno garantizado",
                "Nutrientes correctos inteligencia celular óptima",
                "Ejercicio endorfinas felicidad química natural",
                "Bienestar integral supera metas estéticas"
            )
        ),
        "videojuegos" to TemaContenido(
            noticias = listOf(
                "GTA VI beta Colombia servers 2M jugadores",
                "Nintendo Switch 2 16GB RAM 120Hz OLED",
                "PlayStation 6 devkits enviados estudios AAA",
                "Fortnite Chapter 7 Colombia map 50% done",
                "Xbox handheld 8 pulgadas 2TB SSD",
                "eSports Colombia liga 10M prize pool",
                "Steam Deck 2 batería 12h 1080p60",
                "Roblox metaverso Bogotá 5M usuarios mensuales",
                "Valorant Champions Tour Colombia 2026",
                "Mobile Legends MPL Colombia 500K viewers",
                "Unity 2026.1 raytracing móvil nativo",
                "Epic Games Store 0% comisión indies",
                "Game Pass 600 juegos día 1 2026",
                "Razer laptop 18\" OLED 0.1ms gaming",
                "Colombia LAN center 100 PC RTX5090"
            ),
            datos = listOf(
                "Gaming industry: 184B USD 2025 > cine+música",
                "Esports viewers: 532M 2023 superbowl x10",
                "Steam concurrentes: 38M récord simultáneo",
                "Mobile gaming: 50% revenue industria total",
                "RTX 5090: 32GB GDDR7 21K CUDA cores",
                "PS5: 65M unidades 4 años vs PS4 6 años",
                "FPS 360Hz diferencia 1ms reacción humana",
                "SSD NVMe Gen5: 14GB/s lectura aleatoria",
                "Mouse 80K DPI 8KHz polling rate",
                "Monitor OLED 0.03ms respuesta píxel",
                "VRAM 24GB 4K ultra RT raytracing",
                "CPU 16 cores 5.8GHz boost gaming",
                "HMD 8Kx8K per eye eye-tracking foveado",
                "Cloud gaming: 20ms latencia global promedio",
                "Controller haptic 1000Hz feedback granular"
            ),
            motivacion = listOf(
                "Respawn enseña resiliencia mejor universidad",
                "Lag prueba paciencia, disconnect oportunidad",
                "Build diferente: estrategias únicas ganan",
                "Headshot perfecto: práctica 10K horas recompensada",
                "Teamwipe: confianza colectiva supera skill individual",
                "Frame drop: oportunidad crear espacio ganar",
                "New meta: adaptarse prosperar siempre",
                "Clutch 1v5: momentos definen leyendas",
                "Patch notes: cambio obligatorio evolución",
                "Rank up: grind silencioso grita resultados",
                "Controller drift: superar limitaciones hardware",
                "Ping 300ms: estrategia supera velocidad cruda",
                "New season reset: segunda oportunidad reiniciar",
                "Pro player retiro: legado vive replays eternos",
                "LAN victory: validar grind años segundos"
            )
        ),
        "ciencia" to TemaContenido(
            noticias = listOf(
                "James Webb detecta exoplaneta habitable 12 años luz",
                "Fusión nuclear ITER primera plasma confinada",
                "CRISPR cura diabetes tipo 1 ratones 100%",
                "Gravedad cuántica teoría unificada 12 dimensiones",
                "Telescopio SKA detecta 100 señales SETI",
                "Quantum internet China-Europa 500km estable",
                "Hidrógeno metálico sintetizado 5M atm lab",
                "IA AlphaFold3 predice 99% estructuras proteína",
                "Partícula X17 quinta fuerza evidencia 4 sigma",
                "Terraformación Marte plan NASA 50 años",
                "Teletransportación cuántica gato 2kg Corea",
                "Batería nuclear diamante 28 años carga única",
                "Editor génico prime editing 97% precisión",
                "Antimateria propulsión 20% velocidad luz",
                "Biocomputadora ADN 1 exabyte/cm³"
            ),
            datos = listOf(
                "Velocidad luz: 299.792.458 m/s vacío",
                "Universo observable: 93B años luz diámetro",
                "Átomos cuerpo: 7 octillones 7 segundos",
                "Agua Tierra: 1.386B km³ 71% superficie",
                "DNA longitud: 2m célula estirado completo",
                "Neutrinos: 330B/cm²/segundo atraviesan Tierra",
                "Edad universo: 13.8B años Big Bang",
                "Materia oscura: 27% masa-energía universo",
                "Energía oscura: 68% aceleración expansión",
                "Protón masa: 1.6726x10^-27 kg",
                "Constante Planck: 6.626x10^-34 Js",
                "Número Avogadro: 6.022x10^23 partículas/mol",
                "Velocidad sonido: 343m/s 20°C aire",
                "Entropía universo: aumenta 10^103 kB/s",
                "Constante Hubble: 74.03 km/s/Mpc"
            ),
            motivacion = listOf(
                "Teoría incompleta invita nueva generación física",
                "Experimento fallido descarta hipótesis, avanza ciencia",
                "Frontera conocimiento: donde curiosidad encuentra verdad",
                "Equación simple explica complejidad infinita",
                "Hipótesis rechazada planta semilla Nobel mañana",
                "Datos contradictorios mejor regalo investigación",
                "Variable desconocida solución disfrazada problema",
                "Laboratorio pequeño cambia comprensión universo",
                "Paper rejected 12 journals premiado después",
                "Observación inesperada inicia revolución paradigma",
                "Cálculo manual tedioso, descubrimiento eterno",
                "Modelo imperfecto predice mejor que intuición",
                "Error experimental enseña más que teoría perfecta",
                "Conferencia incómodo planta semilla colaboración",
                "Grant rechazado fuerza creatividad metodología"
            )
        ),
        "literatura" to TemaContenido(
            noticias = listOf(
                "García Márquez memorias póstumas Sotheby 2.4M",
                "Premio Alfaguara 2026 novela colombiana indígena",
                "Harry Potter encuadernación oro 50K USD",
                "Biblioteca Nacional Bogotá digitaliza 2M páginas",
                "Saga nueva fantasía latina Netflix rights 5M",
                "Récord Guiness poema más largo 1.2M versos",
                "FilBA Buenos Aires Colombia invitado honor",
                "Manuskript Dante Comedia subasta 8M euros",
                "Bogotá 50° Feria Libro 2.1M asistentes",
                "IA escribe bestseller thriller Amazon #3",
                "Cervantes 2026 poeta colombiano primera vez",
                "Archivo Borges 1K cartas inéditas",
                "Libro más caro: Birds of America 12M USD",
                "Podcast audiolibros supera ventas físicas",
                "Premio Nobel Literatura rumor latino 2026"
            ),
            datos = listOf(
                "Don Quijote: primer novela moderna 1605",
                "Shakespeare: 884K palabras 37 obras",
                "Biblioteca Alejandría: 700K rollos pergamino",
                "Gutenberg: 180 copias Biblia 1455",
                "Papel China: 105 d.C. Cai Lun",
                "Diccionario Oxford: 600K palabras definidas",
                "Illíada Homero: 15K líneas hexámetros",
                "Cien Años Soledad: 130K palabras 10 años",
                "Wordsworth: 100K líneas poesía vida",
                "Proust: 1.2M palabras Remembrance Things Past",
                "Tolstoy Guerra Paz: 587K palabras",
                "Hugo Les Misérables: 661 páginas manuscrito",
                "Kafka Metamorfosis: rechazada 8 publishers",
                "Joyce Ulysses: 265K palabras 11 años",
                "Biblioteca Congreso USA: 170M ítems"
            ),
            motivacion = listOf(
                "Página en blanco invita historias infinitas",
                "Rechazo editorial planta semilla bestseller",
                "Personaje imperfecto conecta lectores perfectos",
                "Metáfora poderosa comunica verdad silencio",
                "Escribir borrador terrible libera genio interior",
                "Diálogo creíble nace escucha conversaciones reales",
                "Cliffhanger capítulo mantiene lectores cautivos",
                "Show don't tell: acción comunica emoción",
                "Arco personaje: viaje héroe transforma lector",
                "Palabra precisa evoca mil imágenes mentales",
                "Escribir diario planta hábito genio literario",
                "Crítica constructiva afila pluma más que halago",
                "Final inesperado recompensa lectura atenta",
                "Ritmo narrativo: tensión creciente clímax catarsis",
                "Legado literario supera cualquier biografía"
            )
        ),
        "viajes" to TemaContenido(
            noticias = listOf(
                "Avianca 100 rutas nuevas Colombia 2026",
                "Machu Picchu reserva cupos 100% capacidad",
                "Galápagos cruceros eléctricos cero emisiones",
                "Ruta Caribe low-cost 49K COP Bogotá",
                "Antártida turismo colombiano 20% crecimiento",
                "Tayrona peak season 45K visitantes/mes",
                "LATAM Perú-Bogotá directo 4h30m diario",
                "San Andrés sin tiquetes aéreos marzo",
                "Ecuador Galápagos visa electrónica colombianos",
                "Avianca hub El Dorado 95% puntualidad",
                "Cartagena histórica 2M turistas 2026",
                "Panamá hub Américas 250 vuelos diarios",
                "Guatapé récord 1.2M escaladores peñol",
                "Salento eje cafetero 800K visitantes",
                "Eldorado Bogotá aeropuerto 5 estrellas"
            ),
            datos = listOf(
                "Tierra: 510M km² 29% continente 71% agua",
                "8.7M especies estimadas 1.2M catalogadas",
                "Everest: 8.848m crece 4mm/año tectónica",
                "Mariana: 11K m fosa océano Pacífico",
                "Sahara: 9.2M km² caliente desierto",
                "Amazonas: 6.7M km² 20% agua dulce",
                "Volcán Kilauea: erupción continua 35 años",
                "Gran Barrera: 2.3K km visible espacio",
                "Antártida: 14M km² 90% hielo mundo",
                "Niagara: 3.160m³/segundo caudal impresionante",
                "Aconcagua: 6.960m techo Américas",
                "Salar Uyuni: 10K km² espejo perfecto",
                "Angel Falls: 979m caída agua más alta",
                "Delta Okavango: 15K km² interior desértico",
                "Lago Baikal: 25% agua dulce no congelada"
            ),
            motivacion = listOf(
                "Pasaporte páginas: capítulos vida sin editar",
                "Jet lag temporal, recuerdos eternos",
                "Maleta ligera, corazón experiencias llenas",
                "Ruta desconocida lleva mejores historias",
                "Idioma extraño conecta almas universales",
                "Comida local: sabor auténtico cultura",
                "Foto perfecta: sonrisa local genuina",
                "Mapa digital falla, brújula interna guía",
                "Overbooking: oportunidad destino inesperado",
                "Pierna cansada, alma rejuvenecida viajes",
                "Souvenir mejor: conversación taxi nocturno",
                "Vuelo delay: tiempo leer destino nuevo",
                "Callejón sin salida lleva mirador perfecto",
                "Moneda local: puente confianza inmediata",
                "Regreso hogar: contador historias llenas"
            )
        ),
        "gastronomía" to TemaContenido(
            noticias = listOf(
                "Bogotá 12 restaurantes 1 estrella Michelin 2026",
                "Ajiaco declarado patrimonio UNESCO intangible",
                "Leo Espinosa Central #4 mundo 50Best",
                "Ceviche peruano vs colombiano Cartagena festival",
                "Andrés Carne de Res abre Miami design district",
                "Queso paisa Ocala Florida DOP protegido",
                "Harry Sasson anuncia restaurante Tokio 2026",
                "Bandeja paisa calorías oficiales 1800 kcal",
                "Festival sancocho 8 regiones Boyacá 2M asistentes",
                "Chocolate Colombia 72% cacao mejor mundo",
                "Leo Cocinas Bogotá reserva 6 meses",
                "Cocina indígena whenua Amazonía tendencia",
                "Café Colombia subasta 1200 USD/lb récord",
                "Mistura Perú Bogotá edición colombiana",
                "Pescado frito Cartagena 1er lugar street food"
            ),
            datos = listOf(
                "Ajiaco: papa criolla, pastusa, común 3 tipos",
                "Bandeja: 1200-1800 cal/plato tradicional",
                "Arepa: maíz pilado prehispánico Venezuela",
                "Sancocho: 7 carnes 7 regiones Colombia",
                "Lulo: 48mg vitamina C/100g más naranja",
                "Guayaba: 5x vitamina C naranjas",
                "Changua: desayuno prehispánico Muiscas",
                "Bocadillo: 80% azúcar natural panela",
                "Chorizo santarrosano: tripa cerdo natural",
                "Café Colombia: 95% arábica mundo 12%",
                "Panela: 100% jugo caña puro sin químicos",
                "Yuca: 400K toneladas producción Colombia",
                "Queso campesino: 28 días maduración mínima",
                "Patacón: plátano verde frito 2 veces",
                "Mazorca: 8K años domesticación México"
            ),
            motivacion = listOf(
                "Receta imperfecta deleita comensales perfectos",
                "Especia única transforma plato cotidiano obra",
                "Degustación lenta aprecia capas sabor complejas",
                "Ingrediente local conecta mesa con tierra",
                "Presentación bella inicia experiencia gastronómica",
                "Maridaje correcto multiplica placer multiplicador",
                "Técnica clásica reinventada genera tendencia",
                "Degustar consciente aprecia trabajo agricultor",
                "Sazón personal: firma única cada cocinero",
                "Textura perfecta transforma comida experiencia",
                "Vinagre reducción: magia líquida platos",
                "Emplatado artístico eleva comida arte comestible",
                "Descanso fermentación crea complejidad infinita",
                "Fuego controlado carameliza azúcares naturales",
                "Herencia culinaria supera cualquier receta escrita"
            )
        ),
        "moda" to TemaContenido(
            noticias = listOf(
                "Mercedes Almango colección PFW París 2026",
                "Johanna Ortiz NYFW Mercedes Benz Semana Colombia",
                "Silvia Tcherassi Capri Italia pop-up store",
                "Andrés Pajón LVMH Prize finalista latino",
                "Natasha Frank grafiti fashion MFW Milán",
                "Parejo colombiana tendencia Coachella 2026",
                "Loewe colabora artesanos Kogi Sierra Nevada",
                "Camila Ortíz leather free PETA aprobado",
                "Bogotá Fashion Week 45 marcas sostenibles",
                "Aguaje colección inspiración Vaupés Amazonía",
                "Sneakers colombianas StockX 15K reventa",
                "Moda Waorani Paris prêt-à-porter 2026",
                "Textiles Ikat Nasa Yuwe NY tienda",
                "J Balvin streetwear colab Nike 6M pares",
                "Moda precolombina Met Gala tema 2026"
            ),
            datos = listOf(
                "Jeans: 7M pares/día Levi's mundial",
                "Camiseta algodón: 2.6K litros agua producir",
                "Zara: 12K diseños/año 2 semanas ciclo",
                "H&M: 3K tiendas 75 países empleados",
                "Gucci: 13B euros revenue 2025 récord",
                "Sneaker más cara: 8M USD Moon Shoe",
                "Fast fashion: 60% prendas <10 usos",
                "Algodón orgánico: 1% producción mundial",
                "Piel vegana: piña, cactus, manzana",
                "Talla 38 europea = 6 USA mujeres",
                "Denim: 100K km hilo jeans Levi's",
                "Suit Savile Row: 60h trabajo 5K USD",
                "Cachemir: 1 camello produce 150g/año",
                "Seda: 3K orugas gusano 1 bufanda",
                "Indigo: planta índigofera tinctoria natural"
            ),
            motivacion = listOf(
                "Estilo personal supera tendencias pasajeras",
                "Prenda segunda vida genera estilo único",
                "Accesorio pequeño transforma outfit completo",
                "Confianza talla única universal todas tallas",
                "Tendencia copia, estilo personal inventa",
                "Guardarropa capsula: 30 prendas 100 outfits",
                "Detalles cuidadosos crean apariencia impecable",
                "Prenda incómoda nunca moda verdadera",
                "Color inesperado genera estilo memorable",
                "Estilo auténtico atrae personas correctas",
                "Vintage moderno: historia combinada presente",
                "Accesorios conversan antes que ropa hable",
                "Silueta perfecta nace proporción estudiada",
                "Estilo uniforme: simplicidad sofisticada máxima",
                "Moda circular: estilo eterno reinventado"
            )
        ),
        "fotografía" to TemaContenido( // Incluye variante sin tilde por defecto en la capa de presentación
            noticias = listOf(
                "Sony α1 II 150MP global shutter 2026",
                "Canon R5 Mark III 8K60 RAW interno",
                "Nikon Z9H 1DX mirrorless 120fps",
                "Fotógrafo colombiano National Geographic cover",
                "Leica Q4 60MP sumilux 28mm f1.2",
                "Festival fotografía Cartagena 45K asistentes",
                "Hasselblad X3D 150MP medio formato",
                "Fotolibro Sebastião Salgado Amazonía 2M copias",
                "Phase One XF 200MP 16-bit color",
                "Fotografía Colombia MoMA touring exhibition",
                "Fujifilm GFX 200S 102MP estabilización",
                "Fotógrafo paisa drone 16K Salar Uyuni",
                "SIGMA fp L 61MP full-frame cinema",
                "Panasonic Lumix S1H6 6K open gate",
                "Fotografía nocturna Bogotá Atacama Chile"
            ),
            datos = listOf(
                "f/1.4: 50% luz f/2 4 veces menos",
                "ISO doble: grano duplica señal amplificada",
                "1/60s regla mano libre mínima velocidad",
                "HiperFocal: DoF infinito desde sujeto",
                "Exposición perfecta: 18% gris medio",
                "Profundidad Campo: f-stop distancia sujeto",
                "Regla tercios: 4x3 grid composición clásica",
                "Luz dorada: 30min amanecer/atardecer óptima",
                "Polarizador: 1-2 stops luz reduce reflejos",
                "ND64: 6 stops luz 4min exposiciones",
                "16-35mm: ojo humano equivalente angular",
                "85mm f1.4: retrato perfecto perspectiva",
                "50mm f1.8: nifty fifty 300 USD fullframe",
                "14 bits RAW: 16K tonos transición",
                "Enfoque peaking: precisión manual 100%"
            ),
            motivacion = listOf(
                "Fotograma fallido enseña composición perfecta",
                "Luz disponible crea imágenes imposibles estudio",
                "Objetivo kit captura emociones eternas",
                "ISO alto recupera sombras imposibles",
                "Enfoque manual selecciona sujeto emocional",
                "Exposición bracketing salva highlights difíciles",
                "Composición regla tercios guía ojo espectador",
                "Blanco negro elimina distracciones colores",
                "Lente prime fuerza creatividad composición",
                "Luz dura crea sombras dramáticas inolvidables",
                "Profundidad campo aísla sujeto emocional",
                "Exposición larga congela movimiento magia",
                "Reflejo accidental crea simetría perfecta",
                "Post-procesado sutil eleva imagen técnica",
                "Momentos fugaces: timing fotográfico perfecto"
            )
        ),
        "historia" to TemaContenido(
            noticias = listOf(
                "Naufragio galeón San José 5.5B oro recuperado",
                "Tumba Muisca chieftain intacta Guatavita",
                "Ciudad perdida Amazonía LIDAR 6K hectáreas",
                "Manuscritos Mar Muerto digital 100% accesibles",
                "Pirámide Teotihuacán cámara secreta robots",
                "Escritura lineal A descifrada 70% MIT",
                "Tutankamón 3K objetos Cairo museum 2026",
                "Piedra Rosetta IA traduce 12 lenguas muertas",
                "Batalla Waterloo 360° VR experiencia inmersiva",
                "Pergaminos Herculano volcán Vesubio legibles",
                "Genghis Khan ADN 16M descendientes varones",
                "Muro Adriano drones 3D reconstrucción completa",
                "Cueva Altamira réplica 99% pintura 14K años",
                "Alexandria biblioteca 2M papiros Palermo",
                "Troia excavación Schliemann capa VIIa Príamo"
            ),
            datos = listOf(
                "Cleopatra: VII dinastía, griega macedonia",
                "Guerras Púnicas: 1.2M muertos 120 años",
                "Cruzadas: 1-3M muertos 200 años",
                "Peste Negra: 75-200M Europa 50% población",
                "Inca: 12M personas sin escritura rueda",
                "Aztecas: 5M Tenochtitlán 1500 más Roma",
                "Gran Muralla: 21K km 2K años construir",
                "Coliseo: 50K espectadores 80 a.C.",
                "Partenón: 13.5m Atenea oro marfil",
                "Pirámides Giza: 2.3M bloques 80t cada",
                "Moai Rapa Nui: 900 estatuas 10m altura",
                "Stonehenge: 5K años alineación solsticio",
                "Machu Picchu: 1450 sin rueda metal",
                "Chichén Itzá: 365 escaleras sombra serpiente",
                "Petra Jordania: 2K años tallada roca"
            ),
            motivacion = listOf(
                "Civilización colapsa, lecciones perduran milenios",
                "Batalla perdida inspira generaciones victoriosas",
                "Tratado roto planta semilla imperio nuevo",
                "Descubrimiento accidental cambia historia humanidad",
                "Diario soldado común narra epopeya épica",
                "Mapa incorrecto lleva descubrimiento revolucionario",
                "Líder improbable une tribus rivales eternas",
                "Pergamino olvidado descifra civilización perdida",
                "Moneda antigua cuenta historia comercio global",
                "Arma primitiva vence tecnología superior",
                "Diplomacia silenciosa evita guerra centurias",
                "Traición cortesana construye dinastía milenaria",
                "Relato oral sobrevive escritura bibliotecas",
                "Ruinas hablan más que palacios intactos",
                "Epopeya nacional nace canción taberna"
            )
        )
    )

    /**
     * Extrae el tema principal de un "Tema:subtema" o de un tema limpio.
     * Retorna el tema en minúsculas para coincidir con la base de datos (Ej: 'Deporte:fútbol' -> 'deporte')
     * y remueve tildes en caso de haber problemas de codificación.
     */
    private fun normalizarTema(temaRaw: String): String {
        val principal = temaRaw.substringBefore(":").lowercase()
        return when (principal) {
            "música", "musica" -> "música"
            "tecnología", "tecnologia" -> "tecnología"
            "gastronomía", "gastronomia" -> "gastronomía"
            "fotografía", "fotografia" -> "fotografía"
            else -> principal
        }
    }

    /**
     * Genera una sola frase motivacional aleatoria usando uno de los temas elegidos al azar.
     */
    fun generarFraseMotivacional(temasElegidos: List<String>): String {
        if (temasElegidos.isEmpty()) {
            return "Organiza tu tiempo de la mejor manera con Rest Cycle."
        }
        
        // Seleccionamos un tema al azar, luego buscamos sus datos.
        val temaRaw = temasElegidos.random()
        val temaClave = normalizarTema(temaRaw)
        
        val contenidoPorTema = baseDatosTemas[temaClave]
        
        // Si por alguna razón el tema no está en la base, usamos el default.
        if (contenidoPorTema == null || contenidoPorTema.motivacion.isEmpty()) {
            return "Explora nuevos horizontes enfocándote en lo que te apasiona."
        }
        
        return contenidoPorTema.motivacion.random()
    }

    /**
     * Devuelve una lista de artículos (mezclando noticias y datos)
     * que coinciden con los temas elegidos por el usuario.
     */
    fun obtenerNoticiasParaTemas(temasElegidos: List<String>): List<ArticuloMock> {
        if (temasElegidos.isEmpty()) return emptyList()
        
        // Convertimos las preferencias ("Tema:subtema") a un conjunto de temas únicos
        val categoriasUnicas = temasElegidos.map { normalizarTema(it) }.toSet()
        
        val listaNoticiasDinamicas = mutableListOf<ArticuloMock>()
        var idContador = 1
        
        // Extraemos artículos para cada categoría
        for (categoria in categoriasUnicas) {
            val contenido = baseDatosTemas[categoria] ?: continue
            
            // Agregamos algunas Noticias (Artículos)
            val noticiasAleatorias = contenido.noticias.shuffled().take(2)
            for (noticiaStr in noticiasAleatorias) {
                listaNoticiasDinamicas.add(
                    ArticuloMock(
                        id = idContador++,
                        titulo = noticiaStr,
                        tema = categoria.replaceFirstChar { it.uppercase() },
                        fuente = listOf("Info Global", "The Daily", "Top News").random(),
                        tiempoLectura = listOf("3 min", "5 min", "7 min").random()
                    )
                )
            }
            
            // Agregamos algunos Datos Curiosos
            val datosAleatorios = contenido.datos.shuffled().take(2)
            for (datoStr in datosAleatorios) {
                listaNoticiasDinamicas.add(
                    ArticuloMock(
                        id = idContador++,
                        titulo = datoStr,
                        tema = "Dato: " + categoria.replaceFirstChar { it.uppercase() },
                        fuente = "Curiosidades",
                        tiempoLectura = "1 min"
                    )
                )
            }
        }
        
        // Devolvemos la lista aleatorizada de artículos, para no tener todos los deportes juntos, luego todos los cines...
        return listaNoticiasDinamicas.shuffled()
    }
}
