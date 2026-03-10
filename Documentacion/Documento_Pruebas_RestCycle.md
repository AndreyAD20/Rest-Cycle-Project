# Documento de Pruebas y Resultados de Funcionamiento

**Proyecto:** Rest-Cycle
**Plataforma:** Android (Kotlin/Jetpack Compose)
**Fecha:** 02 de Marzo de 2026

Este documento detalla los casos de prueba manuales ejecutados sobre los distintos módulos de la aplicación Rest-Cycle, junto con los resultados de funcionamiento esperados y obtenidos durante la validación del sistema.

---

## 1. Módulo de Autenticación (`features.auth`)

### 1.1. Registro de Usuario (`RegistroComposeActivity`)

- **Descripción:** Validar que un usuario nuevo pueda registrarse correctamente llenando todos los campos requeridos (Nombre, Apellido, Fecha de Nacimiento, Email, Contraseña).
- **Pasos de Prueba:**
  1. Abrir la aplicación y navegar a la pantalla de registro.
  2. Ingresar datos válidos en todos los campos.
  3. Presionar el botón de "Registrarse".
- **Resultado Esperado:** El usuario es creado en Supabase/Base de datos, se envía un correo de verificación si aplica, y se redirige a la pantalla de inicio o login.
- **Resultado Obtenido:** **[EXITOSO]** - Los datos se guardan correctamente y el usuario es redirigido sin errores.

### 1.2. Inicio de Sesión (`LoginComposeActivity`)

- **Descripción:** Validar que un usuario existente pueda acceder a la aplicación con sus credenciales.
- **Pasos de Prueba:**
  1. Ingresar un email y contraseña registrados.
  2. Presionar el botón de "Ingresar".
- **Resultado Esperado:** Autenticación exitosa y redirección a la pantalla de Inicio (`InicioComposeActivity`).
- **Resultado Obtenido:** **[EXITOSO]** - El sistema valida las credenciales y carga el perfil del usuario.

### 1.3. Recuperación de Contraseña y Verificación

- **Descripción:** Validar el flujo de recuperación de contraseña (`OlvidoContrasenaActivity`, `VerificacionCodigoActivity`, `CambioContrasenaActivity`).
- **Pasos de Prueba:**
  1. Solicitar recuperación ingresando el correo.
  2. Ingresar el código recibido.
  3. Establecer una nueva contraseña ("Cambiar Contraseña").
- **Resultado Esperado:** Se actualiza correctamente la contraseña hasheada en la base de datos y el usuario puede iniciar sesión con la nueva clave.
- **Resultado Obtenido:** **[EXITOSO]** - El código es validado y la actualización se refleja en Supabase.

---

## 2. Módulo Principal (`features.home`)

### 2.1. Pantalla de Inicio (`InicioComposeActivity`)

- **Descripción:** Validar que la pantalla principal cargue correctamente la información resumida del usuario tras iniciar sesión.
- **Pasos de Prueba:**
  1. Iniciar sesión exitosamente.
  2. Observar la pantalla principal.
- **Resultado Esperado:** La UI muestra mensajes de bienvenida y los componentes principales sin bloqueos.
- **Resultado Obtenido:** **[EXITOSO]** - La interfaz se renderiza correctamente con Jetpack Compose.

### 2.2. Perfil de Usuario (`PerfilComposeActivity`)

- **Descripción:** Validar la visualización y edición de los datos del perfil (Nombre, Apellido, Email, Cumpleaños, Contraseña).
- **Pasos de Prueba:**
  1. Navegar a la sección de Perfil.
  2. Modificar un campo (ej. Apellido) y guardar los cambios.
- **Resultado Esperado:** Los datos se actualizan en tiempo real en la base de datos de Supabase y la UI refleja los cambios inmediatamente.
- **Resultado Obtenido:** **[EXITOSO]** - La base de datos es actualizada y los cambios persisten tras reiniciar la app.

---

## 3. Módulo de Hábitos y Estadísticas (`features.habits` / `EstadisticasRepository`)

### 3.1. Visualización de Estadísticas (`EstadisticasComposeActivity`)

- **Descripción:** Validar que los gráficos de uso y estadísticas de hábitos se generen con datos reales.
- **Pasos de Prueba:**
  1. Navegar a la sección de Estadísticas.
  2. Revisar los gráficos Semanales y de Calendario (`GraficoBarrasSemanal`, `GraficoSemanasCalendario`).
- **Resultado Esperado:** Los gráficos se dibujan correctamente utilizando los datos de uso registrados del usuario.
- **Resultado Obtenido:** **[EXITOSO]** - Las barras y el calendario representan fielmente el progreso del usuario.

### 3.2. Sincronización y Persistencia de Estadísticas (`EstadisticasRepository`)

- **Descripción:** Validar que las estadísticas de uso se sincronicen correctamente con la base de datos remota.
- **Pasos de Prueba:**
  1. Utilizar un dispositivo con la aplicación por un periodo (ej. 10 minutos).
  2. Forzar la sincronización o esperar el intervalo de actualización automático.
  3. Verificar en la base de datos remota los valores insertados.
- **Resultado Esperado:** Los datos de tiempo de uso se actualizan o insertan correctamente en la base de datos vinculada al usuario y dispositivo actual sin duplicaciones.
- **Resultado Obtenido:** **[EXITOSO]** - El repositorio maneja eficientemente las actualizaciones locales hacia el servidor.

---

## 4. Módulo de Control Parental, Bloqueo y Monitorización (`features.parental` / `features.blocking` / `AppMonitorService`)

### 4.1. Configuración de Restricciones

- **Descripción:** Verificar el correcto funcionamiento del bloqueo de aplicaciones y límites de tiempo mediante la interfaz.
- **Pasos de Prueba:**
  1. Activar el control parental / modos de bloqueo.
  2. Seleccionar aplicaciones específicas para restringir.
- **Resultado Esperado:** Las reglas se guardan correctamente y se reflejan en la configuración del dispositivo activo.
- **Resultado Obtenido:** **[EXITOSO]** - Las preferencias se guardan y sincronizan adecuadamente.

### 4.2. Monitorización en Segundo Plano (`AppMonitorService`)

- **Descripción:** Validar que el servicio en segundo plano detecta el uso de aplicaciones restringidas y aplica el bloqueo correspondiente.
- **Pasos de Prueba:**
  1. Configurar una restricción de tiempo sobre una aplicación objetivo (ej. YouTube, 1 minuto).
  2. Abrir la aplicación objetivo y mantenerla en uso.
  3. Esperar a que el tiempo de uso supere el límite.
- **Resultado Esperado:** El `AppMonitorService` detecta la superación del límite y sobrepone una pantalla de bloqueo, impidiendo el uso continuo de la aplicación.
- **Resultado Obtenido:** **[EXITOSO]** - El servicio detecta los cambios de aplicación activa (UsageStats) con precisión y lanza el Intent de bloqueo a tiempo, registrando el evento correctamente.

---

## 🎯 Conclusión General

**Estado del Sistema:** **ESTABLE**
Todos los flujos críticos de la aplicación `Rest-Cycle` (Autenticación, Gestión de Perfil, Estadísticas y Restricciones) han sido sometidos a pruebas de integración y UI, respondiendo de manera exitosa a los requerimientos funcionales establecidos. No se detectaron cierres inesperados (crashes) ni pérdidas de datos durante las pruebas realizadas.
