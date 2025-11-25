# Caso de Estudio Técnico: Rest Cycle
## Gestión de Hábitos Digitales Saludables y Control Parental

En Rest Cycle, la experiencia del usuario se centra en promover un uso saludable de dispositivos móviles y proporcionar herramientas efectivas de control parental. Todo comienza cuando un usuario decide mejorar sus hábitos digitales o supervisar el uso de dispositivos de sus hijos.

---

## Registro de Usuarios

**María Fernanda López**, una madre de familia, decide utilizar Rest Cycle para gestionar el tiempo de pantalla de su hija adolescente. Para comenzar, María realiza su registro proporcionando datos personales como nombre de usuario, correo electrónico, número de teléfono, fecha de nacimiento y contraseña. Estos datos se almacenan en la entidad `Usuarios`, donde a cada usuario se le asigna un identificador único (`ID_Usuario`).

```sql
INSERT INTO Usuarios (ID_Usuario, Nombre_Usuario, Correo_Electronico, Telefono, Fecha_Nacimiento, Contraseña_Hash, Fecha_Registro, Estado_Cuenta)
VALUES (1001, 'maria.lopez', 'maria.lopez@email.com', '+573001234567', '1985-03-15', 'a1b2c3d4e5f6...', '2024-12-15 10:30:00', 'ACTIVA');
```

María también tiene la opción de registrarse usando cuentas de terceros como Google, Microsoft, iCloud o Facebook. Durante el registro, debe aceptar los términos y condiciones de la aplicación, lo cual se registra en la entidad `Aceptacion_Terminos`.

```sql
INSERT INTO Aceptacion_Terminos (ID_Aceptacion, ID_Usuario, Version_Terminos, Fecha_Aceptacion, IP_Address)
VALUES (2001, 1001, 'v1.2', '2024-12-15 10:32:00', '192.168.1.100');
```

---

## Selección de Modo de Uso

Una vez registrada, María debe seleccionar el modo de operación de la aplicación. Rest Cycle ofrece dos modalidades principales que se registran en la entidad `Modos`:

- **Modo Control Parental**: Para supervisar y gestionar el uso de dispositivos de menores de edad
- **Modo Hábitos Saludables**: Para autogestión voluntaria del tiempo de pantalla

María selecciona el **Modo Control Parental**, que se vincula a su cuenta a través de la entidad `Usuario_Modo`, registrando el `ID_Usuario`, `ID_Modo` y la fecha de activación.

```sql
INSERT INTO Usuario_Modo (ID_Usuario_Modo, ID_Usuario, ID_Modo, Fecha_Activacion, Estado)
VALUES (3001, 1001, 1, '2024-12-15 10:35:00', 'ACTIVO');
```

---

## Enlace de Dispositivos y Creación de Cuenta del Menor

María se encuentra ante dos opciones para vincular el dispositivo de su hija Sofía:

### Opción 1: Crear y Enlazar (Sofía no tiene cuenta)

Como Sofía no tiene una cuenta existente en Rest Cycle, María selecciona la opción "Crear cuenta". En un solo proceso:

**1. Crea la cuenta del menor**: Ingresa datos como nombre de usuario, contraseña y sexo de Sofía. Esta información se almacena en la entidad `Usuarios_Menores` con un identificador único (`ID_Usuario_Menor`).

```sql
INSERT INTO Usuarios_Menores (ID_Usuario_Menor, ID_Usuario_Padre, Nombre_Usuario, Nombre_Completo, Fecha_Nacimiento, Sexo, Contraseña_Hash, Fecha_Creacion)
VALUES (2001, 1001, 'sofia.lopez', 'Sofía López', '2008-07-22', 'F', 'x9y8z7w6v5u4...', '2024-12-15 10:40:00');
```

**2. Enlace automático**: Al mismo tiempo, el sistema crea automáticamente el enlace entre ambas cuentas en la entidad `Enlaces_Dispositivos`, que contiene:

```sql
INSERT INTO Enlaces_Dispositivos (ID_Enlace, ID_Usuario, ID_Usuario_Menor, Fecha_Enlace, Estado_Enlace, Tipo_Dispositivo, ID_Dispositivo)
VALUES (4001, 1001, 2001, '2024-12-15 10:40:00', 'ACTIVO', 'Android', 'device_android_001');
```

**3. Contraseña segura**: María configura una contraseña segura que se almacena en la entidad `Contraseñas_Seguras`, necesaria para autorizar cambios en las configuraciones del dispositivo de Sofía.

```sql
INSERT INTO Contraseñas_Seguras (ID_Contraseña_Segura, ID_Usuario, ID_Usuario_Menor, Contraseña_Hash, Fecha_Creacion, Estado)
VALUES (5001, 1001, 2001, 'secure_pass_123...', '2024-12-15 10:42:00', 'ACTIVA');
```

### Opción 2: Solo Enlazar (Si el hijo ya tiene cuenta)

Si Sofía ya tuviera una cuenta existente en Rest Cycle (por ejemplo, si previamente usaba el Modo Hábitos Saludables de forma independiente), María simplemente seleccionaría la opción "Enlazar cuenta existente":

1. Ingresa el nombre de usuario o correo de la cuenta de Sofía
2. El sistema solicita confirmación desde el dispositivo de Sofía
3. Una vez confirmado, se crea el registro en `Enlaces_Dispositivos`
4. La cuenta de Sofía se convierte automáticamente en una cuenta de menor supervisado
5. María configura la contraseña segura para gestionar las restricciones

---

## Gestión de Aplicaciones con Restricciones

María accede a la funcionalidad de **Gestión de Aplicaciones** donde puede ver todas las aplicaciones instaladas en el dispositivo de Sofía. Selecciona las aplicaciones que desea gestionar y configura las restricciones específicas para cada una. Todo esto se almacena en la entidad `Aplicaciones_Gestionadas`:

### Instagram:
```sql
INSERT INTO Aplicaciones_Gestionadas (ID_Aplicacion_Gestionada, ID_Usuario_Menor, Nombre_Aplicacion, Tiempo_Maximo_Diario, Horario_Inicio, Horario_Fin, Accion_Exceso, Estado)
VALUES (6001, 2001, 'Instagram', 60, '14:00:00', '21:00:00', 'BLOQUEO_AUTOMATICO', 'ACTIVA');
```

### TikTok:
```sql
INSERT INTO Aplicaciones_Gestionadas (ID_Aplicacion_Gestionada, ID_Usuario_Menor, Nombre_Aplicacion, Tiempo_Maximo_Diario, Horario_Inicio, Horario_Fin, Accion_Exceso, Estado)
VALUES (6002, 2001, 'TikTok', 45, '15:00:00', '20:00:00', 'NOTIFICACION_BLOQUEO', 'ACTIVA');
```

### YouTube:
```sql
INSERT INTO Aplicaciones_Gestionadas (ID_Aplicacion_Gestionada, ID_Usuario_Menor, Nombre_Aplicacion, Tiempo_Maximo_Diario, Horario_Inicio, Horario_Fin, Accion_Exceso, Estado)
VALUES (6003, 2001, 'YouTube', 90, '00:00:00', '23:59:59', 'SOLO_NOTIFICACION', 'ACTIVA');
```

### WhatsApp:
```sql
INSERT INTO Aplicaciones_Gestionadas (ID_Aplicacion_Gestionada, ID_Usuario_Menor, Nombre_Aplicacion, Tiempo_Maximo_Diario, Horario_Inicio, Horario_Fin, Accion_Exceso, Estado)
VALUES (6004, 2001, 'WhatsApp', 0, '00:00:00', '23:59:59', 'NINGUNA', 'SIN_RESTRICCIONES');
```

María también configura un **horario de bloqueo total** del dispositivo de 22:00 a 07:00 horas, que se registra en la entidad `Bloqueos_Programados`:

```sql
INSERT INTO Bloqueos_Programados (ID_Bloqueo, ID_Usuario_Menor, Hora_Inicio, Hora_Fin, Dias_Semana, Tipo_Bloqueo, Estado)
VALUES (7001, 2001, '22:00:00', '07:00:00', 'L,M,X,J,V,S,D', 'BLOQUEO_TOTAL', 'ACTIVO');
```

Además, activa la opción para recibir notificaciones cuando Sofía intente descargar nuevas aplicaciones, función registrada en la entidad `Permisos_Descarga`:

```sql
INSERT INTO Permisos_Descarga (ID_Permiso, ID_Usuario_Menor, Requiere_Aprobacion, Notificar_Intentos, Estado)
VALUES (8001, 2001, TRUE, TRUE, 'ACTIVO');
```

---

## Sistema de Tareas y Recompensas

María utiliza la funcionalidad de **Tareas para el Niño** para motivar a Sofía. Crea las siguientes tareas en la entidad `Tareas`:

### Tarea 1: Completar tarea de matemáticas
```sql
INSERT INTO Tareas (ID_Tarea, ID_Usuario_Menor, Descripcion, Tiempo_Recompensa, Estado, Fecha_Limite, Creada_Por)
VALUES (9001, 2001, 'Completar tarea de matemáticas', 30, 'PENDIENTE', '2024-12-16 18:00:00', 1001);
```

### Tarea 2: Leer 20 páginas del libro
```sql
INSERT INTO Tareas (ID_Tarea, ID_Usuario_Menor, Descripcion, Tiempo_Recompensa, Estado, Fecha_Limite, Creada_Por)
VALUES (9002, 2001, 'Leer 20 páginas del libro', 45, 'PENDIENTE', '2024-12-17 20:00:00', 1001);
```

### Tarea 3: Ayudar en las tareas del hogar
```sql
INSERT INTO Tareas (ID_Tarea, ID_Usuario_Menor, Descripcion, Tiempo_Recompensa, Estado, Fecha_Limite, Creada_Por)
VALUES (9003, 2001, 'Ayudar en las tareas del hogar', 20, 'PENDIENTE', '2024-12-15 21:00:00', 1001);
```

Cada tarea contiene:
- `ID_Tarea` (identificador único)
- `ID_Usuario_Menor`
- `Descripcion` de la tarea
- `Tiempo_Recompensa` (en minutos)
- `Estado` (pendiente/completada/vencida)
- `Fecha_Limite`

---

## Lista Negra de Sitios Web

María configura una lista negra de sitios web en la entidad `Sitios_Bloqueados` para proteger a Sofía de contenido inapropiado:

```sql
INSERT INTO Sitios_Bloqueados (ID_Sitio_Bloqueado, URL_Sitio, Categoria, ID_Usuario_Menor, Fecha_Agregado, Estado)
VALUES 
(10001, 'www.contenidoadulto.com', 'ADULTOS', 2001, '2024-12-15 11:00:00', 'ACTIVO'),
(10002, 'www.apuestas.com', 'APUESTAS', 2001, '2024-12-15 11:00:00', 'ACTIVO'),
(10003, 'www.violencia.com', 'VIOLENCIA', 2001, '2024-12-15 11:00:00', 'ACTIVO');
```

Los navegadores del dispositivo de Sofía bloquearán automáticamente el acceso a estos sitios.

---

## Sistema de Notificaciones

Durante el uso diario, el **Sistema de Notificaciones** (entidad `Notificaciones`) informa a María sobre diversas actividades:

### Notificación de tiempo excedido:
```sql
INSERT INTO Notificaciones (ID_Notificacion, ID_Usuario, Tipo_Notificacion, Mensaje, Fecha_Creacion, Estado)
VALUES (11001, 1001, 'TIEMPO_EXCEDIDO', 'Sofía ha superado el límite de 1 hora en Instagram', '2024-12-15 15:30:00', 'NO_LEIDA');
```

### Notificación de descarga:
```sql
INSERT INTO Notificaciones (ID_Notificacion, ID_Usuario, Tipo_Notificacion, Mensaje, Fecha_Creacion, Estado)
VALUES (11002, 1001, 'INTENTO_DESCARGA', 'Sofía está intentando instalar Snapchat', '2024-12-15 16:45:00', 'NO_LEIDA');
```

### Notificación de tarea pendiente:
```sql
INSERT INTO Notificaciones (ID_Notificacion, ID_Usuario, Tipo_Notificacion, Mensaje, Fecha_Creacion, Estado)
VALUES (11003, 1001, 'TAREA_PENDIENTE', 'La tarea de matemáticas vence mañana', '2024-12-15 18:00:00', 'NO_LEIDA');
```

### Notificación de ubicación:
```sql
INSERT INTO Notificaciones (ID_Notificacion, ID_Usuario, Tipo_Notificacion, Mensaje, Fecha_Creacion, Estado)
VALUES (11004, 1001, 'UBICACION', 'Sofía ha llegado a la escuela', '2024-12-15 07:30:00', 'LEIDA');
```

Cada notificación se registra con:
- `ID_Notificacion`
- `Tipo_Notificacion`
- `ID_Usuario` (destinatario)
- `Mensaje`
- `Fecha_Creacion`
- `Estado` (leída/no leída)

---

## Seguimiento de Ubicación

María activa el sistema de ubicación GPS en la entidad `Ubicaciones_GPS`, que registra:

```sql
INSERT INTO Ubicaciones_GPS (ID_Ubicacion, ID_Usuario_Menor, Latitud, Longitud, Fecha_Registro, Descripcion_Lugar, Precisión)
VALUES 
(12001, 2001, 4.6097100, -74.0817500, '2024-12-15 07:30:00', 'Colegio San Patricio', 5.0),
(12002, 2001, 4.6200000, -74.0750000, '2024-12-15 15:45:00', 'Casa', 3.0),
(12003, 2001, 4.6000000, -74.0800000, '2024-12-15 18:20:00', 'Centro Comercial', 8.0);
```

Esto le permite saber dónde está Sofía en tiempo real y revisar el historial de ubicaciones.

---

## Estadísticas de Uso

María puede consultar informes detallados en la entidad `Estadisticas_Uso` que muestran:

```sql
INSERT INTO Estadisticas_Uso (ID_Estadistica, ID_Usuario_Menor, Fecha, Aplicacion, Tiempo_Uso_Minutos, Numero_Accesos, Horario_Pico)
VALUES 
(13001, 2001, '2024-12-15', 'Instagram', 75, 12, '16:00-17:00'),
(13002, 2001, '2024-12-15', 'TikTok', 45, 8, '15:30-16:30'),
(13003, 2001, '2024-12-15', 'YouTube', 90, 5, '19:00-20:00'),
(13004, 2001, '2024-12-15', 'WhatsApp', 120, 25, '15:00-22:00');
```

Esta información se presenta con gráficos y se actualiza en tiempo real, permitiéndole a María tomar decisiones informadas sobre las restricciones.

---

## Modo Hábitos Saludables - Caso de Juan

**Juan Pérez**, un estudiante universitario, también se registra en Rest Cycle pero selecciona el **Modo Hábitos Saludables** para mejorar su productividad.

### Registro de Juan:
```sql
INSERT INTO Usuarios (ID_Usuario, Nombre_Usuario, Correo_Electronico, Telefono, Fecha_Nacimiento, Contraseña_Hash, Fecha_Registro, Estado_Cuenta)
VALUES (1002, 'juan.perez', 'juan.perez@universidad.edu', '+573009876543', '2002-05-10', 'h9i8j7k6l5m4...', '2024-12-15 14:00:00', 'ACTIVA');

INSERT INTO Usuario_Modo (ID_Usuario_Modo, ID_Usuario, ID_Modo, Fecha_Activacion, Estado)
VALUES (3002, 1002, 2, '2024-12-15 14:05:00', 'ACTIVO');
```

### Autogestión de Tiempo

Juan configura voluntariamente sus propios límites en la entidad `Limites_Voluntarios`:

```sql
INSERT INTO Limites_Voluntarios (ID_Limite, ID_Usuario, Aplicacion, Tiempo_Maximo_Diario, Horario_Inicio, Horario_Fin, Accion_Exceso, Estado)
VALUES 
(14001, 1002, 'Instagram', 60, '18:00:00', '23:00:00', 'BLOQUEO_TEMPORAL', 'ACTIVO'),
(14002, 1002, 'TikTok', 30, '19:00:00', '22:00:00', 'NOTIFICACION', 'ACTIVO'),
(14003, 1002, 'Facebook', 45, '17:00:00', '23:00:00', 'BLOQUEO_TEMPORAL', 'ACTIVO');
```

### Descanso obligatorio de 22:00 a 06:00:
```sql
INSERT INTO Bloqueos_Programados (ID_Bloqueo, ID_Usuario, Hora_Inicio, Hora_Fin, Dias_Semana, Tipo_Bloqueo, Estado)
VALUES (7002, 1002, '22:00:00', '06:00:00', 'L,M,X,J,V,S,D', 'BLOQUEO_TOTAL', 'ACTIVO');
```

### Bloqueo de aplicaciones de entretenimiento durante horario de estudio (09:00-17:00):
```sql
INSERT INTO Bloqueos_Programados (ID_Bloqueo, ID_Usuario, Hora_Inicio, Hora_Fin, Dias_Semana, Tipo_Bloqueo, Estado, Aplicaciones_Afectadas)
VALUES (7003, 1002, '09:00:00', '17:00:00', 'L,M,X,J,V', 'BLOQUEO_SELECTIVO', 'ACTIVO', 'Instagram,TikTok,Facebook,YouTube');
```

### Recordatorios de Descanso

Juan activa el horario personalizado de descanso en la entidad `Horarios_Descanso`, configurando:

```sql
INSERT INTO Horarios_Descanso (ID_Horario_Descanso, ID_Usuario, Hora_Inicio, Hora_Fin, Acciones_Descanso, Estado)
VALUES (15001, 1002, '22:00:00', '06:00:00', 'ESCALA_GRISES,SILENCIO,DESACTIVAR_NOTIFICACIONES', 'ACTIVO');
```

### Gestión de Pendientes

Juan utiliza la funcionalidad de **Listado de Pendientes**, conectando Rest Cycle con su calendario. En la entidad `Tareas_Usuario` registra:

```sql
INSERT INTO Tareas_Usuario (ID_Tarea_Usuario, ID_Usuario, Descripcion, Fecha_Limite, Prioridad, Estado, Tipo_Tarea)
VALUES 
(16001, 1002, 'Examen de Cálculo III', '2024-12-20 08:00:00', 'ALTA', 'PENDIENTE', 'ACADEMICA'),
(16002, 1002, 'Entrega proyecto de programación', '2024-12-18 23:59:00', 'ALTA', 'PENDIENTE', 'ACADEMICA'),
(16003, 1002, 'Reunión con el equipo de investigación', '2024-12-17 15:00:00', 'MEDIA', 'PENDIENTE', 'PERSONAL');
```

También guarda información importante en la entidad `Notas`:

```sql
INSERT INTO Notas (ID_Nota, ID_Usuario, Titulo, Contenido, Fecha_Creacion, Categoria, Estado)
VALUES 
(17001, 1002, 'Recordatorio de pago', 'Pagar matrícula universidad - $2,500,000', '2024-12-15 14:30:00', 'FINANZAS', 'ACTIVA'),
(17002, 1002, 'Información trabajo', 'Empresa: TechCorp, Contacto: María García, Tel: 3001234567', '2024-12-15 14:35:00', 'TRABAJO', 'ACTIVA'),
(17003, 1002, 'Cuentas pendientes', 'Netflix: $15,000, Spotify: $20,000, Gym: $80,000', '2024-12-15 14:40:00', 'FINANZAS', 'ACTIVA');
```

### Notificaciones Motivacionales

El sistema envía a Juan notificaciones motivacionales desde la entidad `Notificaciones_Motivacionales`:

```sql
INSERT INTO Notificaciones_Motivacionales (ID_Notificacion_Mot, ID_Usuario, Mensaje, Tipo_Mensaje, Fecha_Envio, Estado)
VALUES 
(18001, 1002, '¡Llevas 3 días cumpliendo tus metas de tiempo de pantalla!', 'PROGRESO', '2024-12-15 19:00:00', 'ENVIADA'),
(18002, 1002, 'Has reducido tu uso de redes sociales en 40% esta semana', 'LOGRO', '2024-12-15 20:00:00', 'ENVIADA'),
(18003, 1002, 'Recuerda tomar un descanso de 5 minutos cada hora', 'RECORDATORIO', '2024-12-15 16:00:00', 'ENVIADA');
```

### Temas de Interés

Juan configura sus temas de interés en la entidad `Temas_Interes`:

```sql
INSERT INTO Temas_Interes (ID_Tema, ID_Usuario, Nombre_Tema, Estado, Fecha_Agregado)
VALUES 
(19001, 1002, 'Productividad', 'ACTIVO', '2024-12-15 14:10:00'),
(19002, 1002, 'Desarrollo Personal', 'ACTIVO', '2024-12-15 14:10:00'),
(19003, 1002, 'Tecnología', 'ACTIVO', '2024-12-15 14:10:00'),
(19004, 1002, 'Ejercicio', 'ACTIVO', '2024-12-15 14:10:00');
```

El sistema le envía contenido relevante y sugerencias basadas en estos intereses.

---

## Gestión de Membresías

Tanto María como Juan tienen acceso a la funcionalidad básica gratuita. La entidad `Membresias` registra:

### María - Plan Gratuito:
```sql
INSERT INTO Membresias (ID_Membresia, ID_Usuario, Tipo_Plan, Fecha_Inicio, Fecha_Vencimiento, Estado, Caracteristicas_Incluidas)
VALUES (20001, 1001, 'GRATUITO', '2024-12-15 10:30:00', NULL, 'ACTIVA', 'Control básico, 2 dispositivos, Reportes semanales');
```

### Juan - Plan Gratuito:
```sql
INSERT INTO Membresias (ID_Membresia, ID_Usuario, Tipo_Plan, Fecha_Inicio, Fecha_Vencimiento, Estado, Caracteristicas_Incluidas)
VALUES (20002, 1002, 'GRATUITO', '2024-12-15 14:00:00', NULL, 'ACTIVA', 'Autogestión básica, 1 dispositivo, Reportes mensuales');
```

María decide actualizar a **plan Premium** para acceder a funciones avanzadas como:
- Seguimiento GPS sin límites
- Informes detallados con exportación
- Bloqueo remoto instantáneo
- Soporte prioritario

### Actualización a Premium:
```sql
UPDATE Membresias 
SET Tipo_Plan = 'PREMIUM', 
    Fecha_Vencimiento = '2025-12-15 10:30:00',
    Caracteristicas_Incluidas = 'Control avanzado, 5 dispositivos, GPS ilimitado, Reportes detallados, Bloqueo remoto, Soporte prioritario'
WHERE ID_Membresia = 20001;

INSERT INTO Pagos (ID_Pago, ID_Usuario, ID_Membresia, Monto, Moneda, Metodo_Pago, Estado_Pago, Fecha_Pago, Transaccion_ID)
VALUES (21001, 1001, 20001, 9.99, 'USD', 'TARJETA_CREDITO', 'APROBADO', '2024-12-15 11:00:00', 'TXN_123456789');
```

---

## Operaciones del Administrador

Detrás de escena, el **Administrador del Sistema** (entidad `Administradores`) realiza operaciones cruciales:

### Gestión de cuentas:
```sql
INSERT INTO Administradores (ID_Administrador, Nombre_Usuario, Correo_Electronico, Rol, Fecha_Creacion, Estado)
VALUES (30001, 'admin.principal', 'admin@restcycle.app', 'SUPER_ADMIN', '2024-01-01 00:00:00', 'ACTIVO');

-- Revisa y elimina cuentas con intentos de fraude
INSERT INTO Intentos_Fraude (ID_Intento, ID_Usuario, Tipo_Fraude, Descripcion, Fecha_Intento, IP_Address, Estado)
VALUES (22001, 9999, 'MULTIPLE_REGISTRO', 'Intento de registro con múltiples cuentas', '2024-12-15 12:00:00', '192.168.1.999', 'DETECTADO');

UPDATE Usuarios SET Estado_Cuenta = 'SUSPENDIDA' WHERE ID_Usuario = 9999;
```

### Gestión de quejas:
```sql
INSERT INTO Quejas_Reclamos (ID_Queja, ID_Usuario, Tipo_Queja, Descripcion, Fecha_Queja, Estado, Prioridad, Asignado_A)
VALUES (23001, 1001, 'FUNCIONALIDAD', 'La aplicación no bloquea correctamente TikTok', '2024-12-15 16:30:00', 'EN_REVISION', 'ALTA', 30001);
```

### Administración de pagos:
```sql
-- Supervisa planes premium activos y vencidos
SELECT COUNT(*) as Planes_Activos FROM Membresias WHERE Tipo_Plan = 'PREMIUM' AND Estado = 'ACTIVA';
SELECT COUNT(*) as Planes_Vencidos FROM Membresias WHERE Fecha_Vencimiento < NOW() AND Estado = 'ACTIVA';
```

### Gestión de publicidad:
```sql
INSERT INTO Contratos_Publicidad (ID_Contrato, Empresa, Tipo_Publicidad, Monto, Fecha_Inicio, Fecha_Fin, Estado)
VALUES (24001, 'TechCorp Solutions', 'BANNER_APP', 5000.00, '2024-12-01 00:00:00', '2025-02-01 00:00:00', 'ACTIVO');
```

### Ofertas y promociones:
```sql
INSERT INTO Ofertas_Usuarios (ID_Oferta, ID_Usuario, Tipo_Oferta, Descripcion, Descuento, Fecha_Validez, Estado)
VALUES (25001, 1001, 'DESCUENTO_PREMIUM', '50% de descuento en primer mes Premium', 50, '2024-12-31 23:59:59', 'DISPONIBLE');
```

---

## Configuración Personal

Todos los usuarios pueden acceder a **Configuraciones** para:

### Modificar contraseña (registrado en `Historial_Contraseñas`):
```sql
INSERT INTO Historial_Contraseñas (ID_Historial, ID_Usuario, Contraseña_Hash_Anterior, Contraseña_Hash_Nueva, Fecha_Cambio, IP_Address)
VALUES (26001, 1001, 'a1b2c3d4e5f6...', 'new_secure_pass_789...', '2024-12-15 17:00:00', '192.168.1.100');
```

### Cambiar idioma de la aplicación (almacenado en `Preferencias_Usuario`):
```sql
INSERT INTO Preferencias_Usuario (ID_Preferencia, ID_Usuario, Tipo_Preferencia, Valor, Fecha_Modificacion)
VALUES (27001, 1001, 'IDIOMA', 'ES', '2024-12-15 17:05:00'),
       (27002, 1002, 'IDIOMA', 'EN', '2024-12-15 14:15:00');
```

### Contactar soporte técnico (tickets en `Soporte_Tecnico`):
```sql
INSERT INTO Soporte_Tecnico (ID_Ticket, ID_Usuario, Asunto, Descripcion, Prioridad, Estado, Fecha_Creacion)
VALUES (28001, 1001, 'Problema con bloqueo de Instagram', 'La aplicación no está bloqueando Instagram según la configuración establecida', 'ALTA', 'ABIERTO', '2024-12-15 16:30:00');
```

### Modificar información personal:
```sql
UPDATE Usuarios 
SET Telefono = '+573001234568', 
    Correo_Electronico = 'maria.lopez.nuevo@email.com'
WHERE ID_Usuario = 1001;
```

### Cambiar entre tema oscuro y claro:
```sql
INSERT INTO Preferencias_Usuario (ID_Preferencia, ID_Usuario, Tipo_Preferencia, Valor, Fecha_Modificacion)
VALUES (27003, 1001, 'TEMA', 'OSCURO', '2024-12-15 17:10:00'),
       (27004, 1002, 'TEMA', 'CLARO', '2024-12-15 14:20:00');
```

---

## Flujo de Datos en Tiempo Real

### Proceso de Monitoreo Continuo:

1. **Recolección de Datos**: El dispositivo de Sofía envía datos de uso cada 5 minutos:
```json
{
  "device_id": "device_android_001",
  "user_id": 2001,
  "timestamp": "2024-12-15T15:30:00Z",
  "apps_usage": [
    {"app": "Instagram", "time_spent": 25, "launches": 3},
    {"app": "TikTok", "time_spent": 15, "launches": 2},
    {"app": "WhatsApp", "time_spent": 40, "launches": 8}
  ],
  "location": {
    "latitude": 4.6097100,
    "longitude": -74.0817500,
    "accuracy": 5.0
  }
}
```

2. **Procesamiento de Restricciones**: El sistema verifica automáticamente:
```sql
-- Verificar si Instagram ha excedido el límite
SELECT SUM(Tiempo_Uso_Minutos) as Tiempo_Total
FROM Estadisticas_Uso 
WHERE ID_Usuario_Menor = 2001 
  AND Aplicacion = 'Instagram' 
  AND DATE(Fecha) = CURDATE();

-- Si excede el límite (60 minutos), activar bloqueo
IF Tiempo_Total > 60 THEN
  INSERT INTO Notificaciones (ID_Usuario, Tipo_Notificacion, Mensaje, Fecha_Creacion, Estado)
  VALUES (1001, 'TIEMPO_EXCEDIDO', 'Sofía ha superado el límite de Instagram', NOW(), 'NO_LEIDA');
  
  -- Enviar comando de bloqueo al dispositivo
  INSERT INTO Comandos_Dispositivo (ID_Comando, ID_Usuario_Menor, Tipo_Comando, Parametros, Estado)
  VALUES (29001, 2001, 'BLOQUEAR_APP', '{"app": "Instagram", "duration": 1440}', 'PENDIENTE');
END IF;
```

3. **Actualización de Estadísticas**: Los datos se procesan y almacenan:
```sql
INSERT INTO Estadisticas_Uso (ID_Usuario_Menor, Fecha, Aplicacion, Tiempo_Uso_Minutos, Numero_Accesos, Horario_Pico)
VALUES (2001, '2024-12-15', 'Instagram', 25, 3, '15:00-16:00');
```

---

## Conclusión

Rest Cycle demuestra cómo una aplicación bien estructurada puede servir dos propósitos fundamentales: ayudar a los padres a supervisar y guiar el uso tecnológico de sus hijos de manera efectiva, y empoderar a los usuarios individuales para desarrollar hábitos digitales más saludables.

Mediante un sistema integral de entidades relacionadas, la aplicación gestiona usuarios, restricciones, notificaciones, estadísticas, tareas, ubicaciones y membresías, ofreciendo una experiencia completa y personalizada que se adapta a las necesidades específicas de cada tipo de usuario.

**El caso de María y Sofía** ilustra el poder del control parental responsable, con configuraciones granulares por aplicación, sistema de recompensas por tareas completadas, y monitoreo de ubicación en tiempo real.

**El caso de Juan** demuestra cómo la autogestión voluntaria puede mejorar la productividad y el bienestar digital, con límites personalizados, horarios de descanso, y gestión de tareas académicas.

Ambos modos operan con la misma infraestructura técnica pero con enfoques adaptados a contextos diferentes, haciendo de Rest Cycle una solución versátil y completa para la gestión del tiempo de pantalla en la era digital.

La arquitectura de base de datos diseñada permite escalabilidad, integridad de datos, y procesamiento en tiempo real, garantizando que las restricciones se apliquen inmediatamente y que los padres reciban información actualizada sobre el comportamiento digital de sus hijos.

