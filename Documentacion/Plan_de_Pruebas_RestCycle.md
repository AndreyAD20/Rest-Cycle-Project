# Plan de Pruebas de Software - Rest-Cycle

**Proyecto:** Rest-Cycle  
**Autor:** Andrey Alejandro Suesca Fuentes  
**Fecha:** 12 de Marzo de 2026  
**Versión:** 1.0  

---

## 1. Introducción
El presente documento define el plan estratégico para las pruebas de software del proyecto **Rest-Cycle**, una solución integral de bienestar digital que incluye una aplicación móvil Android (Kotlin/Jetpack Compose) y una infraestructura de backend basada en Node.js y Supabase. El objetivo es asegurar que todas las funcionalidades, desde el control parental hasta la monitorización en segundo plano, operen con los más altos estándares de calidad, seguridad y rendimiento.

## 2. Análisis de Requerimientos
Basado en el documento de requisitos (V1.5):
*   **Requerimientos Críticos (Alta):** Registro e Inicio de sesión (RF.1, RF.3), Bloqueo general de aplicaciones (RF.18), Establecimiento de tiempos máximos (RF.22), Seguridad de datos (RNF.07).
*   **Requerimientos de Media:** Gestión de tareas (RF.12), Visualización de estadísticas (RF.63), Gestión de membresías (RF.5).
*   **Requerimientos No Funcionales:** Tiempo de respuesta < 4s (RNF.04), Disponibilidad 24/7 (RNF.06), Compatibilidad con versiones de Android (RNF.08).

## 3. Funcionalidades a Probar
### 3.1 Funcionalidades Existentes (Regresión)
- Flujo de autenticación (Login/Registro/Olvido de contraseña).
- Edición de perfil de usuario.
- Visualización de gráficos estadísticos básicos.

### 3.2 Funcionalidades Nuevas/Críticas a Validar
- **Control Parental:** Vinculación de cuentas (Padre/Hijo) y gestión de permisos.
- **Monitorización (Background):** `AppMonitorService` detectando límites de tiempo en tiempo real.
- **Gamificación:** Asignación de tiempo adicional como recompensa por tareas completadas.
- **Seguridad:** Cifrado de comunicaciones y validación de tokens de Supabase.

## 4. Estrategia y Criterios de Pruebas
### 4.1 Estrategia
- **Pruebas de Caja Negra:** Validación de entradas y salidas desde la perspectiva del usuario.
- **Pruebas de Caja Blanca:** Pruebas unitarias sobre la lógica de los Repositorios y ViewModels en Kotlin.
- **Pruebas de Integración:** Verificar la comunicación entre el cliente Android y la API de Node.js/Supabase.
- **Pruebas de Carga:** Simulación de usuarios concurrentes (según RNF.05).

### 4.2 Criterios de Aceptación
- 100% de los casos de prueba críticos ("Alta") aprobados.
- Ningún error de severidad "Bloqueante" o "Crítica" abierto.
- Tiempo de respuesta medido por debajo de los límites del RNF.

## 5. Entornos de Trabajo (Software y Hardware)
- **Hardware:** Dispositivos físicos Android (Android 10+), Servidores locales de desarrollo, Estaciones de trabajo con 16GB RAM+.
- **Software:** 
    - IDE: Android Studio, VS Code.
    - DB: Supabase (PostgreSQL).
    - SO: Windows/Linux para el desarrollo de la API.

## 6. Metodología y Planificación
### 6.1 Metodología
Se utilizará la metodología **Ágil (Scrum)**, integrando las pruebas en cada Sprint (Shift-left testing).

### 6.2 Cronograma Propuesto (4 Semanas)
- **Semana 1:** Pruebas unitarias de lógica de negocio y API endpoints.
- **Semana 2:** Pruebas de integración de servicios en segundo plano (`AppMonitorService`).
- **Semana 3:** Pruebas de UI (Compose) y experiencia de usuario (UX).
- **Semana 4:** Pruebas de aceptación de usuario (UAT) y corrección de errores finales.

## 7. Instrumentos para el Registro de Pruebas
Los instrumentos se llevarán en los siguientes formatos:
1.  **Matriz de Casos de Prueba:** ID, Descripción, Precondición, Pasos, Resultado Esperado, Estado (Pasa/Falla).
2.  **Reporte de Defectos (Bugs):** Título, Gravedad, Pasos para reproducir, Captura de pantalla, Responsable.

## 8. Herramientas Seleccionadas
- **Android:** JUnit 5, MockK, Espresso, Compose Testing API.
- **Backend:** Jest, Supertest (para Node.js).
- **Gestión:** GitHub Issues / Jira.
- **Monitorización:** Firebase Crashlytics.

## 9. Riesgos y Contingencias
- **Riesgo:** Incompatibilidad con versiones antiguas de Android. 
    - *Contingencia:* Uso de librerías de retrocompatibilidad y pruebas en emuladores de diversas versiones.
- **Riesgo:** Fallos en la API de Supabase por límites de cuota.
    - *Contingencia:* Monitoreo constante de consumos y optimización de consultas.

## 10. Conclusiones
El plan de pruebas garantiza que **Rest-Cycle** no solo sea funcional, sino también robusto y seguro para su público objetivo. La combinación de pruebas automatizadas y manuales sobre una arquitectura basada en microservicios y móvil asegura una experiencia de usuario fluida y el cumplimiento de los objetivos de salud digital propuestos.
