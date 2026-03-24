# Política de Privacidad - Rest Cycle

**Última actualización:** 19 de marzo de 2026

## 1. Información que Recopilamos

Rest Cycle recopila varios tipos de información para proporcionar sus funcionalidades de control parental, gestión de tiempo de pantalla y horarios de descanso:

### Información de Cuenta y Perfil

- Correo electrónico (para autenticación y recuperación de cuenta)
- Contraseña (almacenada de forma segura con hash y salt)
- Nombre de usuario
- Foto de perfil (opcional, proporcionada voluntariamente por el usuario)

### Datos de Uso del Dispositivo

- Estadísticas de uso de aplicaciones (tiempo spent en cada app, frecuencia de apertura)
- Información sobre aplicaciones instaladas en el dispositivo
- Historial de uso para funciones de bloqueo inteligente y control parental

### Datos de Ubicación

- Ubicación aproximada (basada en redes Wi-Fi y torres celulares)
- Ubicación precisa (GPS, cuando esté disponible y autorizada)
- **Ubicación en segundo plano (Background Location):** Rest Cycle recopila datos de ubicación para permitir el rastreo parental y las notificaciones de llegada/salida (geocercado) **incluso cuando la aplicación está cerrada o no está en uso**. Esta información solo se comparte con la cuenta del padre o tutor legal vinculado.
- Estas se utilizan para funciones como:
  - Recordatorios contextuales
  - Funciones de "horario de descanso" inteligentes

### Información de Dispositivo y Técnica

- Modelo del dispositivo y versión del sistema operativoe)
- Información de red y conectividad
- Registros de errores y datos de rendimiento (para mejora continua)

### Datos de Comunicación y Notificaciones

- Información sobre notificaciones recibidas (para funciones de horario de descanso y modo No Molestar)
- Información del calendario (para sincronización de eventos y recordatorios)
- Estos datos se utilizan exclusivamente para proporcionar funcionalidades relacionadas con horarios de descanso y no se comparten con terceros

### Permisos Sensibles y APIs de Android (Obligatorio por Google Play)

Para que Rest Cycle funcione como una aplicación de control parental y bienestar digital, requerimos acceso a ciertas APIs sensibles de Android. Nos comprometemos a que el uso de estas herramientas se limita estrictamente a la funcionalidad principal de la app:

- **API de Accesibilidad (AccessibilityService API):** Rest Cycle utiliza la API de Accesibilidad exclusivamente para detectar qué aplicación está en pantalla y bloquear el acceso a aplicaciones restringidas por el padre o tutor. **No utilizamos esta API para recopilar datos personales, no leemos tu pantalla para fines publicitarios, ni observamos tu texto escrito.** La información interceptada por la API de Accesibilidad se procesa temporalmente y solo se utiliza para hacer cumplir los límites de tiempo de pantalla.
- **Acceso a Datos de Uso (UsageStatsManager):** Utilizamos este permiso para recopilar estadísticas sobre cuánto tiempo pasas en cada aplicación. Estos datos se sincronizan con la cuenta del tutor legal para generar reportes y hacer cumplir los horarios de descanso. **No vendemos ni compartimos tu historial de uso de aplicaciones con terceros (anunciantes o agencias de datos).**

## 2. Cómo Usamos tu Información

### Para Proveer la Funcionalidad Principal

- Autenticación y gestión segura de cuentas
- Funciones de control parental y monitoreo de uso
- Bloqueo inteligente de aplicaciones basado en tiempo
- Gestión de horarios de descanso (modo No Molestar y escala de grises)
- Sincronización entre dispositivos (cuando el usuario lo habilita)
- Notificaciones y recordatorios contextuales
- Generación de reportes y estadísticas de uso

### Para Mejorar Nuestros Servicios

- Análisis de tendencias de uso para mejorar características existentes
- Identificación y solución de problemas técnicos
- Desarrollo de nuevas funcionalidades basadas en necesidades de usuarios
- Personalización de la experiencia de usuario (solo con datos agregados y anónimos)

### Para Comunicación

- Envío de notificaciones relacionadas con la cuenta y seguridad
- Respuestas a solicitudes de soporte técnico
- Información sobre actualizaciones, nuevas funciones y mejoras
- Comunicaciones de marketing (únicamente si el usuario ha otorgado consentimiento explícito)

## 3. Compartición de Información

### Con Tu Consentimiento Explícito

- Integración con servicios de salud (Google Fit, Apple Health) cuando el usuario los vincule
- Compartición con servicios de terceros cuando el usuario active integraciones específicas

### Con Proveedores de Servicios Esenciales

- Servicios de alojamiento e infraestructura en la nube (Supabase para almacenamiento de perfil y preferencias)
- Servicios de analítica para mejorar la aplicación (Firebase Analytics)
- Servicios de notificaciones push (Firebase Cloud Messaging)
- Servicios de autenticación y gestión de usuarios

### Por Requisitos Legales o de Protección

- Cuando sea requerido por ley, regulación, orden judicial o proceso legal válido
- Para proteger nuestros derechos legales, derechos de propiedad intelectual o la seguridad de otros usuarios
- Para prevenir, detectar o investigar actividades fraudulentas, ilegales o perjudiciales
- En casos de emergencia que involucren riesgo de daño físico o pérdida de vida

### En Caso de Transacciones Empresariales

- En caso de fusión, adquisición, venta de activos o reestructuración empresarial
- Tus datos serán transferidos sujeto a las mismas protecciones de privacidad descritas aquí

## 4. Tus Derechos y Opciones

### Acceso, Corrección y Eliminación

- Derecho a acceder a toda tu información personal que mantenemos
- Derecho a corregir información inexacta o incompleta
- Derecho a solicitar la eliminación completa de tu cuenta y todos los datos asociados
- Derecho a retirar tu consentimiento para ciertos tipos de procesamiento de datos

### Control sobre la Recopilación de Datos

- Poder revocar permisos de ubicación en cualquier momento desde ajustes del sistema
- Poder desactivar la recopilación de datos de uso de aplicaciones
- Poder limitar el acceso a sensores específicos de movimiento y actividad
- Poder desactivar la sincronización con servicios externos (manteniendo funcionalidad local)

### Preferencias de Comunicación

- Poder optar por no recibir comunicaciones de marketing en cualquier momento
- Poder configurar qué tipos de notificaciones deseas recibir
- Poder controlar la frecuencia y canal de las comunicaciones

### Derechos Especiales según Jurisdicción

- Si resides en la UE: Derechos adicionales bajo GDPR (portabilidad de datos, restricción de procesamiento)
- Si resides en California: Derechos adicionales bajo CCPA/CPRA (opt-out de venta de datos, corrección)
- Si resides en otras jurisdicciones: Derechos aplicables según leyes locales de protección de datos

## 5. Seguridad de la Información

Implementamos medidas de seguridad técnicas y organizativas razonables para proteger tu información:

### Medidas Técnicas

- Encriptación de datos en tránsito utilizando TLS/HTTPS 1.2 o superior
- Almacenamiento seguro de credenciales con hash bcrypt y salt único por usuario
- Protección contra inyecciones SQL y otros ataques comunes de aplicaciones web
- Escaneos regulares de vulnerabilidades y pruebas de penetración
- Actualizaciones automáticas de seguridad para dependencias y sistemas

### Medidas Organizativas

- Control de acceso basado en roles y principio de mínimo privilegio
- Monitoreo continuo de accesos sospechosos y actividades anómalas
- Capacitación regular del equipo en prácticas de seguridad y privacidad
- Procedimientos establecidos para respuesta a incidentes de seguridad
- Evaluaciones periódicas de impacto en privacidad para nuevas funcionalidades

### Limitaciones

Aunque nos esforzamos por proteger tu información, ningún método de transmisión por Internet o almacenamiento electrónico es 100% seguro. No podemos garantizar seguridad absoluta.

## 6. Retención de Datos

Mantenemos tu información personal mientras:

- Tu cuenta permanezca activa y en buen estado
- Sea necesario para proporcionar los servicios que has solicitado
- Sea requerido por obligaciones legales, regulatorias o tribunales competentes
- Sea necesario para resolver disputas, hacer cumplir nuestros términos o prevenir fraudes

### Períodos Específicos

- Datos de cuenta: Hasta eliminación de la cuenta por el usuario
- Datos de uso y estadísticas: Máximo 24 meses después de inactividad de cuenta
- Registros de seguridad y logs: Máximo 12 meses (excepto aquellos necesarios para investigaciones en curso)
- Información de pago: Solo lo necesario para cumplimiento fiscal y legal (según regulaciones aplicables)

### Eliminación

Al eliminar tu cuenta:

- Iniciamos inmediatamente el proceso de eliminación de todos los datos asociados
- Los datos se eliminan de nuestros sistemas activos dentro de los 30 días
- Copias de seguridad pueden retener datos por períodos adicionales según políticas de recuperación de desastres
- Algunos datos pueden retenerse según lo requerido por ley (por ejemplo, registros financieros para fines tributarios)

## 7. Transferencias Internacionales de Datos

Tu información puede ser transferida y procesada en países fuera de tu jurisdicción de residencia, incluyendo:

- Estados Unidos (donde se encuentran nuestros servidores principales en Supabase)
- Otros países donde operen nuestros proveedores de servicios

Al usar nuestros servicios, consientes dicha transferencia. Nos aseguramos de que todas las transferencias cumplan con:

- Cláusulas contractuales tipo aprobadas por autoridades de protección de datos
- Mecanismos de traslado equivalentes cuando se requieran por ley
- Protecciones adecuadas según el Reglamento General de Protección de Datos (GDPR) y otras regulaciones aplicables

## 8. Privacidad de Menores

Nuestro servicio tiene aspectos dirigidos a familias y control parental, pero:

- No recopilamos conscientemente información personal de menores de 13 años sin verificable consentimiento parental
- Las funciones de control parental están diseñadas para ser utilizadas por adultos que supervisan a menores
- Si descubrimos que hemos recopilado información personal de un menor sin verificable consentimiento parental, eliminaremos dicha información inmediatamente
- Los padres o tutores legales pueden acceder, corregir o solicitar eliminación de la información de menores bajo su supervisión

## 9. Cambios a Esta Política de Privacidad

Podemos actualizar esta política de privacidad de vez en cuando para reflejar cambios en nuestras prácticas o por razones legales, regulatorias o operativas. Cuando hagamos cambios significativos:

- Te notificaremos mediante notificación dentro de la aplicación con anticipación razonable
- Actualizaremos la fecha de "última actualización" en la parte superior de este documento
- En casos de cambios materiales, podremos requerir tu consentimiento explícito continuo
- Te recomendamos revisar esta política periódicamente para estar informado sobre cómo protegemos tu información

## 10. Cómo Contactarnos

Si tienes preguntas sobre esta política de privacidad, nuestras prácticas de privacidad, o deseas ejercer alguno de tus derechos, por favor contáctanos en:

**Correo de privacidad:** privacidad@restcycle.app
**Equipo de protección de datos:** Rest Cycle Data Protection Team
**Dirección postal:** [Insertar dirección física si aplica]

Responderemos a todas las solicitudes dentro del plazo legalmente requerido (típicamente 30 días) y proporcionaremos asistencia razonable para ejercer tus derechos de protección de datos.

## 11. Ley Aplicable y Jurisdicción

Esta política de privacidad se rige e interpreta de acuerdo con las leyes de [Insertar Jurisdicción Principal, ej: España o México], sin tener en cuenta sus principios de conflicto de leyes.

Cualquier disputa derivada o relacionada con esta política será sometida a la jurisdicción exclusiva de los tribunales competentes de [Insertar Ciudad, Jurisdicción], renunciando las partes expresamente a cualquier otro fuero que pudiera corresponderles.

## 12. Consentimiento

Al crear una cuenta y usar nuestra aplicación, reconoces que has leído, comprendido y aceptado esta política de privacidad y el procesamiento de tu información personal como se describe aquí. Tu uso continuo de la aplicación constituye tu consentimiento continuo a estos términos.

Si no estás de acuerdo con alguna parte de esta política, por favor no uses la aplicación o elimina tu cuenta inmediatamente.
