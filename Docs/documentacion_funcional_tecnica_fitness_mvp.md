# Especificación Funcional y Técnica (PRD & System Design)
## Proyecto: Application Fitness MVP (PowerTrack)
**Versión:** 1.0.0  
**Fecha:** Julio 2026  
**Autores:** Lead Product Manager, Principal Software Architect & Mobile Tech Lead  
**Estado:** Aprobado para Desarrollo  

---

## 1. Visión del Producto

### 1.1 Declaración de Visión
Convertirse en la herramienta de registro de entrenamiento de fuerza y acondicionamiento físico más rápida, fluida y eficiente del mercado móvil, eliminando la fricción del registro analógico y digital tradicional para permitir que el atleta se enfoque en la ejecución de la sesión sin distracciones.

### 1.2 Principios del Producto
*   **Velocidad Obsesiva (Zero-Friction Logging):** El registro de una serie completada no debe tomar más de 2 segundos ni más de 2 toques en pantalla.
*   **Offline-First:** La experiencia dentro del gimnasio no puede depender de la cobertura de red (sótanos, zonas de baja señal).
*   **Prescriptiva basada en Datos (Deterministic Insights):** Sugerencias operativas inmediatas basadas en algoritmos de sobrecarga progresiva y autoregulación, sin la opacidad ni alucinaciones de la IA generativa.
*   **Claridad Visual:** Interfaz minimalista optimizada para entornos de alta fatiga física y visual.

---

## 2. Problema que Resuelve

### 2.1 Contexto y Puntos de Dolor
1.  **Fricción en Libretas Físicas:** Pérdida de lapiceros, deterioro de papel, imposibilidad de graficar avances históricos sin trabajo manual exhaustivo.
2.  **Lentitud en Apps de Notas Genéricas (Notion, Keep, Apple Notes):** Requiere tipado manual constante, formateo continuo y no ofrece memoria de la carga histórica de la sesión anterior en tiempo real.
3.  **Complejidad Extrema en Apps Fitness Actuales:** Menús recargados, exceso de redes sociales internas, paywalls agresivos y requerimiento de conectividad constante que interrumpe el flujo de entrenamiento.

### 2.2 Solución Propuesta
Una aplicación Android nativa ultrarrápida respaldada por una API robusta en Spring Boot que precarga automáticamente el desempeño de la sesión previa (peso, reps, RPE), permite el registro en un toque y calcula en tiempo real la progresión recomendada para la siguiente serie/sesión.

---

## 3. Público Objetivo

### 3.1 Arquetipos de Usuario (Personas)

#### Persona 1: Mateo "El Atleta Enfocado" (Powerlifting / Hipertrofia)
*   **Edad:** 24 años.
*   **Necesidad:** Registrar con exactitud peso, repeticiones y RPE (Rating of Perceived Exertion).
*   **Frustración:** Las apps actuales le piden demasiados pasos para registrar una serie mientras se recupera de un levantamiento pesado.
*   **Meta:** Sobrecarga progresiva rigurosa sin perder tiempo.

#### Persona 2: Sofia "La Entusiasta Saludable" (Gimnasio + Cardio)
*   **Edad:** 31 años.
*   **Necesidad:** Combinar rutinas de fuerza estructuradas con sesiones ocasionales de running o bicicleta estática.
*   **Frustración:** Olvida qué peso usó la semana pasada para cada ejercicio.
*   **Meta:** Ver un gráfico claro que demuestre que se está volviendo más fuerte y saludable.

---

## 4. Casos de Uso Principales

*   **CU-01:** Autenticación e Inicia de Sesión (Registro, Login, Configuración de Perfil y Objetivo Deportivo).
*   **CU-02:** Gestión de Rutinas (Crear, Editar, Listar, Estructurar por Días/Ejercicios/Series/Rangos/Descanso).
*   **CU-03:** Ejecución de Entrenamiento Activo (Pantalla Principal -> Decisión: Empezar Rutina / Ver Desempeño; Registro ultrarrápido con precarga automática).
*   **CU-04:** Registro de Actividad Cardiovascular (Caminar, Correr, Bici, Natación, Remo, etc.).
*   **CU-05:** Consulta de Historial y Análisis de Progreso (Gráficos por ejercicio, rutina y grupo muscular).
*   **CU-06:** Generación de Sugerencias Automáticas de Carga (Reglas de negocio deterministas de sobrecarga progresiva).

---

## 5. Historias de Usuario (US)

| ID | Título | Como... | Quiero... | Para... | Criterios de Aceptación |
|:---|:---|:---|:---|:---|:---|
| **US-01** | Selección de Objetivo Deportivo | Usuario nuevo | Seleccionar mi objetivo al registrarme (fuerza, hipertrofia, etc.) | Que el sistema adapte los algoritmos de recomendación | Se debe elegir 1 objetivo de la lista predefinida al crear la cuenta. |
| **US-02** | Pantalla Principal de Decisión | Usuario autenticado | Ver un menú principal con las acciones 'Empezar Rutina' y 'Ver Desempeño' | Acceder en 1 toque a lo que necesito hacer en el gimnasio | Botones de alta visibilidad en Home sin distracciones. |
| **US-03** | Precarga del Historial Anterior | Usuario entrenando | Ver automáticamente en cada ejercicio qué peso y reps hice la sesión anterior | Saber exactamente qué carga debo superar sin buscar en el historial | Muestra flotante o inline con la última marca grabada del ejercicio. |
| **US-04** | Registro Rápido de Serie | Usuario entrenando | Confirmar una serie con un solo tap aceptando o ajustando valores precargados | Minimizar el tiempo con el teléfono en la mano | Teclado numérico optimizado, botón de autocompletar serie previa. |
| **US-05** | Sugerencia de Progresión Determinista | Usuario completando rutina | Recibir una sugerencia automática de ajuste de peso/reps basada en mi RPE y reps previas | Saber si debo subir, mantener o bajar la carga | Notificación visual en pantalla al finalizar el ejercicio basada en las reglas de negocio. |
| **US-06** | Modo Offline Transaccional | Usuario en zona sin señal | Registrar toda mi sesión sin conexión a internet | No perder mis datos si la red falla en el gimnasio | Persistencia en Room y sincronización diferida automática al recuperar red. |

---

## 6. Requisitos Funcionales (RF)

*   **RF-01 (Gestión de Cuentas):** El sistema debe permitir registro con email/password y selección obligatoria del objetivo deportivo (Fuerza, Hipertrofia, Estética, Atletismo, Movilidad, Flexibilidad, Salud, Powerlifting, Calistenia, Running).
*   **RF-02 (Pantalla Hub):** La pantalla principal debe priorizar dos flujos inmediatos: "Empezar Rutina" y "Ver Desempeño".
*   **RF-03 (Creador de Rutinas):** Debe permitir estructurar días, ejercicios asociados, número de series objetivo, rango de repeticiones objetivo (ej: 8-12), tiempo de descanso recomendado (segundos) y notas técnicas.
*   **RF-04 (Motor de Ejecución de Entrenamiento):**
    *   Debe mostrar el historial previo del ejercicio seleccionado (peso anterior, repeticiones anteriores, RPE anterior, observaciones).
    *   Permitir capturar: peso utilizado, repeticiones logradas, RPE (escala 1-10), sensación general de la sesión (Mala, Regular, Buena, Excelente) y observaciones.
*   **RF-05 (Módulo Cardiovascular):** Permitir registrar actividades (caminar, correr, bicicleta, nadar, saltar cuerda, remo, senderismo) capturando: tiempo total, distancia (km), calorías (opcional), ritmo medio, RPE y observaciones.
*   **RF-06 (Motor de Sugerencias Automáticas):** Calcular y desplegar recomendaciones automáticas al finalizar un ejercicio (Aumentar peso, Mantener peso, Reducir peso, Aumentar repeticiones, Recomendar descarga/Deload, Recomendar descanso adicional).
*   **RF-07 (Visualización de Progreso):** Presentar gráficos interactivos de evolución por ejercicio (1RM estimado, volumen total), por rutina y por grupo muscular.

---

## 7. Requisitos No Funcionales (RNF)

*   **RNF-01 (Performance Móvil):** La pantalla de registro de serie debe responder en menos de 100 ms tras la acción del usuario.
*   **RNF-02 (Disponibilidad Offline):** La aplicación móvil debe soportar lectura y escritura en local (Room) sin degradación de la experiencia en ausencia total de red.
*   **RNF-03 (Seguridad Backend):** Autenticación mediante tokens JWT firmados con RSA/HMAC SHA-256. Expiración de Access Token a los 15 minutos y Refresh Tokens seguros.
*   **RNF-04 (Tiempos de Respuesta API):** El 95% de las peticiones REST deben responder en menos de 200 ms.
*   **RNF-05 (Arquitectura de Software):**
    *   **Backend:** Spring Boot con Arquitectura Hexagonal (Ports & Adapters). Núcleo de dominio (modelo + casos de uso + motor de reglas) desacoplado de frameworks; adaptadores de entrada (Controllers REST, Security) y de salida (JPA, integraciones externas) implementan los puertos definidos por el dominio. Elegida para facilitar la incorporación desacoplada de integraciones futuras (Health Connect/Google Fit, modo Entrenador/Cliente, import/export QR — ver §18) sin modificar el núcleo de negocio.
    *   **Android:** Arquitectura MVVM + Clean Architecture ligera, utilizando Kotlin Coroutines, Flow, Jetpack Compose y Dependency Injection con Hilt.

---

## 8. Arquitectura de Alto Nivel

El sistema utiliza un patrón **Offline-First Synchronized Architecture**. El dispositivo Android actúa como la fuente de verdad primaria durante la sesión activa utilizando **Room DB**. El backend en **Spring Boot** gestiona la consolidación centralizada, autenticación, cálculo de agregados históricos y persistencia en **PostgreSQL**.

```
 +-------------------------------------------------------------------------+
 |                            ANDROID CLIENT                               |
 |                                                                         |
 |  [ UI Layer: Jetpack Compose ] <---> [ ViewModel / StateFlow ]          |
 |                                              |                          |
 |                                    [ Repository Manager ]               |
 |                                     /                  \               |
 |                       (Offline DB) /                    \ (REST/HTTPS)  |
 |                      [ Room SQLite ]                   [ Retrofit ]     |
 +-------------------------------------------------------------------------+
                                                              |
                                                    (JSON / JWT Security)
                                                              |
 +-------------------------------------------------------------------------+
 |                           BACKEND SERVICE                               |
 |                                                                         |
 |   [ Nginx Reverse Proxy / SSL ]                                         |
 |                 |                                                       |
 |   [ Spring Boot Application (Java 21) - Arquitectura Hexagonal ]        |
 |      ├── Adaptadores IN: Controllers REST + Spring Security (JWT)       |
 |      ├── Núcleo de Dominio: Casos de Uso + Motor de Reglas (Ports IN/OUT)|
 |      └── Adaptadores OUT: Spring Data JPA + Flyway (implementan Ports)  |
 |                 |                                                       |
 |   [ PostgreSQL Database (Dockerized Container) ]                        |
 +-------------------------------------------------------------------------+
```

---

## 9. Módulos del Sistema

### 9.1 Módulos Backend (Spring Boot)
1.  **Auth Service:** Gestión de usuarios, credenciales, emisión y renovación de JWT, hash de contraseñas con BCrypt.
2.  **User Profile Service:** Gestión de datos antropométricos y objetivos deportivos.
3.  **Routine Management Service:** CRUD de rutinas, días, asignación de ejercicios y parámetros.
4.  **Workout Execution Service:** Procesamiento de logs de entrenamiento, captura de series y cálculo de métricas.
5.  **Cardio Log Service:** Registro y consulta de sesiones cardiovasculares.
6.  **Progress Analytics Engine:** Motor de métricas agregadas por grupo muscular, volumen de tonelaje y 1RM.
7.  **Recommendation Engine:** Evaluación de reglas de negocio para sugerencias automáticas de carga.

### 9.2 Módulos Android (Kotlin)
1.  **Auth Module:** Vistas de Login/Registro, gestión local del token JWT en EncryptedSharedPreferences / DataStore.
2.  **Home Dashboard:** Pantalla minimalista de selección ("Empezar Rutina" vs "Ver Desempeño").
3.  **Routine Manager:** UI Compose para diseñar y estructurar programas de entrenamiento.
4.  **Live Tracker (Active Session):** Interfaz ultrarrápida con precarga de datos anteriores, temporizadores integrados y entrada mínima de teclado.
5.  **Cardio Tracker:** Formulario ágil para la captura de actividades aeróbicas.
6.  **Analytics & Charts:** Componente gráfico (Vico / MPAndroidChart) para visualizar curvas de progreso.
7.  **Sync Engine (WorkManager):** Tareas en segundo plano para sincronizar Room con Spring Boot.

---

## 10. Modelo Entidad-Relación Conceptual

Below is the relational database schema structure designed for PostgreSQL (managed via Flyway migrations):

```
 +------------------+        +---------------------+        +-----------------------+
 |     USERS        |        |   ROUTINES          |        |     ROUTINE_DAYS      |
 +------------------+        +---------------------+        +-----------------------+
 | PK id            |<-------| FK user_id          |   +--->| PK id                 |
 |    email         |        | PK id               |   |    | FK routine_id         |<---+
 |    password_hash |        |    name             |   |    |    day_name           |    |
 |    full_name     |        |    description      |   |    |    order_index        |    |
 |    sport_goal    |        |    created_at       |   |    +-----------------------+    |
 |    created_at    |        +---------------------+   |                                 |
 +------------------+                                  |                                 |
          |                                            |                                 |
          |                  +---------------------+   |    +-----------------------+    |
          |                  |  EXERCISES          |   |    | ROUTINE_EXERCISES     |    |
          |                  +---------------------+   |    +-----------------------+    |
          |                  | PK id               |   |    | PK id                 |    |
          |                  |    name             |<-------| FK exercise_id        |    |
          |                  |    target_muscle    |   |    | FK routine_day_id ----+----+
          |                  |    category         |   |    |    order_index        |
          |                  +---------------------+   |    |    target_sets        |
          |                                            |    |    target_rep_min     |
          |                                            |    |    target_rep_max     |
          |                                            |    |    rest_seconds       |
          |                                            |    |    notes              |
          |                                            |    +-----------------------+
          v                                            |
 +------------------+        +---------------------+   |
 | WORKOUT_SESSIONS |        | WORKOUT_LOGS        |   |
 +------------------+        +---------------------+   |
 | PK id            |        | PK id               |   |
 | FK user_id       |        | FK session_id       |---+
 | FK routine_day_id|------->| FK routine_exercise |
 |    start_time    |        |    overall_feeling   |
 |    end_time      |        |    notes            |
 |    status        |        +---------------------+
 +------------------+                   |
          |                             v
          |                  +---------------------+
          |                  | LOG_SETS            |
          |                  +---------------------+
          |                  | PK id               |
          |                  | FK workout_log_id   |
          |                  |    set_number       |
          |                  |    weight_kg        |
          |                  |    reps_completed   |
          |                  |    rpe              |
          |                  +---------------------+
          v
 +------------------+
 | CARDIO_LOGS      |
 +------------------+
 | PK id            |
 | FK user_id       |
 |    activity_type |
 |    duration_sec  |
 |    distance_km   |
 |    calories      |
 |    pace          |
 |    rpe           |
 |    notes         |
 |    performed_at  |
 +------------------+
```

---

## 11. Flujo Completo del Usuario

```
                         [ INICIO ]
                             |
                   ¿Usuario Autenticado?
                   /                                  (No)                     (Sí)
                /                                [ Registro / Login ]        [ PANTALLA PRINCIPAL ]
                |                    /                        [ Seleccionar Objetivo ]   /                                    |                 /                                      +----------------+                                                         |                         |
                    [ Opción A: Empezar Rutina ]    [ Opción B: Ver Desempeño ]
                                 |                         |
                       Seleccionar Rutina            Seleccionar Vista:
                         /            \              - Por Ejercicio
             (Rutina Existente)    (Crear Nueva)     - Por Rutina
                       |              |              - Por Grupo Muscular
                       v              v                    |
            [ Cargar Entrenamiento Activo ]          [ Desplegar Gráficos ]
                       |
        +-----------------------------------+
        | Cargar datos de Sesión Anterior   |
        | (Peso, Reps, RPE previo)          |
        +-----------------------------------+
                       |
            [ Registrar Serie / Ejercicio ]
                       |
         ¿Completó todos los ejercicios?
                   /                          (No)             (Sí)
                /                        (Siguiente Serie)     [ Finalizar Sesión ]
                                   |
                        [ Evaluar Reglas Negocio ]
                                   |
                        [ Mostrar Sugerencia de ]
                        [ Progresión / Carga     ]
                                   |
                         [ Sync a Backend API ]
                                   |
                              [ FIN ]
```

---

## 12. Diseño de la API REST

Todas las rutas están bajo el prefijo `/api/v1`. Respuestas en formato `application/json`.

### 12.1 Autenticación y Usuarios
*   `POST /api/v1/auth/register`
    *   **Propósito:** Registrar un nuevo usuario con su objetivo deportivo inicial.
*   `POST /api/v1/auth/login`
    *   **Propósito:** Autenticar credenciales y devolver JWT Access Token y Refresh Token.
*   `GET /api/v1/users/me`
    *   **Propósito:** Obtener el perfil del usuario autenticado y su configuración.
*   `PUT /api/v1/users/me/goal`
    *   **Propósito:** Actualizar el objetivo deportivo.

### 12.2 Rutinas
*   `GET /api/v1/routines`
    *   **Propósito:** Listar todas las rutinas creadas por el usuario.
*   `POST /api/v1/routines`
    *   **Propósito:** Crear una nueva rutina con sus días, ejercicios, series objetivo, rangos de repetición, descansos y notas.
*   `GET /api/v1/routines/{id}`
    *   **Propósito:** Obtener el detalle completo de una rutina específica.
*   `DELETE /api/v1/routines/{id}`
    *   **Propósito:** Eliminar una rutina.

### 12.3 Ejercicios
*   `GET /api/v1/exercises`
    *   **Propósito:** Obtener la lista maestra de ejercicios predeterminados y personalizados.
*   `POST /api/v1/exercises`
    *   **Propósito:** Crear un ejercicio personalizado por el usuario.

### 12.4 Sesiones de Entrenamiento y Logs
*   `POST /api/v1/workouts/session/start`
    *   **Propósito:** Iniciar una sesión de entrenamiento activa vinculada a un día de rutina.
*   `GET /api/v1/workouts/previous-log?routineExerciseId={id}`
    *   **Propósito:** Obtener los datos del entrenamiento anterior para el ejercicio seleccionado (peso, reps, RPE).
*   `POST /api/v1/workouts/session/{sessionId}/finish`
    *   **Propósito:** Finalizar la sesión, guardar todas las series realizadas, registrar la sensación general y obtener las sugerencias automáticas de progresión.

### 12.5 Cardio Log
*   `POST /api/v1/cardio`
    *   **Propósito:** Registrar una actividad de cardio (correr, bici, nadar, etc.).
*   `GET /api/v1/cardio`
    *   **Propósito:** Obtener el historial de logs cardiovasculares.

### 12.6 Analítica y Progreso
*   `GET /api/v1/analytics/exercise/{exerciseId}`
    *   **Propósito:** Obtener la serie temporal de datos (1RM estimado, peso máximo, volumen) para graficar progreso por ejercicio.
*   `GET /api/v1/analytics/muscle-group`
    *   **Propósito:** Obtener la distribución de volumen acumulado por grupo muscular.

---

## 13. Estrategia de Autenticación

### 13.1 Flujo JWT
1.  **Emisión:** Al autenticarse correctamente, el servidor Spring Boot emite:
    *   `accessToken`: Validez de 15 minutos (se envía en el header `Authorization: Bearer <token>`).
    *   `refreshToken`: Validez de 30 días (almacenado de forma segura).
2.  **Validación Backend:** Filtro `JwtAuthenticationFilter` en Spring Security que intercepta las peticiones, valida la firma y extrae el `UserId` y `Roles`.
3.  **Manejo Móvil:**
    *   Almacenamiento seguro del token en Android utilizando **EncryptedSharedPreferences** (MasterKey mediante Android Keystore).
    *   `Authenticator` personalizado de **Retrofit** que detecta respuestas HTTP 401 y solicita automáticamente la renovación con el `refreshToken` sin interrumpir la experiencia del usuario.

---

## 14. Reglas de Negocio (Motor Determinista de Sugerencias)

El sistema **NO** usa IA generativa. Aplica algoritmos deterministas basados en las variables de la sesión: **Repeticiones objetivo ($R_{target}$), Repeticiones logradas ($R_{done}$), RPE ($RPE$), Peso utilizado ($W$), Sensación General ($S$).**

### 14.1 Matriz de Decisión de Progresión (Por Ejercicio)

*   **Regla 1: Incrementar Carga (Progreso Exitoso)**
    *   *Condición:* Si $R_{done} \ge R_{target\_max}$ en TODAS las series del ejercicio **Y** $RPE \le 8$ **Y** $S \in \{	ext{Buena}, 	ext{Excelente}\}$.
    *   *Acción:* **Aumentar Peso.**
    *   *Sugerencia:* "Aumenta entre un 2.5% y 5% el peso para la próxima sesión (+1.25kg - +2.5kg por lado)."

*   **Regla 2: Incrementar Repeticiones (Consolidación de Carga)**
    *   *Condición:* Si $R_{done} < R_{target\_max}$ pero $R_{done} \ge R_{target\_min}$ **Y** $RPE \in \{7, 8\}$.
    *   *Acción:* **Mantener Peso y Aumentar Repeticiones.**
    *   *Sugerencia:* "Conserva el peso actual ($W$). Intenta agregar 1 repetición extra por serie la próxima sesión."

*   **Regla 3: Mantener Parámetros**
    *   *Condición:* Si $R_{done} \ge R_{target\_min}$ pero el $RPE = 9$ o la sensación $S = 	ext{Regular}$.
    *   *Acción:* **Mantener Peso y Repeticiones.**
    *   *Sugerencia:* "Buena intensidad. Consolida el mismo peso y repeticiones antes de subir la carga."

*   **Regla 4: Alerta de Fatiga / Reducción de Carga**
    *   *Condición:* Si $R_{done} < R_{target\_min}$ en más del 50% de las series **O** $RPE = 10$ con sensación $S = 	ext{Mala}$.
    *   *Acción:* **Reducir Peso.**
    *   *Sugerencia:* "Carga muy alta para la capacidad actual. Reduce el peso un 5% - 10% para garantizar técnica correcta."

*   **Regla 5: Recomendación de Descarga (Deload)**
    *   *Condición:* Si durante 3 sesiones consecutivas del mismo ejercicio el usuario muestra reducción de reps/peso **O** si el RPE promedio es $\ge 9.5$ de forma sostenida con sensación $S = 	ext{Mala}$.
    *   *Acción:* **Recomendar Descarga del Ejercicio/Rutina.**
    *   *Sugerencia:* "Acumulación de fatiga detectada. Se sugiere una semana de descarga (Deload) reduciendo el volumen al 50% y la carga al 70%."

---

## 15. Roadmap del MVP y Futuras Versiones

```
 +-------------------------------------------------------------------------+
 |                                ROADMAP                                  |
 +-------------------------------------------------------------------------+
 |                                                                         |
 |  [ FASE 1: MVP Core ] (4 Meses)                                         |
 |  ├── Autenticación JWT + Perfil con Objetivos                           |
 |  ├── Creador de Rutinas Básicas                                         |
 |  ├── Live Tracker (Registro < 2s, Precarga Histórica)                   |
 |  ├── Módulo Cardio Básico                                               |
 |  ├── Motor de Reglas de Progresión Deterministas                        |
 |  └── Room DB + Sincronización Básica                                    |
 |                                                                         |
 |  [ FASE 2: Analítica & UX Avanzada ] (3 Meses)                          |
 |  ├── Récords Personales (PRs) Automáticos                               |
 |  ├── Seguimiento de Volumen Semanal por Grupo Muscular                  |
 |  ├── Control de Peso Corporal y Medidas                                 |
 |  ├── Temporizador de Descanso Flotante en Pantalla                      |
 |  └── Exportación de Datos en CSV/JSON                                   |
 |                                                                         |
 |  [ FASE 3: Ecosistema & Entrenadores ] (4 Meses)                         |
 |  ├── Integración con Health Connect / Google Fit                        |
 |  ├── Modo Entrenador / Cliente (Asignación remota)                      |
 |  ├── Soporte Sistemas Avanzados (PPL, Upper/Lower, 5x5, Heavy Duty)      |
 |  └── Importación / Exportación de Rutinas mediante QR o Links           |
 +-------------------------------------------------------------------------+
```

---

## 16. Riesgos Técnicos y Mitigación

*   **Riesgo 1: Conflictos de Sincronización Offline/Online.**
    *   *Mitigación:* Implementar una estrategia de timestamping "Last Write Wins" (LWW) en Room y procesar operaciones de log como eventos inmutables.
*   **Riesgo 2: Lentitud en la Renderización de Listas en Compose durante la sesión activa.**
    *   *Mitigación:* Uso estricto de `@Stable` y `@Immutable` en Compose UI Models, claves únicas (`key`) en `LazyColumn` y diferimiento de lecturas de estado.
*   **Riesgo 3: Bloqueo de hilos en la ejecución del Motor de Progresión.**
    *   *Mitigación:* Ejecutar el motor de reglas en segundo plano utilizando `Dispatchers.Default` en Kotlin y servicios asíncronos desacoplados en Spring Boot.

---

## 17. Supuestos y Restricciones

*   **Supuesto 1:** Los usuarios utilizan teléfonos Android con versión Android 8.0 (API Level 26) o superior.
*   **Supuesto 2:** La mayoría de las interacciones de registro de series ocurren dentro del gimnasio con una sola mano libre.
*   **Restricción 1:** El MVP estará limitado únicamente a la plataforma móvil Android (Kotlin + Jetpack Compose).
*   **Restricción 2:** El backend se desplegará en contenedores Docker gestionados mediante un entorno PostgreSQL desacoplado.

---

## 18. Ideas para Versiones Futuras (Post-MVP Roadmap Extended)

1.  **Detección Automática de Récords Personales (PRs):** Notificación con animaciones al romper marcas históricas de 1RM, peso máximo o volumen por serie.
2.  **Seguimiento de Volumen Semanal Acumulado:** Gráficos de barras que comparen el volumen de series efectivas trabajadas contra el rango hipertrófico óptimo (10-20 series por grupo muscular).
3.  **Módulo Antropométrico:** Registro periódico de peso corporal, porcentaje de grasa y medidas en cm (pecho, cintura, brazos, muslos).
4.  **Temporizador de Descanso Inteligente:** Contador regresivo contextual que emite vibración auditiva/háptica al terminar el tiempo de descanso recomendado.
5.  **Integración con Health Connect & Google Fit:** Sincronización automática de calorías quemadas, pasos y frecuencia cardíaca desde smartwatches.
6.  **Plataforma Entrenador/Cliente:** Permite a entrenadores personales estructurar rutinas remotamente, asignarlas a sus clientes y auditar su cumplimiento en tiempo real.
7.  **Soporte de Metodologías Reconocidas:** Plantillas preconfiguradas para sistemas de entrenamiento famosos: *Push-Pull-Legs (PPL)*, *Upper/Lower*, *Full Body*, *Stronglifts 5x5*, *Heavy Duty (Mike Mentzer)* y *Wendler 5/3/1*.
8.  **Importación/Exportación de Rutinas mediante Código QR:** Compartir planes de entrenamiento con un par de toques entre usuarios de la comunidad.
