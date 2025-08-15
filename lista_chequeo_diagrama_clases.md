# Lista de Chequeo para Evaluación de Diagramas de Clases UML

## 1. Nomenclatura y Convenciones

### 1.1 Nombres de Clases
- **C1.1** - Nombres significativos: Las clases utilizan sustantivos descriptivos que reflejan claramente su propósito y responsabilidad en el dominio del problema.
- **C1.2** - Convención PascalCase: Todos los nombres de clases siguen la convención PascalCase (primera letra mayúscula) de manera consistente.
- **C1.3** - Evitar abreviaciones: Los nombres evitan abreviaciones confusas y utilizan términos del dominio comprensibles por stakeholders.

### 1.4 Nombres de Atributos y Métodos
- **C1.4** - Nomenclatura descriptiva: Atributos y métodos tienen nombres que expresan claramente su función, evitando nombres genéricos como "data" o "info".
- **C1.5** - Convención camelCase: Atributos y métodos siguen consistentemente la convención camelCase.
- **C1.6** - Coherencia terminológica: El vocabulario utilizado es consistente en todo el diagrama, usando los mismos términos para conceptos similares.

## 2. Estructura de Clases: Atributos y Métodos

### 2.1 Definición de Atributos
- **C2.1** - Tipos de datos apropiados: Cada atributo especifica un tipo de dato coherente con su propósito funcional.
- **C2.2** - Visibilidad correcta: Los modificadores de acceso (public, private, protected) están correctamente asignados según el principio de encapsulación.
- **C2.3** - Atributos esenciales: Solo se incluyen atributos que son fundamentales para la identidad y comportamiento de la clase.

### 2.2 Definición de Métodos
- **C2.4** - Firmas completas: Los métodos incluyen parámetros con tipos y valores de retorno cuando corresponde.
- **C2.5** - Métodos cohesivos: Cada método tiene una responsabilidad específica y bien definida dentro del contexto de la clase.
- **C2.6** - Operaciones esenciales: Se incluyen métodos constructores, getters/setters necesarios y operaciones de negocio relevantes.

## 3. Relaciones y Multiplicidades

### 3.1 Asociaciones
- **C3.1** - Semántica clara: Cada asociación representa una relación conceptual válida y necesaria entre las clases del dominio.
- **C3.2** - Direccionalidad apropiada: Las asociaciones unidireccionales y bidireccionales están correctamente representadas según la necesidad de navegación.
- **C3.3** - Nomenclatura de roles: Los roles en las asociaciones tienen nombres significativos que clarifican la función de cada clase en la relación.
- **C3.4** - Etiquetado descriptivo: Las asociaciones incluyen etiquetas verbales que describen claramente la naturaleza de la relación.

### 3.2 Multiplicidades
- **C3.5** - Notación correcta: Las multiplicidades utilizan la notación UML estándar (1, 0..1, 1..*, 0..*, n..m) de forma precisa.
- **C3.6** - Coherencia con requisitos: Las cardinalidades reflejan fielmente las reglas de negocio y restricciones del dominio.
- **C3.7** - Consistencia bidireccional: En asociaciones bidireccionales, las multiplicidades son coherentes desde ambas perspectivas.
- **C3.8** - Ausencia de contradicciones: No existen multiplicidades que generen inconsistencias lógicas en el modelo.

### 3.3 Especialización y Composición
- **C3.9** - Jerarquías válidas: Las relaciones de herencia representan verdaderas relaciones "es-un" del dominio.
- **C3.10** - Composición vs agregación: Se distingue correctamente entre composición (diamante lleno) y agregación (diamante vacío) según el ciclo de vida.
- **C3.11** - Profundidad apropiada: Las jerarquías de herencia no son excesivamente profundas (máximo 4-5 niveles recomendados).

## 4. Principios SOLID

### 4.1 Single Responsibility Principle (SRP)
- **C4.1** - Responsabilidad única: Cada clase tiene una sola razón para cambiar y una responsabilidad claramente definida.
- **C4.2** - Cohesión alta: Los atributos y métodos de cada clase están estrechamente relacionados con su propósito principal.

### 4.3 Open/Closed Principle (OCP)
- **C4.3** - Extensibilidad: El diseño permite extensión de funcionalidad sin modificar clases existentes, usando herencia o composición.
- **C4.4** - Abstracciones apropiadas: Se utilizan interfaces o clases abstractas para facilitar la extensión del comportamiento.

### 4.5 Liskov Substitution Principle (LSP)
- **C4.5** - Sustituibilidad: Las subclases pueden sustituir a sus clases base sin alterar la funcionalidad del sistema.
- **C4.6** - Contratos preservados: Las subclases respetan los contratos (precondiciones/postcondiciones) de las clases padre.

### 4.7 Interface Segregation Principle (ISP)
- **C4.7** - Interfaces específicas: Las interfaces son pequeñas y específicas, sin forzar a las clases a implementar métodos innecesarios.
- **C4.8** - Segregación funcional: Las responsabilidades están segregadas en múltiples interfaces cuando es apropiado.

### 4.9 Dependency Inversion Principle (DIP)
- **C4.9** - Dependencias abstractas: Las clases de alto nivel dependen de abstracciones, no de implementaciones concretas.
- **C4.10** - Inversión de control: Las dependencias se inyectan desde el exterior, no se crean internamente.

## 5. Calidad General del Diagrama

### 5.1 Completitud y Consistencia
- **C5.1** - Completitud funcional: El diagrama incluye todas las clases necesarias para cumplir con los requisitos funcionales identificados.
- **C5.2** - Integridad referencial: Todas las referencias entre clases están correctamente definidas y son navegables.
- **C5.3** - Ausencia de redundancia: No existen clases, atributos o métodos duplicados o innecesariamente similares.

### 5.4 Claridad Visual y Organización
- **C5.4** - Distribución espacial: Las clases están organizadas de manera que las relaciones sean claras y el diagrama sea legible.
- **C5.5** - Agrupación lógica: Las clases relacionadas están visualmente agrupadas, facilitando la comprensión del modelo.
- **C5.6** - Simplicidad apropiada: El diagrama no es excesivamente complejo para su propósito, manteniendo un equilibrio entre detalle y claridad.

### 5.7 Escalabilidad y Mantenibilidad
- **C5.7** - Bajo acoplamiento: Las clases tienen dependencias mínimas y bien justificadas entre sí.
- **C5.8** - Patrones de diseño: Se aplican patrones de diseño reconocidos cuando añaden valor al modelo.
- **C5.9** - Flexibilidad: El diseño permite modificaciones futuras sin requerir cambios extensivos en múltiples clases.
- **C5.10** - Trazabilidad: Existe una correspondencia clara entre las clases del diagrama y los requisitos del sistema.

---

**Total de Criterios: 40**

### Escala de Evaluación Sugerida:
- ✅ **Cumple completamente** (3 puntos)
- ⚠️ **Cumple parcialmente** (2 puntos)  
- ❌ **No cumple** (1 punto)
- ❓ **No aplicable** (0 puntos)

**Puntuación máxima:** 120 puntos  
**Umbral de calidad recomendado:** ≥ 85% (102 puntos)