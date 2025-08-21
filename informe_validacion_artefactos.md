# INFORME DE VALIDACIÓN DE ARTEFACTOS

## Introducción

La validación de artefactos es un proceso fundamental en el desarrollo de software que garantiza que los productos entregables cumplan con los estándares de calidad establecidos y los requisitos especificados. Este informe documenta el proceso de validación llevado a cabo para asegurar la conformidad y calidad de los artefactos desarrollados en el proyecto.

La validación permite identificar defectos, inconsistencias y desviaciones de los requisitos en etapas tempranas del desarrollo, reduciendo costos y mejorando la calidad del producto final.

## Objetivo

El objetivo principal de este informe es:

- Documentar el proceso de validación de artefactos del proyecto
- Verificar el cumplimiento de los requisitos funcionales y no funcionales establecidos
- Identificar el grado de conformidad de los artefactos con los estándares de calidad
- Proporcionar una base documentada para la toma de decisiones sobre la aceptación de los entregables
- Establecer un registro formal del proceso de validación para futuras referencias y auditorías

## Descripción del Proyecto

### Contexto del Proyecto

RestCycle es una aplicación móvil innovadora diseñada para promover hábitos saludables en el uso de dispositivos tecnológicos, especialmente enfocada en el control parental y la gestión del tiempo de pantalla.

- **Nombre del proyecto**: RestCycle
- **Alcance**: Aplicación móvil multiplataforma para control parental y gestión de hábitos digitales saludables
- **Stakeholders principales**: 
  - Padres de familia (usuarios gestores)
  - Menores de edad (usuarios gestionados)
  - Administradores del sistema
  - Desarrolladores del equipo
- **Funcionalidades principales**: 
  - Control parental avanzado
  - Gestión de tiempo de uso de aplicaciones
  - Sistema de notificaciones inteligentes
  - Seguimiento de ubicación GPS
  - Gestión de tareas y actividades
  - Sistema de membresías (gratuita y premium)
- **Metodología de desarrollo**: Desarrollo ágil con entregas incrementales
- **Versión actual de documentación**: v1.0 (Julio 2025)

### Artefactos a Validar

Los artefactos sujetos a validación en el proyecto RestCycle incluyen:

- **Documentación de requisitos** (RestCycle-DocumentoRequisitos-V1.0)
- **Casos de uso e historias de usuario** (RestCycle-DocumentoCasosUso.HistoriasaUsuario-v1.0)
- **Diagrama de clases** (RestCycle-DiagramaClases-v0.5)
- **Modelo Entidad-Relación** (RestCycle-ModeloEntidadRelacion-MER-v1.0)
- **Modelo Relacional** (RestCycle-ModeloRelacional-v0.5)
- **Diccionario de datos** (RestCycle-DiccionarioDatos-v1.0)
- **Bocetos de interfaz** (RestCycle-BocetosDeLaInterfaz-v0.5)
- **Propuesta técnica** (RestCycle-PropuestaTecnica-v0.5)

## Requisitos Funcionales

El proyecto RestCycle cuenta con 58 requisitos funcionales organizados en las siguientes categorías principales:

### Gestión de Usuarios y Autenticación

#### RF.1: Registro de Usuario
- **Descripción**: Registro para usuarios nuevos tomando datos esenciales para la creación de la cuenta
- **Prioridad**: Alta
- **Criterios de aceptación**: El sistema debe permitir registro con datos básicos y cuentas de terceros

#### RF.2: Términos y Condiciones
- **Descripción**: Aceptar términos y condiciones para registrarse
- **Prioridad**: Alta
- **Criterios de aceptación**: El usuario debe aceptar términos antes de completar registro

#### RF.3: Inicio de Sesión
- **Descripción**: Ingreso de usuarios con cuentas por medio de credenciales
- **Prioridad**: Alta
- **Criterios de aceptación**: Autenticación segura con credenciales o cuentas de terceros

#### RF.4: Cambio de Contraseña
- **Descripción**: Cambio de contraseña en caso de olvidarse por medio de códigos enviados a correos o teléfono
- **Prioridad**: Media
- **Criterios de aceptación**: Proceso de recuperación seguro vía email/SMS

### Control Parental y Gestión

#### RF.5: Membresías
- **Descripción**: Elección de membresía ya sea gratuita o de pago
- **Prioridad**: Alta
- **Criterios de aceptación**: Sistema de membresías diferenciadas con funcionalidades específicas

#### RF.6: Selección de Modo
- **Descripción**: Acceso a cualquiera de los dos modos ya sea para controlar y generar hábitos saludables o el control parental
- **Prioridad**: Alta
- **Criterios de aceptación**: Interfaz clara para selección de modo de uso

#### RF.7-RF.9: Gestión de Cuentas Vinculadas
- **Descripción**: Creación, vinculación y gestión de cuentas para menores de edad
- **Prioridad**: Alta
- **Criterios de aceptación**: Sistema seguro de vinculación entre cuentas gestoras y gestionadas

### Monitoreo y Control de Aplicaciones

#### RF.10: Historial de Uso
- **Descripción**: Ver un informe de las estadísticas de uso de aplicaciones del usuario
- **Prioridad**: Alta
- **Criterios de aceptación**: Reportes detallados y visualizaciones claras del uso

#### RF.18: Bloqueo General
- **Descripción**: Bloqueo de todas las aplicaciones seleccionadas por el usuario que gestiona instantáneamente
- **Prioridad**: Alta
- **Criterios de aceptación**: Bloqueo inmediato y efectivo de aplicaciones

#### RF.22: Tiempo Máximo Diario por Aplicación
- **Descripción**: Establecer el tiempo de uso máximo diario en las aplicaciones seleccionadas del usuario gestionado
- **Prioridad**: Alta
- **Criterios de aceptación**: Configuración flexible y aplicación automática de límites

### Sistema de Notificaciones

#### RF.14-RF.17: Notificaciones Inteligentes
- **Descripción**: Sistema completo de notificaciones para actividades, uso excedido y recomendaciones
- **Prioridad**: Media
- **Criterios de aceptación**: Notificaciones oportunas y relevantes para ambos tipos de usuario

### Funcionalidades Administrativas

#### RF.45-RF.53: Gestión Administrativa
- **Descripción**: Funcionalidades para administradores incluyendo gestión de cuentas, reportes y contratos
- **Prioridad**: Media
- **Criterios de aceptación**: Panel administrativo completo y funcional

**Total de Requisitos Funcionales**: 58 requisitos identificados y documentados

## Requisitos No Funcionales

El proyecto RestCycle cuenta con 12 requisitos no funcionales específicos que garantizan la calidad y operabilidad del sistema:

### RNF.01: Rendimiento de Notificaciones
- **Descripción**: El sistema notifica a través de una burbuja tipo chat, esta notificación debe enviarse y generarse entre 5 a 7 segundos de respuesta
- **Métricas**: Tiempo de respuesta de notificaciones: 5-7 segundos
- **Criterios de validación**: Pruebas de rendimiento de sistema de notificaciones

### RNF.02: Usabilidad de Interfaz
- **Descripción**: La interfaz debe ser fácil de manejar con un diseño atractivo y muy accesible
- **Métricas**: Evaluación heurística de usabilidad, tiempo de aprendizaje
- **Criterios de validación**: Pruebas de usabilidad con usuarios reales

### RNF.03: Accesibilidad e Internacionalización
- **Descripción**: La aplicación debe ser accesible y que cuente con un soporte en varios idiomas
- **Métricas**: Cumplimiento de estándares de accesibilidad, número de idiomas soportados
- **Criterios de validación**: Verificación de estándares WCAG, pruebas de localización

### RNF.04: Tiempo de Respuesta del Sistema
- **Descripción**: La aplicación debe responder a las interacciones con el usuario en menos de 4 segundos
- **Métricas**: Tiempo de respuesta máximo: 4 segundos
- **Criterios de validación**: Pruebas de rendimiento automatizadas

### RNF.05: Capacidad de Usuarios Concurrentes
- **Descripción**: La aplicación debe ser capaz de manejar mil usuarios simultáneamente sin degradar el rendimiento del sistema
- **Métricas**: 1000 usuarios concurrentes sin degradación
- **Criterios de validación**: Pruebas de carga y estrés

### RNF.06: Disponibilidad del Sistema
- **Descripción**: El sistema debe estar disponible las 24 horas del día, los 7 días de la semana
- **Métricas**: Disponibilidad 24/7 (99.9% uptime)
- **Criterios de validación**: Monitoreo continuo de disponibilidad

### RNF.07: Seguridad de Datos
- **Descripción**: La aplicación debe garantizar la protección de los datos obtenidos del usuario mediante un cifrado de datos y medidas de seguridad robustas
- **Métricas**: Cifrado de datos, cumplimiento de estándares de seguridad
- **Criterios de validación**: Auditorías de seguridad, pruebas de penetración

### RNF.08: Compatibilidad de Sistemas Operativos
- **Descripción**: La aplicación debe ser compatible con la mayoría de las versiones de sistemas operativos móviles y con los más recientes
- **Métricas**: Compatibilidad con iOS y Android (últimas 3 versiones)
- **Criterios de validación**: Pruebas en múltiples dispositivos y versiones de SO

### RNF.09: Compatibilidad de Dispositivos
- **Descripción**: La aplicación debe funcionar correctamente en la mayoría de los dispositivos móviles
- **Métricas**: Funcionamiento en gama alta, media y básica de dispositivos
- **Criterios de validación**: Pruebas en diferentes especificaciones de hardware

### RNF.10: Integridad de Datos en Actualizaciones
- **Descripción**: Las actualizaciones de la aplicación no deben afectar la integridad de los datos existentes
- **Métricas**: 100% de preservación de datos en actualizaciones
- **Criterios de validación**: Pruebas de migración y rollback

### RNF.11: Control de Calidad
- **Descripción**: La aplicación debe contar con un proceso de control de calidad, para que las nuevas versiones no lleven errores
- **Métricas**: Cobertura de pruebas, procesos de QA
- **Criterios de validación**: Revisión de procesos de testing y QA

### RNF.12: Escalabilidad
- **Descripción**: La aplicación debe soportar un aumento del 50% de los usuarios concurrentes sin afectar integridad
- **Métricas**: Escalabilidad del 50% adicional de usuarios
- **Criterios de validación**: Pruebas de escalabilidad horizontal

**Total de Requisitos No Funcionales**: 12 requisitos identificados y documentados

## Conclusiones

### Resumen del Estado de Validación

El proyecto RestCycle presenta una documentación robusta y bien estructurada en su fase de diseño:

- **Artefactos documentados**: 8 artefactos principales identificados y versionados
- **Requisitos funcionales**: 58 requisitos funcionales especificados y categorizados
- **Requisitos no funcionales**: 12 requisitos no funcionales con métricas específicas
- **Estado de documentación**: Versión 1.0 para requisitos principales (Julio 2025)

### Hallazgos Principales

#### Fortalezas Identificadas:
- **Documentación completa**: El proyecto cuenta con una amplia gama de artefactos de diseño
- **Requisitos bien definidos**: Los 58 requisitos funcionales están claramente especificados
- **Enfoque innovador**: La aplicación aborda una necesidad real del control parental digital
- **Arquitectura sólida**: Modelos de datos y diagramas de clases bien estructurados
- **Versionado adecuado**: Todos los artefactos están versionados apropiadamente

#### Áreas de Mejora:
- **Consistencia en versiones**: Algunos artefactos están en v0.5 mientras otros en v1.0
- **Especificación técnica**: Algunos requisitos podrían beneficiarse de mayor detalle técnico
- **Casos de prueba**: Se requiere documentación específica de casos de prueba

#### Riesgos Identificados:
- **Complejidad del sistema**: 58 requisitos funcionales representan un sistema complejo
- **Dependencias externas**: Integración con calendarios y aplicaciones de terceros
- **Seguridad crítica**: Manejo de datos sensibles de menores de edad

### Recomendaciones

1. **Estandarización de versiones**: Unificar todas las versiones de artefactos a v1.0 para la entrega final
2. **Documentación de pruebas**: Crear casos de prueba específicos para cada requisito funcional crítico
3. **Revisión de seguridad**: Realizar auditoría de seguridad especializada dado el manejo de datos de menores

### Estado de Aceptación

Basado en la documentación revisada del proyecto RestCycle:

- ✅ **Artefactos aprobados**: 
  - Documento de Requisitos v1.0
  - Casos de Uso e Historias de Usuario v1.0
  - Modelo Entidad-Relación v1.0
  - Diccionario de Datos v1.0

- ⚠️ **Artefactos con observaciones**:
  - Diagrama de Clases v0.5 (requiere actualización a v1.0)
  - Bocetos de Interfaz v0.5 (requiere finalización)
  - Modelo Relacional v0.5 (requiere actualización)
  - Propuesta Técnica v0.5 (requiere finalización)

- ❌ **Artefactos no conformes**: 
  - Casos de prueba (no identificados en la documentación actual)
  - Plan de despliegue (pendiente de creación)

### Próximos Pasos

1. **Finalizar artefactos v0.5**: Completar y actualizar todos los artefactos a versión 1.0
2. **Crear casos de prueba**: Desarrollar casos de prueba para los 58 requisitos funcionales
3. **Validación técnica**: Realizar revisión técnica de la arquitectura propuesta
4. **Aprobación de stakeholders**: Presentar documentación completa para aprobación final

---

**Fecha del informe**: [Fecha de creación]  
**Responsable de validación**: [Nombre del responsable]  
**Versión del documento**: 1.0  
**Estado**: [Borrador/En revisión/Aprobado]