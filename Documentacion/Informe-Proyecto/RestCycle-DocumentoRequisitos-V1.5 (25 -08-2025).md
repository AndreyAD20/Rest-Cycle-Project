# Requisito Funcionales y No Funcionales

Requisitos para el proyecto Rest Cycle

## Nomenclatura

Con el fin de mantener uniformidad en el presente documento, se adoptan las siguientes convenciones:

Requisitos: se identifican con el prefijo RF (Requisito Funcional) y RNF (Requisito No Funcional), seguido de un número consecutivo (ejemplo: RF.1, RNF.3).

Actores del sistema: se escriben en Mayúscula Inicial:
Usuario: Persona con interes en mejorar sus habitos al usar un dispositivo movil.
Administrador: Personal encargado de la gestión técnica de la aplicación.
Usuario que gestiona:Padre/madre o tutor con control sobre las cuentas enlazadas.
Usuario gestionado: Hijo/a o menor de edad cuya actividad se controla.

Las notificaciones del sistema se representarán en Mayúscula Inicial (ejemplo: Notificación de uso excesivo).

## Objetivo principal

Desarrollar y promover una aplicación móvil que fomente hábitos saludables en el uso del celular mediante herramientas de control parental y autogestión del tiempo, ayudando a reducir la dependencia tecnológica y promoviendo un uso equilibrado y consciente de los dispositivos móviles.

## Alcance

El proyecto se define en la salud de los adultos y niños creando una restricción en el uso continuo del teléfono, mostrando una notificación instantánea que le indique el tiempo que lleva usándolo. Ya cumplido el tiempo limite de uso; de momento se bloqueará las aplicaciones y se indicara la reactivación de las apps. De esta forma los padres pueden minimizar el tiempo que sus hijos están utilizando el dispositivo.

## Requisitos

## REQUISITOS FUNCIONALES

RF.1 [Alta] REGISTRO DE USUARIO  
Permitir el registro de usuarios nuevos, solicitando datos esenciales para la creación de la cuenta.

RF.2 [Alta] ACEPTACIÓN DE TÉRMINOS Y CONDICIONES  
Solicitar la aceptación de términos y condiciones para completar el registro.

RF.3 [Alta] INICIO DE SESIÓN  
Permitir el ingreso de usuarios mediante credenciales.

RF.4 [Alta] RECUPERACIÓN Y CAMBIO DE CONTRASEÑA  
Facilitar el cambio de contraseña en caso de olvido, mediante códigos enviados por correo electrónico o teléfono.

RF.5 [Media] SELECCIÓN DE MEMBRESÍA  
Permitir la elección de membresía gratuita o de pago.

RF.6 [Media] SELECCIÓN DE MODO DE USO  
Permitir el acceso a los modos de control de hábitos saludables o control parental.

RF.7 [Alta] CREACIÓN DE CUENTAS PARA MENORES  
Permitir la creación de cuentas para menores de edad, enlazadas automáticamente a la cuenta del usuario que gestiona.

RF.8 [Alta] VINCULACIÓN DE CUENTAS  
Permitir vincular cuentas entre el usuario que gestiona y el usuario gestionado.

RF.9 [Alta] CREACIÓN DE CONTRASEÑA PARA USUARIO GESTIONADO  
Permitir la creación de contraseña específica para el usuario gestionado.

RF.10 [Media] CONSULTA DE HISTORIAL DE USO  
Permitir la visualización de estadísticas de uso de aplicaciones por parte del usuario.

RF.11 [Media] VISUALIZACIÓN DE TAREAS PENDIENTES  
Permitir ver las tareas pendientes generadas por el usuario que gestiona.

RF.12 [Media] GESTIÓN DE TAREAS PENDIENTES  
Permitir al usuario que gestiona agregar, modificar y eliminar tareas, eventos o compromisos pendientes para el usuario gestionado.

RF.13 [Media] ASIGNACIÓN DE TIEMPO ADICIONAL POR TAREAS COMPLETADAS  
Permitir al usuario que gestiona modificar el tiempo de uso de aplicaciones como recompensa por completar tareas.

RF.14 [Alta] NOTIFICACIONES DE ACTIVIDADES  
Notificar información sobre la actividad del usuario enlazado, especialmente sobre uso excesivo de aplicaciones.

RF.15 [Alta] NOTIFICACIÓN DE USO EXCEDIDO  
Notificar al usuario que gestiona y al gestionado cuando se excede el tiempo de uso en alguna aplicación.

RF.16 [Media] NOTIFICACIÓN DE TAREAS Y ACTIVIDADES  
Notificar al usuario que gestiona y al gestionado sobre tareas y actividades registradas.

RF.17 [Media] NOTIFICACIÓN DE RECOMENDACIONES AL PADRE  
Notificar al usuario que gestiona sobre actividades recomendadas para mejorar los hábitos del usuario enlazado.

RF.18 [Alta] BLOQUEO GENERAL DE APLICACIONES  
Permitir el bloqueo instantáneo de todas las aplicaciones seleccionadas por el usuario que gestiona.

RF.19 [Media] NOTIFICACIÓN DE INTENTO DE INSTALACIÓN  
Notificar al usuario que gestiona sobre intentos de instalación de aplicaciones por parte del usuario gestionado.

RF.20 [Media] GESTIÓN DE PERMISOS DE INSTALACIÓN  
Permitir al usuario que gestiona aprobar o rechazar la instalación de aplicaciones por parte del usuario gestionado.

RF.21 [Baja] VINCULACIÓN CON CALENDARIO  
Permitir vincular la aplicación con un calendario para gestionar tareas y actividades pendientes del usuario enlazado.

RF.22 [Alta] ESTABLECIMIENTO DE TIEMPO MÁXIMO DIARIO POR APLICACIÓN  
Permitir establecer el tiempo máximo de uso diario en aplicaciones seleccionadas para el usuario gestionado.

RF.23 [Alta] BLOQUEO TOTAL POR HORARIO  
Permitir al usuario que gestiona definir horarios en los que el dispositivo del usuario gestionado se bloquea completamente.

RF.24 [Media] INFORME DE TIEMPO DE USO DEL DISPOSITIVO  
Permitir al usuario que gestiona consultar informes sobre el tiempo de uso del dispositivo móvil por parte del usuario gestionado.

RF.25 [Media] CONSULTA DE HISTORIAL DE NAVEGACIÓN  
Permitir al usuario que gestiona ver el historial de navegación del usuario gestionado.

RF.26 [Media] GESTIÓN DE LISTA NEGRA DE SITIOS WEB  
Permitir al usuario que gestiona agregar sitios web a una lista negra para bloquear el acceso desde los navegadores del usuario gestionado.

RF.27 [Baja] ACTIVACIÓN DE GPS  
Permitir al usuario gestionado activar el GPS con los permisos correspondientes.

RF.28 [Media] REPORTE DE UBICACIÓN  
Permitir al usuario que gestiona recibir reportes de ubicaciones donde estuvo el dispositivo del usuario gestionado.

RF.29 [Alta] NOTIFICACIÓN POR USO EXCESIVO  
Notificar cuando el usuario gestionado utiliza una aplicación después de superar el tiempo límite.

RF.30 [Baja] NOTIFICACIONES MOTIVACIONALES  
Enviar mensajes motivacionales al usuario para fomentar mejores hábitos.

RF.31 [Baja] NOTIFICACIONES DE TEMAS DE INTERÉS  
Notificar al usuario sobre temas de interés seleccionados.

RF.32 [Media] PERSONALIZACIÓN DE NOTIFICACIONES  
Permitir al usuario configurar qué tipo de notificaciones desea recibir y en qué horario.

RF.33 [Media] RECORDATORIOS DE ACTIVIDADES PENDIENTES  
Notificar al usuario sobre actividades pendientes y sus fechas de realización.

RF.34 [Baja] ALERTA POR USO PROLONGADO DE AURICULARES  
Notificar al usuario cuando se detecta un uso excesivo de audífonos.

RF.35 [Baja] ENLACE CON OTRAS APLICACIONES  
Permitir enlazar la aplicación con otras apps para gestionar el tiempo de uso diario.

RF.36 [Alta] ESTABLECIMIENTO DE TIEMPO MÁXIMO POR EXCESO DE USO  
Permitir definir el tiempo máximo de uso diario para una aplicación.

RF.37 [Media] SELECCIÓN DE MEDIDAS POR EXCESO DE TIEMPO  
Permitir seleccionar las acciones a tomar cuando se alcanza el tiempo máximo de uso de una aplicación.

RF.38 [Media] INFORME DE TIEMPO EN APLICACIONES  
Permitir solicitar informes del tiempo de uso por aplicación y filtrar por tipo de aplicación.

RF.39 [Media] ESTABLECIMIENTO DE HORARIO DE DESCANSO  
Permitir definir horas de descanso en las que el dispositivo no se puede usar.

RF.40 [Media] DEFINICIÓN DE MEDIDAS DURANTE EL DESCANSO  
Permitir establecer acciones que se aplican durante el horario de descanso.

RF.41 [Baja] CONEXIÓN CON CALENDARIO DEL DISPOSITIVO  
Permitir enlazar la aplicación con el calendario nativo del dispositivo.

RF.42 [Media] GESTIÓN DE TAREAS Y COMPROMISOS  
Permitir agendar tareas y compromisos pendientes con detalles relevantes.

RF.43 [Baja] ALMACENAMIENTO DE INFORMACIÓN RELEVANTE  
Permitir crear notas con título y contenido.

RF.44 [Baja] ELECCIÓN DE TEMAS DE INTERÉS  
Permitir seleccionar los temas de interés del usuario.

RF.45 [Baja] MODIFICACIÓN DE TEMAS DE INTERÉS  
Permitir cambiar los temas de interés seleccionados.

RF.46 [Alta] ELIMINACIÓN DE CUENTAS FRAUDULENTAS  
Permitir al administrador eliminar cuentas con fallos o intentos de fraude en la autenticación.

RF.47 [Baja] SUGERENCIAS PARA MEJORAS  
Permitir al administrador enviar sugerencias a los programadores para editar interfaces o funciones.

RF.48 [Media] GESTIÓN DE QUEJAS Y RECLAMOS  
Permitir al administrador consultar y gestionar quejas y reclamos de los usuarios.

RF.49 [Baja] REPORTES DE PLANES PREMIUM  
Permitir al administrador consultar reportes de los planes premium activos.

RF.50 [Media] LISTADO DE CUENTAS  
Permitir al administrador generar listados de cuentas comunes y premium para su gestión.

RF.51 [Media] GESTIÓN DE CUENTAS PREMIUM VENCIDAS  
Permitir al administrador revisar y gestionar cuentas premium vencidas.

RF.52 [Media] GESTIÓN DE PAGOS Y CONTRATOS  
Permitir al administrador gestionar pagos y contratos con empresas publicitarias.

RF.53 [Baja] ENVÍO DE OFERTAS  
Permitir al administrador generar listados y enviar ofertas a usuarios registrados.

RF.54 [Baja] GESTIÓN DE CONTRATOS CON PATROCINADORES  
Permitir al administrador gestionar contratos con patrocinadores.

RF.55 [Alta] CAMBIO DE CONTRASEÑA  
Permitir al usuario modificar su contraseña.

RF.56 [Media] SELECCIÓN DE IDIOMA  
Permitir al usuario cambiar el idioma de la aplicación.

RF.57 [Media] CONTACTO CON SOPORTE TÉCNICO  
Permitir al usuario comunicarse con soporte técnico.

RF.58 [Media] EDICIÓN DE INFORMACIÓN PERSONAL  
Permitir al usuario modificar su información personal.

RF.59 [Baja] CAMBIO ENTRE TEMA CLARO Y OSCURO  
Permitir al usuario alternar entre tema claro y oscuro.

RF.60 [Alta] SOPORTE TÉCNICO  
Proveer comunicación y ayuda en caso de errores con la aplicación.

RF.61 [Alta] RESTABLECIMIENTO DE CUENTA  
Permitir el proceso de recuperación de cuentas bloqueadas o eliminadas accidentalmente.

RF.62 [Media] GESTIÓN AVANZADA DE NOTAS  
Permitir marcar notas como favoritas mediante un icono de estrella, asignar colores personalizados para organización visual, buscar notas por título mediante campo de búsqueda, y solicitar confirmación antes de eliminar para prevenir pérdidas accidentales de información.

RF.63 [Media] VISUALIZACIÓN GRÁFICA DE ESTADÍSTICAS  
Mostrar gráficos de barras animados y visualizaciones interactivas del tiempo de uso de aplicaciones, con filtros por período temporal (diario, semanal, mensual) y presentación de top 5 y top 10 aplicaciones más utilizadas.

RF.64 [Baja] INTEGRACIÓN CON MAPAS EN EVENTOS  
Permitir agregar ubicaciones geográficas a eventos del calendario con integración de Google Maps para visualización de mapas, selección de ubicaciones y navegación a los lugares de los eventos.

RF.65 [Media] NAVEGACIÓN LATERAL  
Implementar menú lateral deslizable (navigation drawer) para acceso rápido y organizado a todas las funcionalidades principales de la aplicación, incluyendo estadísticas, notas, calendario, tareas, horarios de descanso, bloqueo de apps y control parental.

## Normativas Aplicables

El desarrollo y operación de Rest Cycle debe cumplir con las siguientes normativas y estándares:

- **Protección de Datos Personales:** Cumplimiento de la Ley 1581 de 2012 (Colombia) y el Reglamento General de Protección de Datos (GDPR) para usuarios internacionales, garantizando el manejo seguro, consentimiento informado y derechos de los titulares de datos.
- **Seguridad de la Información:** Aplicación de buenas prácticas de seguridad como ISO/IEC 27001 para la gestión de la información, incluyendo cifrado de datos, autenticación robusta y control de accesos.
- **Accesibilidad Digital:** Adopción de las pautas WCAG 2.1 para asegurar que la plataforma sea accesible para personas con discapacidad.
- **Normativa Técnica:** Cumplimiento de estándares de interoperabilidad y desarrollo web (W3C), así como compatibilidad con sistemas operativos móviles vigentes.
- **Normativa de Protección Infantil:** Implementación de controles parentales y mecanismos de reporte conforme a la Ley 1098 de 2006 (Colombia) y recomendaciones internacionales para la protección de menores en entornos digitales.
- **Propiedad Intelectual:** Respeto por derechos de autor y licencias de software de terceros utilizados en la plataforma.

El equipo de desarrollo debe revisar periódicamente la legislación vigente y actualizar los procesos y funcionalidades para asegurar el cumplimiento normativo.

## REQUISITOS NO FUNCIONALES

RNF.01 El sistema notifica a través de una burbuja tipo chat, esta notificación debe enviarse y generarse entre 5 a 7 segundos de respuesta.

RNF.02 La interfaz debe ser fácil de manejar con un diseño atractivo y muy accesible.

RNF.03 La aplicación debe ser accesible y que cuente con un soporte en varios idiomas.

RNF.04 La aplicación debe responder a las interacciones con el usuario en menos de 4 segundos.

RNF.05 La aplicación debe ser capaz de manejar mil usuarios simultáneamente sin degradar el rendimiento del sistema.
RNF.06 El sistema debe estar disponible las 24 horas del día, los 7 días de la semana.

RNF.07 La aplicación debe garantizar la protección de los datos obtenidos del usuario mediante un cifrado de datos y medidas de seguridad robustas.

RNF.08 La aplicación debe ser compatible con la mayoría de las versiones de sistemas operativos móviles y con los más recientes.

RNF.09 La aplicación debe funcionar correctamente en la mayoría de los dispositivos móviles.

RNF.10 Las actualizaciones de la aplicación no deben afectar la integridad de los datos existentes.

RNF.11 La aplicación debe contar con un proceso de control de calidad, para que las nuevas versiones no lleven errores.

RNF.12 La aplicación debe soportar un aumento del 50% de los usuarios concurrentes sin afectar integridad.

RNF.13 ESCALABILIDAD: El sistema debe poder escalar horizontalmente para soportar el crecimiento de usuarios y datos.

RNF.14 RECUPERACIÓN ANTE DESASTRES: Debe existir un plan y mecanismos de respaldo y recuperación ante fallos graves.

RNF.15 MONITOREO Y LOGS: El sistema debe contar con monitoreo en tiempo real y registro de logs para detectar y solucionar problemas rápidamente.

RNF.16 DOCUMENTACIÓN DE USUARIO: Manuales y tutoriales accesibles para todos los tipos de usuario.

RNF.17 SOPORTE A ACTUALIZACIONES AUTOMÁTICAS: La aplicación debe poder actualizarse automáticamente sin intervención del usuario.

RNF.18 CUMPLIMIENTO DE NORMAS DE USABILIDAD: Validar que la interfaz cumple con estándares internacionales de usabilidad.

RNF.19 PROTECCIÓN CONTRA ATAQUES: Medidas específicas contra ataques comunes (phishing, malware, fuerza bruta).

## Glosario

**Administrador:** Persona encargada de la gestión técnica y administrativa de la aplicación.

**Autenticación:** Proceso de verificación de la identidad de un usuario.

**Cuenta enlazada:** Relación entre la cuenta del usuario que gestiona y la del usuario gestionado.

**Gestión de hábitos:** Proceso de manejo y control del tiempo de uso de dispositivos móviles.

**Historial de uso:** Registro de actividades y tiempo de uso de aplicaciones o dispositivos.

**Notificación:** Mensaje enviado por el sistema para informar sobre eventos relevantes.

**Protección de datos:** Conjunto de medidas para garantizar la seguridad y privacidad de la información personal.

**Requisito funcional (RF):** Característica o función que el sistema debe cumplir.

**Requisito no funcional (RNF):** Restricción o condición de calidad que el sistema debe satisfacer.

**Usuario:** Persona que utiliza la aplicación para mejorar sus hábitos o gestionar cuentas.

**Usuario gestionado:** Menor de edad cuya actividad es supervisada por el usuario que gestiona.

**Usuario que gestiona:** Padre, madre o tutor responsable de la supervisión de cuentas enlazadas.

**V1.1 Autor:** Andrey Alejandro Suesca Fuentes
**Revisado por:**
