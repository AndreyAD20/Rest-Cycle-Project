
# Requisito Funcionales y No Funcionales

Casos de uso

Nombre del caso de uso	RF.1 - Registro de Nuevo Usuario
Actor(es)	Usuario
Precondiciones	El usuario debe acceder a la opción de registro de la aplicación.
Flujo normal	•	El usuario selecciona "Registrarse" en la pantalla de inicio.
•	La aplicación solicita datos como nombre de usuario, correo electrónico, número de teléfono y fecha de nacimiento.
•	Alternativamente, el usuario puede registrarse mediante cuentas de terceros (Google, Microsoft, iCloud, Facebook).
•	El usuario ingresa los datos requeridos o autoriza el enlace a la cuenta de terceros.
•	El usuario revisa y acepta los términos y condiciones.
•	La aplicación muestra un mensaje de confirmación de registro exitoso.
Flujo alternativo	•	Error en el ingreso de datos: Si los datos son incorrectos o incompletos.
•	la aplicación muestra un mensaje de error.
Postcondiciones	El usuario queda registrado en la base de datos.

Nombre del caso de uso	RF.2 - Aceptación de Términos y Condiciones
Actor(es)	Usuario
Precondiciones	El usuario debe estar en proceso de registro.
Flujo normal	•	Durante el registro, la aplicación muestra los términos y condiciones.
•	El usuario debe aceptar los términos y condiciones para continuar.
•	Al aceptar, el usuario puede completar el registro.
Flujo alternativo	•	Si el usuario no acepta los términos, no podrá completar el registro.
Postcondiciones	El usuario acepta los términos y condiciones.
 

Nombre del caso de uso	RF.3 - Inicio de Sesión
Actor(es)	Usuario
Precondiciones	El usuario debe tener una cuenta registrada.
Flujo normal	•	El usuario selecciona "Iniciar sesión" en la pantalla de inicio.
•	El usuario ingresa su nombre de usuario y contraseña, o selecciona el inicio de sesión con cuenta de terceros.
•	La aplicación verifica las credenciales.
•	Al aprobar la autenticación, el usuario accede a la aplicación.
Flujo alternativo	•	Error en credenciales: Si los datos son incorrectos, la aplicación muestra un mensaje de error.
Postcondiciones	El usuario accede a la aplicación.
 
Nombre del caso de uso	RF.4 - Recuperación de Contraseña
Actor(es)	Usuario
Precondiciones	El usuario debe estar registrado y olvido su contraseña.
Flujo normal	•	El usuario selecciona "Olvidé mi contraseña" en la pantalla de inicio.
•	La aplicación solicita el nombre de usuario y correo electrónico o teléfono.
•	La aplicación envía un enlace de recuperación o código al correo o teléfono proporcionado.
•	El usuario sigue las instrucciones para cambiar su contraseña.
Flujo alternativo	•	El correo electrónico o el teléfono ingresados son incorrectos.
•	El sistema muestra un mensaje de error indicando que los datos no coinciden con ninguna cuenta registrada.
•	Se ofrece al usuario la opción de intentarlo nuevamente o contactar al soporte para recibir asistencia.
Postcondiciones	El usuario puede iniciar sesión con la nueva contraseña.

 
Nombre del caso de uso	RF.5 - Selección de Modo de Aplicación
Actor(es)	Usuario
Precondiciones	El usuario debe estar autenticado en la aplicación.
Flujo normal	•	Al iniciar, la aplicación muestra la opción de seleccionar entre "Modo Control Parental" y "Modo Hábitos Saludables".
•	El usuario selecciona el modo deseado.
•	La aplicación lo redirige a la interfaz del modo deseado.
Postcondiciones	El usuario accede a la interfaz del modo seleccionado.


 
Nombre del caso de uso	RF.6 - Creación de Cuentas para Hijos en Modo Parental
Actor(es)	Usuario (Padre/Tutor)
Precondiciones	El usuario debe estar en Modo Control Parental.
Flujo normal	•	El usuario selecciona la opción para crear una cuenta de menor.
•	La aplicación solicita datos como nombre de usuario, contraseña, edad y sexo del menor.
•	El usuario ingresa los datos requeridos.
•	La aplicación confirma la creación de la cuenta del menor.
Flujo alternativo	•	Al crear el usuario, este es mayor de edad
•	El sistema no permite la creación del usuario y manda un mensaje de que solo se puede crear usuarios para menores de edad.
Postcondiciones	La cuenta del menor queda registrada y enlazada al usuario padre/tutor.
 

Nombre del caso de uso	RF.7 - Enlace de Cuenta de Menor con Cuenta Registrada
Actor(es)	Usuario (Padre/Tutor) Usuario (Jóvenes y Adolescentes)
Precondiciones	El usuario (Jóvenes y Adolescentes) ya debe tener una cuenta registrada.
El usuario debe estar en Modo Control Parental.
Flujo normal	•	El usuario selecciona la opción para enlazar una cuenta existente de menor.
•	La aplicación solicita el correo electrónico del menor.
•	El usuario ingresa el correo electrónico del usuario (Jóvenes y Adolescentes).
•	El usuario (Jóvenes y Adolescentes) recibe un correo electrónico y le da en la opción aceptar.
Flujo alternativo	•	El correo electrónico no está en uso con ninguna cuenta registrada.
•	El sistema genera un mensaje de correo invalido.
•	El sistema le permite volver a ingresar el correo electrónico.
Postcondiciones	La cuenta del menor queda enlazada para control parental.
 
Nombre del caso de uso	RF.8 - Activación de Contraseña Segura en Control Parental
Actor(es)	Usuario (Padre/Tutor)
Precondiciones	La cuenta del usuario debe estar enlazada.
Flujo normal	•	El usuario accede a la opción de configuración de contraseña segura.
•	La aplicación permite activar una contraseña segura.
•	El usuario activa la contraseña segura o la modifica según sus razones.
•	El usuario guarda la configuración.
•	La aplicación le pregunta si desea guardar los cambios.
•	El usuario acepta.
Flujo alternativo	•	Al modificar la contraseña segura no cumple con los parámetros
•	El sistema no dejara modificar la contraseña segura
Postcondiciones	La contraseña segura queda activada, desactivada o modificada según lo configurado.
 
Nombre del caso de uso	RF.9 - Ingreso de Contraseña Segura para Acceso al Modo Parental
Actor(es)	Usuario (Jóvenes y adolescentes)
Precondiciones	La contraseña segura debe estar activada
Flujo normal	•	El usuario entra a Rest Cycle e intenta acceder al Modo Control Parental.
•	La aplicación solicita la contraseña segura.
•	El usuario ingresa la contraseña para acceder.
•	La contraseña ingresada es correcta 
Flujo alternativo	•	La contraseña ingresada es incorrecta.
•	La aplicación no le da acceso a control parental.
Postcondiciones	El usuario accede al modo parental y solo puede agregar aplicación para vincular

Nombre del caso de uso	RF.10 - Notificación de Uso Excesivo de Aplicaciones en Dispositivo Enlazado
Actor(es)	Sistema, Usuario (Padre/Tutor)
Precondiciones	El dispositivo del usuario menor debe estar enlazado.
Flujo normal	•	La aplicación monitorea el tiempo de uso de las aplicaciones del dispositivo enlazado.
•	Cuando el joven llega al máximo de tiempo establecido.
•	La aplicación envía una notificación al usuario tutor.
Postcondiciones	El usuario tutor recibe la notificación de uso excesivo.
 
Nombre del caso de uso	RF.11 - Notificación de Tiempo Restante de Uso de Aplicaciones
Actor(es)	Sistema, Usuario (Padre/Tutor y Jóvenes y adolescentes)
Precondiciones	La aplicación debe estar monitoreando el tiempo de uso.
Flujo normal	•	La aplicación calcula el tiempo restante para el uso de aplicaciones.
•	Se envía una notificación a ambos usuarios cuando el tiempo restante es crítico.
Postcondiciones	Ambos usuarios reciben la notificación del tiempo restante.

Nombre del caso de uso	RF.12 - Notificación de Tareas o Actividades Pendientes
Actor(es)	Sistema, Usuario (Padre/Tutor y jóvenes y adolescentes)
Precondiciones	Deben existir tareas o actividades pendientes registradas.
Flujo normal	•	La aplicación revisa las tareas pendientes y su proximidad.
•	Se envía una notificación al usuario tutor y al joven cuando las fechas de estas son próximas.
Postcondiciones	Ambos usuarios son notificados de tareas o actividades.
 

Nombre del caso de uso	RF.13 - Vinculación de Aplicaciones para Bloqueo en Modo Parental
Actor(es)	Usuario (Padre/Tutor)
Precondiciones	El dispositivo del menor debe estar enlazado
Flujo normal	•	El usuario entra a Rest Cycle en el modo control parental y selecciona la opción de gestión de dispositivos.
•	El usuario entra en la opción gestión de apps, y selecciona las apps para gestionar, y le da en vincular.
•	La aplicación le muestra un mensaje que la vinculación fue exitosa.
•	El usuario bloquea las apps vinculadas que desee, y le da en bloquear.
•	La aplicación guarda la configuración.
Postcondiciones	Las aplicaciones seleccionadas quedan bloqueadas en el dispositivo del joven.
 
Nombre del caso de uso	RF.14 - Notificación de Instalación o Descarga de Nuevas Aplicaciones
Actor(es)	Sistema, Usuario (Padre/Tutor y Jóvenes y adolescentes)
Precondiciones	La cuenta del menor debe estar enlazada.
Flujo normal	•	El usuario joven inicia una descarga/instalación
•	La aplicación notifica al usuario tutor
•	El usuario es notificado y realiza los respectivos procedimientos
Postcondiciones	El tutor es notificado sobre la instalación o descarga.

Nombre del caso de uso	RF.15: Permitir o denegar la descarga o instalación de aplicaciones en el dispositivo enlazado.
Actor(es)	Usuario (Padre/Tutor) Usuario jóvenes y Adolescentes (usuario enlazado)
Precondiciones	El usuario debe tener una cuenta activa en Rest Cycle.
El usuario debe haber enlazado su cuenta con el dispositivo del usuario Joven.
El usuario debe estar en el Modo Control Parental y autenticado correctamente.
Flujo normal	•	El joven intenta descargar o instalar una aplicación.
•	El usuario tutor es notificado de que el joven quiere instalar o descargar una aplicación.
•	El usuario tutor selecciona la opción para gestionar las descargas e instalaciones en el dispositivo enlazado.
•	El sistema muestra dos opciones: "Permitir" y "Bloquear" la descarga e instalación de nuevas aplicaciones.
•	El usuario selecciona su preferencia (Permitir o Bloquear) y confirma la acción.
•	El sistema actualiza la configuración en tiempo real para aplicar la restricción o permiso seleccionado en el dispositivo del usuario enlazado.
Postcondiciones	La configuración de descarga e instalación de aplicaciones en el dispositivo del usuario Joven se ha actualizado exitosamente, aplicando el permiso o restricción según la elección del usuario.

 

Nombre del caso de uso	RF.16 – Enlace a Calendario para gestionar tareas y actividades.
Actor(es)	Usuario Padre/Tutor, Usuario Joven (usuario enlazado)
Precondiciones	La cuenta del Joven debe estar enlazada.
Flujo normal	•	El usuario entra a Rest Cycle en el modo control parental y selecciona la opción de Agendar tareas.
•	La aplicación le pide que la enlace con el calendario.
•	El sistema solicita permisos de acceso y vinculación con el calendario del dispositivo enlazado.
•	El sistema vincula el calendario y permite al usuario Padre/Tutor agregar, modificar o eliminar tareas y actividades en el calendario del usuario Joven.
Postcondiciones	El usuario Padre/Tutor ha vinculado el calendario con el dispositivo del usuario Joven y puede gestionar sus actividades a través de la aplicación.
 
Nombre del caso de uso	RF.17 - Configuración de Tiempo de Uso Diario en Aplicaciones Enlazadas
Actor(es)	Usuario Padre/Tutor, Usuario Joven (usuario enlazado)
Precondiciones	El dispositivo del usuario Joven debe estar enlazado con la cuenta del usuario Padre/Tutor.
Flujo normal	•	El usuario accede a la opción de configuración de apps enlazadas en el Modo Control Parental.
•	El sistema muestra una lista de las aplicaciones enlazadas en el dispositivo del usuario joven.
•	El usuario selecciona una o más aplicaciones y establece el límite de tiempo de uso diario.
•	El usuario confirma los cambios.
•	El sistema guarda la configuración de tiempo de uso diario.
Flujo alternativo	•	Si el dispositivo enlazado no tiene conexión a Internet:
•	El sistema muestra un mensaje indicando que no es posible acceder al historial de navegación por falta de conexión en el dispositivo enlazado.
•	El caso de uso termina.
Postcondiciones	El tutor puede ver el historial de navegación del Joven
 
Nombre del caso de uso	RF.18 - Visualización de Estadísticas de Uso en Dispositivo Enlazado
Actor(es)	Usuario Padre/Tutor, Usuario Joven (usuario enlazado)
Precondiciones	La cuenta del Joven debe estar enlazada.
Flujo normal	•	El usuario Padre/Tutor accede a la aplicación Rest Cycle en el Modo Control Parental.
•	El usuario tutor ingresa a la opción ver estadísticas de uso.
•	La aplicación muestra un informe (diario, semanal y mensual) con los datos del tiempo que uso en las aplicaciones.
•	El usuario tutor selecciona el informe que desee ver.
Postcondiciones	El usuario tutor puede visualizar el tiempo de uso del dispositivo y las aplicaciones del menor.
 
Nombre del caso de uso	RF.19 - Visualización de Historial de Navegación del Dispositivo Enlazado
Actor(es)	Usuario Padre/Tutor, Usuario Joven (usuario enlazado)
Precondiciones	El dispositivo del usuario Joven debe estar enlazado con la cuenta del usuario Padre/Tutor.
Flujo normal	•	El usuario Tutor selecciona la opción "Ver Historial de Navegación".
•	El sistema solicita y recupera el historial de navegación desde el dispositivo enlazado del usuario Joven.
•	El sistema muestra en pantalla el historial de navegación reciente, con detalles de sitios visitados y fechas.
Flujo alternativo	•	Si el dispositivo enlazado no tiene conexión a Internet:
•	El sistema muestra un mensaje indicando que no es posible acceder al historial de navegación por falta de conexión en el dispositivo enlazado.
•	El caso de uso termina.
Postcondiciones	El tutor puede ver el historial de navegación del Joven
 
Nombre del caso de uso	RF.20 - Agregar Tareas o Actividades Pendientes para el Menor
Actor(es)	Usuario Padre/Tutor, Usuario Joven (usuario enlazado)
Precondiciones	La cuenta del Joven debe estar enlazada.
Flujo normal	•	El usuario Padre/Tutor accede a la aplicación Rest Cycle en el Modo Control Parental.
•	El usuario selecciona la opción agregar tareas o actividades pendientes.
•	El usuario agrega tarea.
•	La aplicación solicita detalles de la tarea, actividad o evento.
•	El usuario completa la información y guarda la tarea
Postcondiciones	La tarea o actividad queda registrada en la aplicación y vinculada al calendario del Joven.
 
Nombre del caso de uso	RF.21 - Notificación de Uso Excesivo en Modo Hábitos saludables
Actor(es)	Sistema, Usuario (Autocuidado)
Precondiciones	El usuario debe haber configurado un límite de tiempo para la aplicación en uso.
Flujo normal	•	El usuario está usando una aplicación por mucho tiempo y está a punto de llegar al límite establecido
•	La aplicación envía una notificación al usuario faltando 5 minutos para llegar al límite.
Postcondiciones	El usuario recibe una notificación sobre el uso excesivo de la aplicación.
 
Nombre del caso de uso	RF.22 - Notificaciones Motivacionales para Hábitos Saludables
Actor(es)	Sistema, Usuario (Autocuidado)
Precondiciones	El usuario debe permitir la activación de notificaciones motivacionales.
Flujo normal	•	La aplicación selecciona frases y recordatorios motivacionales.
•	Se envía una notificación al usuario con contenido motivacional para mejorar el uso del dispositivo.
Postcondiciones	El usuario recibe mensajes motivacionales para ayudarle a corregir sus hábitos de uso del celular.
 
Nombre del caso de uso	RF.23 - Notificaciones sobre Temas de Interés
Actor(es)	Sistema, Usuario (Autocuidado)
Precondiciones	El usuario debe permitir que se activen las notificaciones sobre temas de interés.
Flujo normal	•	La aplicación revisa el uso del dispositivo del usuario para idear los posibles temas de interés del usuario.
•	Cuando se dispone de nueva información, se envía una notificación al usuario sobre alguna noticia del tema de interés.
Postcondiciones	El usuario recibe notificaciones con información relevante a sus intereses.
 
Nombre del caso de uso	RF.24 - Notificación de Actividades y Fechas Próximas
Actor(es)	Sistema, Usuario (Autocuidado)
Precondiciones	Deben existir actividades y fechas configuradas por el usuario.
Flujo normal	•	La aplicación revisa las actividades programadas y próximas fechas importantes.
•	Envía notificaciones al usuario recordando las fechas
Postcondiciones	El usuario es notificado de sus actividades pendientes o fechas próximas.
 
Nombre del caso de uso	RF.25 - Notificación de Uso Prolongado de Audífonos
Actor(es)	Sistema, Usuario (Autocuidado)
Precondiciones	El usuario debe activar notificaciones del uso de audífonos.
Flujo normal	•	La aplicación monitorea el tiempo de uso de audífonos.
•	Cuando el uso excede el límite, se envía una notificación al usuario.
Postcondiciones	El usuario recibe una notificación de advertencia por uso prolongado de audífonos.

 

Nombre del caso de uso	RF.26 - Enlace Voluntario de Aplicaciones para Gestión de Tiempo de Uso
Actor(es)	Usuario (Autocuidado)
Precondiciones	El usuario debe tener aplicaciones que desea gestionar.
Flujo normal	•	El usuario entra a Rest Cycle y entra al Modo Hábitos Saludables.
•	Selecciona la opción de vincular aplicaciones.
•	El usuario selecciona aplicaciones que desea vincular, Y las agrega.
•	La aplicación las agrega con éxito
Postcondiciones	Las aplicaciones seleccionadas quedan para ser gestionadas para el control de tiempo de uso.
 
Nombre del caso de uso	RF.27 - Configuración de Tiempo Máximo Diario para Aplicaciones
Actor(es)	Usuario (Autocuidado)
Precondiciones	Las aplicaciones deben estar vinculadas.
Flujo normal	•	El usuario entra Rest Cycle y accede al Modo hábitos Saludables.
•	El usuario accede a la opción de gestión de aplicaciones.
•	El usuario establece un límite de tiempo máximo diario para las aplicaciones enlazadas.
•	La aplicación guarda el límite de tiempo configurado y lo aplica.
Postcondiciones	El límite diario queda establecido para las aplicaciones seleccionadas, limitando su uso diario.
 
Nombre del caso de uso	RF.28 - Configuración de Medidas al Exceder el Tiempo de Uso
Actor(es)	Usuario (Autocuidado)
Precondiciones	El usuario debe haber configurado un tiempo límite de uso para las aplicaciones.
Flujo normal	•	El usuario entra Rest Cycle y accede al Modo Hábitos Saludables.
•	El usuario accede a la opción de medidas de exceso.
•	El sistema le ofrece algunas medidas como, bloqueo de la aplicación, notificación, entre otros.
•	El usuario elige medidas a tomar cuando el tiempo de uso es excedido.
•	La aplicación guarda la configuración.
Postcondiciones	La aplicación aplicará las medidas configuradas cuando el tiempo máximo se alcance.
 
Nombre del caso de uso	RF.29 - Informe del Tiempo Utilizado en Aplicaciones
Actor(es)	Usuario (Autocuidado)
Precondiciones	El usuario debe haberse registrado y haber usado la app durante un tiempo 
Flujo normal	•	El usuario entra Rest Cycle y accede al Modo Hábitos Saludables.
•	El usuario accede a la opción de informes de uso
•	El usuario selecciona el informe que desea ver (Diario, Semanal, mensual).
•	La aplicación presenta un resumen del tiempo utilizado en cada aplicación durante el tiempo seleccionado.
Postcondiciones	El usuario visualiza el informe semanal de uso.
 
Nombre del caso de uso	RF.30 - Activación de la Hora de Descanso
Actor(es)	Usuario (Autocuidado)
Precondiciones	El usuario debe haber iniciado sesión en la aplicación.
El usuario desea activar la función de hora de descanso.
Flujo normal	•	El usuario entra Rest Cycle y accede al Modo Hábitos Saludables.
•	El usuario selecciona la opción de "Hora de Descanso".
•	El usuario define el horario en el que desea activar la hora de descanso.
•	El usuario guarda la configuración de la hora de descanso.
•	La aplicación verifica y almacena los datos de configuración.
Postcondiciones	La hora de descanso queda configurada y activa en la aplicación.
 
Nombre del caso de uso	RF.31 - Configuración de Medidas en Horario de Descanso
Actor(es)	Usuario (Autocuidado)
Precondiciones	Debe existir un horario de descanso previamente configurado.
Flujo normal	•	El usuario entra Rest Cycle y accede al Modo Hábitos Saludables.
•	El usuario accede a la opción de horas de descanso.
•	El usuario selecciona las medidas a tomar cuando inicia el horario de descanso (bloquear aplicaciones, desactivar notificaciones, activar colores blanco y negro).
•	La aplicación aplica las medidas configuradas.
Postcondiciones	Las medidas quedan activadas al iniciar el horario de descanso.
 
Nombre del caso de uso	RF.32 - Agenda de Tareas y Compromisos Pendientes
Actor(es)	Usuario (Autocuidado)
Precondiciones	El usuario debe tener tareas o compromisos a registrar.
Flujo normal	•	El usuario entra Rest Cycle y accede al Modo Hábitos Saludables.
•	El usuario accede a la opción de Agenda.
•	El usuario accede a la agenda de tareas y compromisos.
•	Registra nombre de tareas o compromisos pendientes.
•	Detalla cada tarea o compromiso, con su fecha.
•	La aplicación guarda la información.
Postcondiciones	La tarea o compromiso queda registrado en la agenda de pendientes.
 
Nombre del caso de uso	RF.33 - Almacenamiento de Información Importante
Actor(es)	Usuario (Autocuidado)
Precondiciones	El usuario tiene información relevante que desea almacenar
Flujo normal	•	El usuario entra Rest Cycle y accede al Modo Hábitos Saludables.
•	El usuario accede a la opción de Agenda.
•	El usuario accede a la agenda de notas importantes.
•	El usuario registra notas, deudas, información del trabajo, entre otras.
•	La aplicación guarda los detalles.
Postcondiciones	La información importante queda almacenada en la aplicación.
 
Nombre del caso de uso	RF.34 - Modificación de Contraseña
Actor(es)	Usuario (General)
Precondiciones	El usuario quiere cambiar su contraseña
Flujo normal	•	El usuario entra Rest Cycle y accede a configuración.
•	El usuario accede a la opción de Cambio de contraseña.
•	La aplicación le pide que ingrese la contraseña existente, la nueva contraseña y la confirme.
•	El usuario ingresa los datos solicitados.
•	La aplicación valida los datos y guarda la nueva contraseña.
Flujo Alternativo	•	El usuario ingresa mal la contraseña actual.
•	La aplicación genera un mensaje de error y pide que vuelva a digitar la contraseña actual.
•	El usuario vuelve a ingresar la contraseña actual.
•	La contraseña nueva no coincide con la confirmación de contraseña
•	La aplicación no realiza cambios en contraseña y manda un mensaje de error.
Postcondiciones	La contraseña es actualizada.
 
Nombre del caso de uso	RF.35 - Cambio de Idioma de la App
Actor(es)	Usuario (General)
Precondiciones	La aplicación debe tener soporte para múltiples idiomas.
Flujo normal	•	El usuario entra Rest Cycle y accede a configuración.
•	El usuario accede a la opción de Idioma deseado.
•	La aplicación le muestra una lista desplegable de todos los idiomas en los que está disponible la aplicación.
•	El usuario selecciona el idioma deseado.
•	La aplicación le pide un reinicio de esta para realizar el cambio.
Postcondiciones	La interfaz de la aplicación queda en el idioma seleccionado.
.  
Nombre del caso de uso	RF.36 - Comunicación con Soporte técnico
Actor(es)	Usuario (General)
Precondiciones	El usuario tiene acceso a soporte técnico o servicio al cliente.
Flujo normal	•	El usuario entra Rest Cycle y accede a configuración.
•	El usuario accede a la opción de Soporte Técnico.
•	Sistema muestra las opciones de contacto para soporte técnico (chat en vivo, correo electrónico, número de teléfono).
•	Usuario selecciona el método de contacto deseado.
•	Sistema abre el canal de comunicación:
Chat en vivo: inicia la sesión de chat en la aplicación.
Correo electrónico: muestra un formulario de solicitud de soporte.
Teléfono: muestra el número de contacto del soporte técnico.
•	Usuario describe el problema técnico que experimenta.
Sistema de Soporte Técnico asiste al usuario:
Chat en vivo: responde en tiempo real.
Correo electrónico: responde tras revisar la solicitud.
Postcondiciones	El usuario se comunica con el soporte técnico.
 
Nombre del caso de uso	RF.37 - Modificación de Información Personal
Actor(es)	Usuario (General)
Precondiciones	El Usuario debe estar autenticado en la aplicación.
El usuario desea modificar su información personal
Flujo normal	•	El usuario entra Rest Cycle y accede a configuración.
•	El usuario accede a la opción de Información personal.
•	El usuario selecciona la información que desea modificar.
•	El usuario modifica la información los datos deseados y los guarda.
•	La aplicación verifica los datos, luego los guarda y cambia la información anterior por la nueva.
•	Sistema muestra un mensaje confirmando que los cambios se han guardado exitosamente.
Postcondiciones	La información personal del usuario ha sido actualizada exitosamente en el sistema.
 
Nombre del caso de uso	RF.38 - Cambio de Tema (Claro/Oscuro)
Actor(es)	Usuario (General)
Precondiciones	El Usuario debe estar autenticado en la aplicación.
. El usuario desea cambiar el tono de la aplicación.
Flujo normal	•	El usuario entra en la aplicación Rest Cycle y accede a la configuración.
•	El usuario accede a la opción de "Tema".
•	El usuario selecciona la opción de cambiar entre "Tema Claro" y "Tema Oscuro".
•	El usuario confirma la selección del tema deseado.
•	La aplicación aplica el tema seleccionado.
•	La aplicación muestra el nuevo tema aplicado exitosamente.
Postcondiciones	El tema de la aplicación se ha cambiado a la opción seleccionada por el usuario.
 
Historias de usuario
NUMERO DE HISTORIA	RF.1
NOMBRE DE LA HISTORIA	Registro de nuevos usuarios
USUARIO	nuevo
PUNTOS ESTIMADOS DE ESFUERZO	6 puntos
DESCRIPCION 	Como usuario nuevo, quiero crear una cuenta para acceder a la aplicación.
OBSERVACIONES	Quiero ingresar con mi cuenta de terceros (Google)
CRITERIOS DE ACEPTACION	Se ha registrado correctamente con el correo (***********@gmail.com) en Rest Cycle. 
El sistema muestra una opción clara para que el usuario acepte los términos y condiciones antes de registrarse.

NUMERO DE HISTORIA	RF.2
NOMBRE DE LA HISTORIA	Términos y condiciones
USUARIO	nuevo
PUNTOS ESTIMADOS DE ESFUERZO	2 puntos
DESCRIPCION 	Como usuario quiero leer los términos y condiciones para aceptarlos antes de registrarme.
OBSERVACIONES	La aceptación de términos y condiciones es necesaria para cumplir con requisitos legales y debe ser un paso obligatorio en el proceso de registro. El usuario no puede completar el registro sin haber aceptado estos términos.
CRITERIOS DE ACEPTACION	El sistema muestra los términos y condiciones al usuario antes de completar el registro.
El sistema solicita la aceptación explícita de los términos y condiciones (casilla de verificación) para poder continuar con el registro.
El usuario no puede finalizar el proceso de registro si no acepta los términos y condiciones.
El sistema almacena el registro de la aceptación de términos por parte del usuario con fecha y hora.

NUMERO DE HISTORIA	RF.3
NOMBRE DE LA HISTORIA	Inicio de sesión
USUARIO	Usuario
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como usuario quiero ingresar a la app para acceder a sus funciones
OBSERVACIONES	El ingreso a mi perfil debe ser sencillo y rápido, usando mis cuentas de terceros.
CRITERIOS DE ACEPTACION	Que los datos correspondan correctamente a los registrados en Rest Cycle

NUMERO DE HISTORIA	RF.4
NOMBRE DE LA HISTORIA	Recuperación de contraseña
USUARIO	Usuario
PUNTOS ESTIMADOS DE ESFUERZO	6 puntos
DESCRIPCION 	Como Usuario Registrado, quiero solicitar un cambio de contraseña si olvido la actual, para poder recuperar el acceso a mi cuenta de forma segura.
OBSERVACIONES	Este proceso de recuperación de contraseña debe permitir a los usuarios ingresar su nombre de usuario y un método de contacto (correo electrónico o número de teléfono) para recibir instrucciones de recuperación. El sistema debe garantizar la seguridad y autenticidad de la solicitud antes de permitir el cambio.
CRITERIOS DE ACEPTACION	El sistema solicita al usuario ingresar su nombre de usuario y correo electrónico o número de teléfono.
Si los datos coinciden con una cuenta registrada, el sistema envía un enlace o código de recuperación a través del método de contacto indicado.
El sistema permite al usuario restablecer la contraseña usando el enlace o código proporcionado.
La nueva contraseña debe cumplir con los criterios de seguridad establecidos.
Si los datos ingresados no son válidos, el sistema muestra un mensaje de error claro al usuario.

NUMERO DE HISTORIA	RF.5
NOMBRE DE LA HISTORIA	Selección de modo de uso
USUARIO	Usuario
PUNTOS ESTIMADOS DE ESFUERZO	2 puntos
DESCRIPCION 	Como usuario, quiero seleccionar el modo de uso de la app para que se ajuste a mis necesidades.
OBSERVACIONES	Este proceso le permite al usuario elegir el modo de uso que más le convenga y beneficie a sus necesidades
CRITERIOS DE ACEPTACION	El usuario deberá elegir un modo para ajustarlo a sus necesidades.

NUMERO DE HISTORIA	RF.6
NOMBRE DE LA HISTORIA	Crear cuenta para hijos
USUARIO	Usuario(padre/tutor)
PUNTOS ESTIMADOS DE ESFUERZO	6 puntos
DESCRIPCION 	Como padre, quiero crearles una cuenta a mis hijos para gestionar el de sus dispositivos móviles
OBSERVACIONES	Este proceso debe permitirle al padre o tutor la creación de cuentas para vincularlas con los dispositivos de los hijos, y así gestionar su uso en el dispositivo móvil.
CRITERIOS DE ACEPTACION	El tutor/padre debe iniciar sesión en los dispositivos de los hijos con las cuentas que fueron creadas para ellos.
El padre/tutor debe ajustar la app para gestionar el uso del dispositivo móvil.

NUMERO DE HISTORIA	RF.7
NOMBRE DE LA HISTORIA	Enlace con cuentas de jóvenes
USUARIO	Tutor/padres
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como padre/tutor quiero enlazar las cuentas de mis hijos a mi cuenta para gestionar y monitorear el uso que le estén dando a los dispositivos 
OBSERVACIONES	Este proceso le permite al padre estar monitoreando el uso de los dispositivos móviles de sus hijos.
CRITERIOS DE ACEPTACION	Los dispositivos deben estar enlazados y conectados a internet para recibir los reportes y estadísticas de uso.

NUMERO DE HISTORIA	RF.8
NOMBRE DE LA HISTORIA	Activar contraseña segura
USUARIO	Padre/tutor
PUNTOS ESTIMADOS DE ESFUERZO	6 puntos
DESCRIPCION 	Como padre/tutor quiero tener una contraseña de seguridad para restringir que mis hijos no eviten las medidas.
OBSERVACIONES	Para que esta función se realice deben tener los permisos de los padres/tutores que le permitan algún cambio o modificación a los dispositivos de los hijos.
CRITERIOS DE ACEPTACION	Para que esta función se ejecute debe estar activa y registrada con su debida contraseña para evitar errores y vulnerabilidad de la app 

NUMERO DE HISTORIA	RF.9
NOMBRE DE LA HISTORIA	Ingreso a control parental con contraseña segura
USUARIO	Tutor/padre
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como tutor/padre quiero que mi hijo ingrese la contraseña segura para entrar al modo control parental.
OBSERVACIONES	Para que esta función el padre debió activar la contraseña segura, esta solo se le pedirá al joven para ingresar al modo control parental o para quitar los bloqueos de apps.
CRITERIOS DE ACEPTACION	Esta función deberá pasar un protocolo de seguridad (sistema anti-atajos) para verificar el manejo adecuado y real del padre que se esté haciendo en ese momento.

NUMERO DE HISTORIA	RF.10
NOMBRE DE LA HISTORIA	Notificaciones de uso
USUARIO	Padre/tutor
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como padre/tutor quiero recibir notificaciones para monitorear alguna alerta o restricción que de la app del mal uso del dispositivo móvil
OBSERVACIONES	Para que esta función este activa deben estar los dispositivos enlazados conectados a wifi para recibir las notificaciones
CRITERIOS DE ACEPTACION	Esta función debe estar activa con su respectiva seguridad para enviar los reportes y notificación al dispositivo del padre/tutor.

NUMERO DE HISTORIA	RF.11
NOMBRE DE LA HISTORIA	Control de tiempo de uso
USUARIO	Padre/tutor
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como padre/tutor quiero recibir notificaciones del tiempo restante de uso en @, para estar informado de cómo están usando sus dispositivos móviles.
OBSERVACIONES	Para que esta función se realice debe conectarse cada x tiempo los dispositivos a wifi para así poder recibir los repórter de uso diarios
CRITERIOS DE ACEPTACION	Esta función le permite al padre/tutor estar monitoreando el uso de los dispositivos de su hijo@ en tiempo real permitiéndole ver que apps usan más y en cual se centran ellos, un informe más detallado de como usan sus dispositivos, permitiéndole realizar la gestión correspondiente y necesaria.

NUMERO DE HISTORIA	RF.12
NOMBRE DE LA HISTORIA	Notificación de tareas
USUARIO	Usuario
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como usuario quiero que la app me permita tener un recordatorio de las pareas pendiente que tenga o me asignen para llevar un control y orden de mi día a día.
OBSERVACIONES	Para que esta función se ejecute debe configurarse y agregarte previamente a las funciones activas de la app.
para que el usuario le integre nuevas tareas o recordatorios al dispositivo vinculado estos deben se deben conectar al wifi para que los cambios se agreguen.
CRITERIOS DE ACEPTACION	Esta función le permitirá al padre/tutor asignarle tareas al hijo@ y recordatorio de sus deberes para mejorar su formación.

NUMERO DE HISTORIA	RF.13
NOMBRE DE LA HISTORIA	Seleccionar apps a gestionar
USUARIO	Usuario 
PUNTOS ESTIMADOS DE ESFUERZO	6 puntos
DESCRIPCION 	Como usuario quiero agregar a la aplicación Rest Cycle las apps que crea necesarias para gestionarles el tiempo de uso que crea necesario
OBSERVACIONES	Para que esta función se ejecute se deben seleccionar las apps que quiera gestionar y darles el tiempo de uso correspondiente.
CRITERIOS DE ACEPTACION	El usuario o padre/tutor podrá agregar las apps que crea necesario gestionar, agregándoles el tiempo de uso que crea necesario para sus hijos o el mismo usuario.
El agregar apps para gestionar se podrá hacer de forma manual o remota con los respectivos permisos de seguridad del padre/tutor para verificar la autenticidad de estos cambios.

NUMERO DE HISTORIA	RF.14
NOMBRE DE LA HISTORIA	Notificar el intento de instalación de apps
USUARIO	Padre/tutor
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como padre/tutor quiero recibir notificaciones para cuando mis hijos intentan instalar apps.
OBSERVACIONES	Para que esta función se ejecute los dispositivos deben estar conectados a wifi y enlazados para permitir la respectiva acción de cambio al dispositivo.
CRITERIOS DE ACEPTACION	Esta función debe estar respaldada con el respectivo permiso del padre/tutor para que el cambio se pueda ejecutar.
El usuario podrá permitirle al joven instalar o no la app.

NUMERO DE HISTORIA	RF.15
NOMBRE DE LA HISTORIA	Permisos de instalación/desinstalación
USUARIO	Padre/tutor
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como padre quiero estar informado de los cambios que intente realizar los hijos para permitírselos o negárselos de forma responsable.
OBSERVACIONES	Para que esta función se ejecute los dispositivos deben estar vinculados y conectados a wifi para otorgar o denegar los cambios que se intenten realizar.
CRITERIOS DE ACEPTACION	Esta función le permite al padre/tutor otorgar y gestionar los cambios que intente hacer su hijo o que el mismo desee realizar para una gestión optima de los dispositivos móviles de sus hijos.

NUMERO DE HISTORIA	RF.16
NOMBRE DE LA HISTORIA	Calendario de tareas
USUARIO	Usuario
PUNTOS ESTIMADOS DE ESFUERZO	6 puntos
DESCRIPCION 	Como usuario quiero tener un calendario para agregar tareas, pendientes o compromisos que tenga en trascurso de los próximos días.
OBSERVACIONES	Para que esta función se ejecute se debe programas manualmente o vía wifi por el enlace de dispositivos el cual el padre le podrá gestionar y asignar tareas a su hijo.
CRITERIOS DE ACEPTACION	Esta función le permite tener un orden y una gestión de su tiempo al usuario o el hijo@ que le permita gestionar el tiempo día a día.

NUMERO DE HISTORIA	RF.17
NOMBRE DE LA HISTORIA	Tiempos de uso programados
USUARIO	Usuario o padre/tutor
PUNTOS ESTIMADOS DE ESFUERZO	6 puntos
DESCRIPCION 	Como padre/tutor quiero programar las apps y asignarle un tiempo de uso para que cuando se exceda ese uso se bloqueen y también asignarle hasta cuándo o que horas se pueden volver a usar de nuevo. Gestionando el tiempo que se usan.
OBSERVACIONES	Para que esta función se ejecute se debe hacer el ajuste manual en el dispositivo a gestionar.
CRITERIOS DE ACEPTACION	Esta función le permitirá al padre/tutor darle un tiempo máximo de uso a las apps que ya tenga en los dispositivos de los hijos y así mismo ajustarle hasta que horas tiene permitido volver a usarlas.
Esta función ira respaldad con su correspondiente sistema de seguridad anti-atajos que estará previamente activada.

NUMERO DE HISTORIA	RF.18
NOMBRE DE LA HISTORIA	Estadísticas de uso
USUARIO	Padre/tutor
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como padre/tutor quiero recibir un reportes diarios, semanales o mensuales de las estadísticas de como mi hijo@ está usando el dispositivo móvil.
OBSERVACIONES	Para que esta función se ejecute debe previamente a ver elegido con qué frecuencia desea recibir estos reportes y por ente estar conectados ambos dispositivos para generar las estadísticas de uso
CRITERIOS DE ACEPTACION	Esta función le permite al padre/tutor recibir el análisis de uso del dispositivo de los hijos para así hacer su debida intervención y gestión del uso de los dispositivos móviles de los hijos.

NUMERO DE HISTORIA	RF.19
NOMBRE DE LA HISTORIA	Control de navegación en dispositivos
USUARIO	Padre/tutor
PUNTOS ESTIMADOS DE ESFUERZO	6 puntos
DESCRIPCION 	Como padre/tutor quiero monitorear las búsquedas en los navegadores que mi hijo@ realice para estar pendiente de si les dan un buen uso a los dispositivos móviles.
OBSERVACIONES	Para que esta función se realice y envié reportes de búsqueda deben conectarse así sea 1 vez a wifi y estar enlazados para enviar el respectivo informe de búsquedas en navegador.
CRITERIOS DE ACEPTACION	Esta función le permite al padre/tutor el control en tiempo real de las búsquedas de navegación de sus hijos, permitiéndole hacer la intervención y gestión necesaria en el menor tiempo posible.

NUMERO DE HISTORIA	RF.20
NOMBRE DE LA HISTORIA	Asignación de tareas para el hijo
USUARIO	Padre/tutor
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como padre quiero signarle tareas o deberes a mi hijo@ para que la app se las notifique en el tras curso del día.
OBSERVACIONES	Para que esta función se ejecute el padre/tutor y el dispositivo del hijo deben conectar sea wifi para que se actualice los cambios generados y por ende realice sus respectivos recordatorios por medio de las notificaciones de Rest cicle
CRITERIOS DE ACEPTACION	Esta función le permite al padre/tutor gestionar el tiempo del hijo y asignarle tareas o deberes en el trascurso del día

NUMERO DE HISTORIA	RF.21
NOMBRE DE LA HISTORIA	Recordatorio de exceso de uso
USUARIO	Usuario
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como usuario quiero que Rest Cycle me dé una notificación de que estoy excediendo el uso de alguna app vinculada para evitar los usos excesivos en las apps del dispositivo.
OBSERVACIONES	Para que esta función se ejecute el usuario deberá elegir las aplicaciones que crea necesarias y ponerles un límite de tiempo para el uso diario de estas.
CRITERIOS DE ACEPTACION	Esta función le permitirá de manera voluntaria al usuario manejar y gestionar los tiempos de uso excesivos de las apps en el día a día.

NUMERO DE HISTORIA	RF.22
NOMBRE DE LA HISTORIA	Notificaciones motivacionales
USUARIO	Usuario
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como usuario quiero que Rest Cycle me de mensajes motivacionales y consejos para mejorar mis hábitos para que en el transcurso del día los pueda implementar
OBSERVACIONES	Para que esta función se ejecute el usuario debe elegir los temas de interés para que Rest Cycle recopile información y le de mensajes de interés al usuario
CRITERIOS DE ACEPTACION	Esta función le permitirá al usuario enfocarse y cambiar los hábitos en el día a día, para crear hábitos saludables.

NUMERO DE HISTORIA	RF.23
NOMBRE DE LA HISTORIA	Notificaciones de interés
USUARIO	Usuario 
PUNTOS ESTIMADOS DE ESFUERZO	2 puntos
DESCRIPCION 	Como usuario quiero que Rest Cycle me notifique de ofertas, productos o actividades de mi interés para implementarlas o darles el interés necesario si se necesitan.
OBSERVACIONES	Para que esta función se ejecute el usuario deberá elegir los temas de su interés al momento que re registe en Rest Cycle para que la app le redirija información de la índole que el usuario elija.
CRITERIOS DE ACEPTACION	Esta función le permite al usuario estar al pendiente de noticias, ofertas o actividades del interés de este que le ayuden a enfocarse más en la implementación de sus hábitos saludables.

NUMERO DE HISTORIA	RF.24
NOMBRE DE LA HISTORIA	Recordatorios de actividades
USUARIO	usuario
PUNTOS ESTIMADOS DE ESFUERZO	4 puntos
DESCRIPCION 	Como usuario quiero que Rest Cycle me recuerde los compromisos y tareas que tengo agendadas en el trascurso del mes para estar gestionando mi tiempo y mis tareas y así optimizar mi desempeño
OBSERVACIONES	Para que esta función se ejecute el usuario deberá tener sus actividades agenden dadas en el calendario para que Rest Cycle gestione las notificaciones y le de los respectivos recordatorios de los compromisos
CRITERIOS DE ACEPTACION	Esta función le permite al usuario gestionar y controlar su tiempo para cumplir con las responsabilidades que este tenga en el transcurso del mes o semana optimizando así su desempeño en responsabilidad.

Numero de historia	RF. 25
Nombre de la historia	Notificación por exceso en el uso de audífonos.
Usuario	Usuario (Autocuidado)
Puntos estimados de esfuerzo	4 puntos
Descripción	Como usuario, quiero que Rest Cycle me notifique cuando lleve mucho tiempo usando audífonos, para evitar el uso excesivo de estos.
Observaciones	El sistema notificara después de cuarenta minutos de uso continuo de audífonos, recomendándole que los deje de usar.
Criterios de aceptación	El usuario permite que Rest Cycle le notifique por uso excesivo de audífonos.
La aplicación notifica, después de llegar al tiempo recomendado de uso de audífonos.

Numero de historia	RF. 26
Nombre de la historia	Enlazar aplicaciones con Rest Cycle para gestionarlas.
Usuario	Usuario (Autocuidado)
Puntos estimados de esfuerzo	6 puntos
Descripción	Como usuario, quiero enlazar Rest Cycle con otras aplicaciones, para gestionarlas y mejorar mis hábitos.
Observaciones	El usuario debe tener la posibilidad de enlazar o desenlazar cualquier aplicación, para gestionarla a su gusto.
Criterios de aceptación	El usuario puede acceder a la gestión de aplicaciones de Rest Cycle.
El usuario puede enlazar cualquier aplicación que crea conveniente.
La aplicación le permite al usuario gestionar algunos aspectos de la aplicación enlazada.

Numero de historia	RF. 27
Nombre de la historia	Tiempo máximo en el uso de aplicaciones.
Usuario	Usuario (Autocuidado)
Puntos estimados de esfuerzo	4 puntos
Descripción	Como usuario, quiero establecer un tiempo máximo de uso diario en aplicaciones específicas, para poder controlar el tiempo que paso en ellas.
Observaciones	El usuario debe tener la posibilidad de ajustar este límite en cualquier momento para adaptarlo a sus necesidades diarias.
Criterios de aceptación	El usuario puede acceder a la gestión de tiempo máximo de uso en la aplicación Rest Cycle.
El usuario puede establecer un tiempo límite diario para cada aplicación enlazada.
La aplicación debe monitorear el tiempo de uso en las aplicaciones enlazadas y notificar al usuario al alcanzar el límite establecido.

Numero de historia	RF. 28
Nombre de la historia	Medidas por límite de tiempo
Usuario	Usuario (Autocuidado)
Puntos estimados de esfuerzo	6 puntos
Descripción	Como usuario, quiero seleccionar las medidas que se tomarán cuando alcance el tiempo máximo de uso en aplicaciones enlazadas, para para controlar y reducir el tiempo que paso en ellas de acuerdo con mis objetivos personales de uso saludable.
Observaciones	El usuario puede configurar diferentes medidas, como bloqueo temporal de la aplicación, notificaciones de advertencia o recordatorios de descanso, según sus preferencias.
Criterios de aceptación	El usuario puede acceder a la configuración de límites de tiempo en la aplicación Rest Cycle.
El usuario puede seleccionar entre diferentes medidas a tomar cuando se alcance el tiempo máximo en aplicaciones enlazadas.
La aplicación debe activar la medida seleccionada automáticamente al llegar al tiempo límite de uso en una aplicación.

Numero de historia	RF. 29
Nombre de la historia	Informes de uso de aplicaciones.
Usuario	Usuario (Autocuidado)
Puntos estimados de esfuerzo	4 puntos
Descripción	Como usuario, quiero ver un informe semanal de mi uso del celular, para mejorar aspectos en el uso del celular.
Observaciones	El usuario tendrá la opción de ver informes del tiempo de uso de aplicaciones en el celular. Además, recibirá mensajes motivacionales para mejorar sus hábitos en el uso del celular.
Criterios de aceptación	El usuario puede acceder a una sección para ver su tiempo en el celular y aplicaciones.
El usuario puede descargar el informe en un pdf.
La aplicación va guardando esos informes para generar los diferentes mensajes motivacionales.
La aplicación muestra una notificación cada domingo para que el usuario vea el informe.

Numero de historia	RF. 30
Nombre de la historia	Activación y modificación de las horas de descanso.
Usuario	Usuario (Autocuidado)
Puntos estimados de esfuerzo	6 puntos
Descripción	Como usuario, quiero activar y modificar horas para descansar, para reducir el uso del celular y descansar mejor.
Observaciones	El usuario tendrá la opción de poner el horario en cualquier hora que desee conveniente descansar, y pueden agregar varios horarios.
Criterios de aceptación	El usuario puede acceder a una sección para agregar horas de descanso.
El usuario puede agregar uno o más horarios de descanso.
La aplicación guarda los horarios establecidos.
La aplicación muestra una notificación de que el horario ha sido agregado exitosamente.
El usuario puede modificar o desactivar horarios que haya establecido.

Numero de historia	RF. 31
Nombre de la historia	Medidas a tomar en la hora de descanso.
Usuario	Usuario (Autocuidado)
Puntos estimados de esfuerzo	4 puntos
Descripción	Como usuario, quiero gestionar las medidas que tomará la aplicación cuando llegue la hora de descanso, para asegurarme de reducir el tiempo de uso del dispositivo y favorecer hábitos saludables.
Observaciones	El usuario debe tener opciones para elegir medidas como bloquear el acceso a ciertas aplicaciones, activar notificaciones de descanso, o reducir el brillo de la pantalla
Criterios de aceptación	El usuario puede acceder a una sección de configuración para la hora de descanso.
El usuario puede seleccionar una o varias medidas para activar durante la hora de descanso.
La aplicación aplica las medidas configuradas automáticamente cuando llega la hora de descanso.
La aplicación muestra una notificación informando que las medidas se han activado.
El usuario puede desactivar o modificar las medidas en cualquier momento desde la configuración.
	
Numero de historia	RF. 32
Nombre de la historia	Agendar tareas pendientes y compromiso.
Usuario	Usuario (Autocuidado)
Puntos estimados de esfuerzo	4 puntos
Descripción	Como usuario, quiero guardar las fechas de tareas o compromisos pendientes, para que la aplicación me recuerde de las fechas importantes.
Observaciones	La función debería permitir al usuario añadir las fechas de compromisos o tareas pendientes con fechas, e irles recordando unos días antes y también el día que se realiza estarles notificando.
Criterios de aceptación	El usuario acceder a una sección de compromiso importantes en la agenda.
Podrá agregar la información del compromiso, con la fecha en que se llevará a cabo este.
El usuario puede ver, modificar y borrar los compromisos que haya creado en caso de que lo crea necesario.
La aplicación muestra un mensaje de que se han guardado correctamente los compromisos.
El sistema le ira notificando del compromiso para que no se le olvide

Numero de historia	RF. 33
Nombre de la historia	Guardar notas de importancia.
Usuario	Usuario (Autocuidado)
Puntos estimados de esfuerzo	4 puntos
Descripción	Como usuario, quiero poder guardar las notas de información importantes, para tener un registro de datos relevantes, que no se me olviden o pierdan.
Observaciones	La función debería permitir al usuario añadir, editar y eliminar notas desde una sección dedicada dentro de la aplicación. Las notas deben almacenarse de manera segura.
Criterios de aceptación	El usuario acceder a una sección de notas en la aplicación.
Podrá agregar notas, agregando texto y guardándolas.
El usuario puede ver, modificar y borrar las notas que ya ha creado.
La aplicación muestra un mensaje de que se han guardado correctamente las notas.

Numero de historia	RF. 34
Nombre de la historia	Modificación de la contraseña actual.
Usuario	Usuario
Puntos estimados de esfuerzo	2 puntos
Descripción	Como usuario, quiero modificar la contraseña de inicio de sesión, para cuando crea que la contraseña ya es anticuada.
Observaciones	La aplicación permitirá el cambio de contraseña desde configuración, esta deberá tener unos caracteres mínimos establecidos por la aplicación.
Criterios de aceptación	El usuario puede acceder a la opción de cambiar la contraseña en la sección de configuración.
El usuario puede cambiar la contraseña sin importar que se parezcan un poco.
La aplicación deja invalida la contraseña anterior.
Un mensaje de confirmación informa al usuario que la contraseña ha sido cambiada exitosamente.

Numero de historia	RF. 35
Nombre de la historia	Cambiar el idioma de la aplicación.
Usuario	Usuario
Puntos estimados de esfuerzo	4 puntos
Descripción	Como usuario, quiero cambiar el idioma predeterminado de la aplicación, para que la interfaz sea más comprensible y se adapte a mi idioma preferido.
Observaciones	La aplicación debería ofrecer soporte para múltiples idiomas (ej. español, inglés, francés) y permitir que el usuario cambie de idioma en cualquier momento desde la configuración.
Criterios de aceptación	El usuario puede acceder a la opción de cambiar idioma en la sección de configuración.
El usuario puede seleccionar un nuevo idioma de una lista de opciones disponibles.
La aplicación cambia el idioma de toda la interfaz una vez seleccionado y guardado.
Un mensaje de confirmación informa al usuario que el idioma ha sido cambiado exitosamente.
Todos los textos y notificaciones de la aplicación se muestran en el idioma seleccionado. 

Numero de historia	RF. 36
Nombre de la historia	Comunicación con el soporte técnico.
Usuario	Usuario
Puntos estimados de esfuerzo	2 puntos
Descripción	Como usuario, quiero comunicarme con soporte técnico, para solucionar algún problema que tenga con la aplicación.
Observaciones	La comunicación con soporte técnico estará disponible en configuraciones, y habrá varios canales de comunicación con soporte técnico.
Criterios de aceptación	El sistema ofrece la opción para comunicarse con soporte técnico en la configuración, después de iniciar sesión.
Se puede comunicar con soporte técnico por medio de un chat en vivo en la aplicación o WhatsApp, por correo electrónico o por teléfono en llamadas.
. El usuario debe tener conexión a internet 

Numero de historia	RF. 37
Nombre de la historia	Modificación de información personal
Usuario	Usuario
Puntos estimados de esfuerzo	4 puntos
Descripción	Como usuario, quiero modificar mis datos personales, para actualizar mis datos cuando sea necesario.
Observaciones	La modificación de información personal estará disponible en configuraciones, y al realizarse cambios el sistema verificará que sean reales si llega a ser necesario.
Criterios de aceptación	El sistema ofrece la opción de modificar la información personal en la configuración, después de iniciar sesión.
Se puede modificar la información personal cuando se desee.
El sistema verificara la información en caso de ser necesario.

Numero de historia	RF. 38
Nombre de la historia	Configuración de tema
Usuario	Usuario
Puntos estimados de esfuerzo	2 puntos
Descripción	Como usuario, quiero cambiar el tema de la aplicación, para tener el tono de mi preferencia.
Observaciones	El cambio de tema estará disponible en configuraciones, e influirá en los colores de la interfaz de la aplicación.
Criterios de aceptación	El sistema ofrece la opción de cambiar de tema en la configuración, después de iniciar sesión.
Se puede seleccionar cualquiera de las dos opciones.
El sistema de adapta de acuerdo con la selección del tema.
El cambio se puede modificar cuando el usuario desee.


5. Requisitos No Funcionales
Usabilidad:
La aplicación mostrará notificaciones en burbujas de chat en un tiempo de 5 a 7 segundos; debe contar con una interfaz fácil de usar, accesible y disponible en múltiples idiomas.
Rendimiento: 
Respuesta a interacciones de usuario en menos de 4 segundos y capacidad para manejar 1,000 usuarios simultáneos sin degradación.
Seguridad: 
Protección robusta de datos mediante cifrado y acceso seguro.
Compatibilidad: 
Compatible con versiones recientes de sistemas operativos y dispositivos iOS y Android.
Mantenimiento: 
Actualizaciones no deben comprometer la integridad de datos; cada nueva versión debe pasar por control de calidad.
Escalabilidad: 
Soporte para un 50% más de usuarios concurrentes sin afectar funcionalidad.



