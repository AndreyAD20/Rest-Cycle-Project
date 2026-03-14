# Rest Cycle - Módulos de la Aplicación

> **Versión:** 2.1  
> **Fecha:** 12 de Febrero de 2026

---

## Índice

1. [Módulos Implementados](#módulos-implementados)
2. [Módulos Pendientes](#módulos-pendientes)
3. [Resumen de Estado](#resumen-de-estado)

---

## Módulos Implementados

### 1. Autenticación y Seguridad

| Submódulo                  | Estado      | Requisitos |
| -------------------------- | ----------- | ---------- |
| Registro de usuario        | ✅ Completo | RF.1, RF.2 |
| Inicio de sesión           | ✅ Completo | RF.3       |
| Recuperación de contraseña | ✅ Completo | RF.4       |
| Verificación por código    | ✅ Completo | RF.1       |
| Cambio de contraseña       | ✅ Completo | RF.55      |

**Archivos**: LoginComposeActivity, RegistroComposeActivity, OlvidoContrasenaActivity, CodigoRecuperacionActivity, CambioContrasenaActivity, VerificacionCodigoActivity

---

### 2. Gestión de Hábitos Saludables

| Submódulo                   | Estado      | Requisitos          |
| --------------------------- | ----------- | ------------------- |
| Estadísticas de uso de apps | ✅ Completo | RF.10, RF.24, RF.38, RF.63 |
| Selector de modo de uso     | ✅ Completo | RF.6                       |
| Perfil de usuario           | ✅ Parcial  | RF.58, RF.65               |

**Archivos**: EstadisticasComposeActivity, InicioComposeActivity, PerfilComposeActivity

**Características**:

- Gráficos de barras animados con top 5 y top 10 apps (RF.63)
- Filtros: Diario, Semanal, Mensual (RF.63)
- Menú lateral de navegación (RF.65)
- Permiso PACKAGE_USAGE_STATS

---

### 3. Herramientas de Productividad

| Submódulo             | Estado      | Requisitos   |
| --------------------- | ----------- | ------------ |
| Notas personales      | ✅ Completo | RF.43, RF.62        |
| Calendario de eventos | ✅ Completo | RF.41, RF.42, RF.64 |
| Gestión de tareas     | ✅ Completo | RF.11, RF.12 |
| Horarios de descanso  | ✅ Completo | RF.39, RF.40 |
| Bloqueo de apps (UI)  | ⚠️ Parcial  | RF.18, RF.22 |

**Archivos**: NotasComposeActivity, CalendarioComposeActivity, TareasComposeActivity, HoraDescansoComposeActivity, BloqueoAppsComposeActivity

**Características**:

- Notas con colores, favoritos, búsqueda y confirmación de eliminación (RF.62)
- Eventos con ubicación en Google Maps (RF.64)
- Tareas con fechas
- Horarios por días de la semana

---

### 4. Control Parental (Básico)

| Submódulo                  | Estado      | Requisitos |
| -------------------------- | ----------- | ---------- |
| Creación de cuenta de hijo | ✅ Completo | RF.7, RF.9 |
| Gestión de hijos           | ⚠️ Parcial  | RF.8       |

**Archivos**: CrearHijoActivity, GestionHijosComposeActivity

**Limitaciones**: Solo permite crear cuentas, falta supervisión y control real.

---

## Módulos Pendientes

### 5. Sistema de Notificaciones

| Submódulo                         | Prioridad | Requisitos          |
| --------------------------------- | --------- | ------------------- |
| Notificaciones de uso excesivo    | 🔴 Alta   | RF.14, RF.15, RF.29 |
| Notificaciones de tareas          | 🟡 Media  | RF.16, RF.33        |
| Recomendaciones al padre          | 🟡 Media  | RF.17               |
| Notificaciones motivacionales     | 🟢 Baja   | RF.30, RF.31        |
| Personalización de notificaciones | 🟡 Media  | RF.32               |
| Alerta de auriculares             | 🟢 Baja   | RF.34               |

---

### 6. Control Parental Avanzado

| Submódulo                      | Prioridad | Requisitos   |
| ------------------------------ | --------- | ------------ |
| Dashboard de supervisión       | 🔴 Alta   | RF.8, RF.24  |
| Tiempo adicional por tareas    | 🟡 Media  | RF.13        |
| Control de instalación de apps | 🟡 Media  | RF.19, RF.20 |
| Bloqueo remoto de apps         | 🔴 Alta   | RF.18, RF.22 |
| Informes de uso del hijo       | 🟡 Media  | RF.24, RF.38 |

**Características pendientes**:

- Visualización de actividad del hijo en tiempo real
- Sistema de recompensas
- Aprobación/rechazo de instalaciones
- Enforcement de límites de tiempo

---

### 7. Control de Navegación Web

| Submódulo               | Prioridad | Requisitos |
| ----------------------- | --------- | ---------- |
| Historial de navegación | 🟡 Media  | RF.25      |
| Lista negra de sitios   | 🟡 Media  | RF.26      |

---

### 8. Geolocalización

| Submódulo              | Prioridad | Requisitos |
| ---------------------- | --------- | ---------- |
| Activación de GPS      | 🟢 Baja   | RF.27      |
| Reporte de ubicaciones | 🟡 Media  | RF.28      |

**Características pendientes**:

- Tracking en background
- Historial de ubicaciones
- Geofencing

---

### 9. Sistema de Membresías

| Submódulo             | Prioridad | Requisitos |
| --------------------- | --------- | ---------- |
| Selección de plan     | 🟡 Media  | RF.5       |
| Pagos y suscripciones | 🟡 Media  | RF.5       |
| Features premium      | 🟡 Media  | RF.5       |

---

### 10. Panel de Administrador

| Submódulo                  | Prioridad | Requisitos   |
| -------------------------- | --------- | ------------ |
| Gestión de usuarios        | 🟡 Media  | RF.46, RF.50 |
| Gestión de cuentas premium | 🟡 Media  | RF.49, RF.51 |
| Quejas y reclamos          | 🟡 Media  | RF.48        |
| Reportes y estadísticas    | 🟡 Media  | RF.49, RF.50 |
| Gestión de contratos       | 🟢 Baja   | RF.52, RF.54 |
| Envío de ofertas           | 🟢 Baja   | RF.53        |
| Sugerencias de mejora      | 🟢 Baja   | RF.47        |

---

### 11. Configuración y Preferencias

| Submódulo                  | Prioridad | Requisitos   |
| -------------------------- | --------- | ------------ |
| Cambio de idioma           | 🟡 Media  | RF.56        |
| Tema claro/oscuro          | 🟢 Baja   | RF.59        |
| Edición de perfil          | 🟡 Media  | RF.58        |
| Soporte técnico            | 🔴 Alta   | RF.57, RF.60 |
| Restablecimiento de cuenta | 🔴 Alta   | RF.61        |

---

### 12. Funcionalidades Adicionales

| Submódulo             | Prioridad | Requisitos   |
| --------------------- | --------- | ------------ |
| Enlace con otras apps | 🟢 Baja   | RF.35        |
| Temas de interés      | 🟢 Baja   | RF.44, RF.45 |
| Calendario nativo     | 🟢 Baja   | RF.41        |

---

## Resumen de Estado

### Por Prioridad

| Prioridad | Módulos Completos | Módulos Parciales | Módulos Pendientes | Total  |
| --------- | ----------------- | ----------------- | ------------------ | ------ |
| 🔴 Alta   | 5                 | 1                 | 4                  | 10     |
| 🟡 Media  | 4                 | 2                 | 14                 | 20     |
| 🟢 Baja   | 0                 | 0                 | 8                  | 8      |
| **Total** | **9**             | **3**             | **26**             | **38** |

### Por Categoría

| Categoría          | Implementado | Parcial | Pendiente |
| ------------------ | ------------ | ------- | --------- |
| Autenticación      | 100%         | -       | -         |
| Hábitos Saludables | 70%          | 30%     | -         |
| Productividad      | 80%          | 20%     | -         |
| Control Parental   | 20%          | 30%     | 50%       |
| Notificaciones     | 0%           | -       | 100%      |
| Configuración      | 10%          | -       | 90%       |
| Administración     | 0%           | -       | 100%      |

### Cobertura de Requisitos Funcionales

- **Total de requisitos**: 65
- **Implementados**: 19 (29%)
- **Parcialmente implementados**: 5 (8%)
- **Pendientes**: 41 (63%)

---

## Notas Importantes

> [!IMPORTANT]
> **Módulos Críticos Pendientes**:
>
> 1. Sistema de Notificaciones (RF.14-17, RF.29-34)
> 2. Control Parental Completo (RF.8, RF.13, RF.19-20)
> 3. Bloqueo Real de Apps (RF.18, RF.22, RF.23)
> 4. Soporte Técnico (RF.57, RF.60, RF.61)

> [!NOTE]
> Este documento se actualizará conforme se implementen nuevos módulos.

---

**Última actualización**: 12 de Febrero de 2026
