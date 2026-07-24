# Historial de Requerimientos del MVP — Responsabilidad Backend vs Mobile

Ahora que `backend` y `mobile` son repos separados, este documento existe para que los agentes de cada repo (y quien retome el proyecto) tengan claro, sin tener que abrir el otro repo, qué parte de cada requerimiento del PRD (`Docs/documentacion_funcional_tecnica_fitness_mvp.md`) le corresponde a este lado y qué ya está resuelto.

---

## 1. Casos de Uso (PRD §4)

| ID | Caso de Uso | Backend | Mobile | Estado |
|---|---|---|---|---|
| CU-01 | Autenticación e Inicio de Sesión | API de registro/login/refresh + JWT | UI de login/registro, guardado seguro del token | **Backend: implementado.** Mobile: pantalla `AuthScreen` existe, falta verificar conexión real. |
| CU-02 | Gestión de Rutinas | CRUD completo + catálogo de ejercicios | UI de listar/crear/editar rutina | **Backend: implementado.** Mobile: pantallas `RoutinesScreen`, `RoutineCreateScreen`, `RoutineDetailScreen` existen (scaffold). |
| CU-03 | Ejecución de Entrenamiento Activo | Iniciar sesión, precarga de último log, finalizar + sugerencias | UI de Live Tracker con precarga y teclado optimizado | **Backend: implementado** (incluye motor de progresión). Mobile: pantalla `LiveTrackerScreen` existe (scaffold). |
| CU-04 | Registro de Actividad Cardiovascular | Endpoints `/cardio` (crear, listar) | Formulario de captura de cardio | **Pendiente en ambos lados.** No hay endpoint ni pantalla todavía. |
| CU-05 | Consulta de Historial y Análisis de Progreso | Endpoints `/analytics/*` (agregados por ejercicio/grupo muscular) | Gráficos (Vico/MPAndroidChart) | **Pendiente en ambos lados.** `PerformanceScreen` existe como pantalla pero sin los endpoints de analítica detrás. |
| CU-06 | Generación de Sugerencias Automáticas de Carga | `ProgressionRuleEngine` (5 reglas, ver `Docs/decisiones-tecnicas.md`) | Solo mostrar el `recommendation`/`message` que ya devuelve `finish` | **Backend: implementado y probado.** Mobile: pendiente de consumir el campo `suggestions` de la respuesta. |

---

## 2. Requisitos Funcionales (PRD §6)

| ID | Requisito | Responsabilidad | Estado |
|---|---|---|---|
| RF-01 | Registro con email/password + objetivo deportivo obligatorio | Backend: validación + persistencia · Mobile: formulario + selector de objetivo | Backend implementado; mobile scaffold pendiente de verificar |
| RF-02 | Pantalla Hub ("Empezar Rutina" / "Ver Desempeño") | Mobile exclusivo | Pendiente de confirmar en `Screen.kt`/`PowerTrackNavGraph.kt` |
| RF-03 | Creador de rutinas (días, ejercicios, series, rangos, descanso, notas) | Backend: modelo + API · Mobile: UI de armado | Backend implementado; mobile scaffold (`RoutineCreateScreen`) |
| RF-04 | Motor de ejecución (historial previo + captura de peso/reps/RPE/sensación) | Backend: `previous-log` + `finish` · Mobile: UI de captura | Backend implementado; mobile scaffold (`LiveTrackerScreen`) |
| RF-05 | Módulo cardiovascular | Backend: API · Mobile: formulario | **No implementado en ninguno de los dos lados** |
| RF-06 | Motor de sugerencias automáticas | Backend exclusivo (dominio puro, sin UI) | Implementado (`ProgressionRuleEngine`) |
| RF-07 | Visualización de progreso (gráficos por ejercicio/rutina/grupo muscular) | Backend: agregación · Mobile: renderizado de gráficos | **No implementado en ninguno de los dos lados** |

---

## 3. Requisitos No Funcionales (PRD §7)

| ID | Requisito | Responsabilidad |
|---|---|---|
| RNF-01 | Registro de serie responde en <100ms | Mobile exclusivo (percepción de UI) |
| RNF-02 | Disponibilidad offline (lectura/escritura local) | Mobile exclusivo (Room), con **contrato de soporte del backend**: IDs generados por cliente + timestamps de cliente para que la sincronización diferida sea idempotente (ver `Docs/decisiones-tecnicas.md`, entrada "base offline-first") |
| RNF-03 | JWT firmado, expiración de 15 min access / 90 días refresh | Backend exclusivo |
| RNF-04 | 95% de requests REST < 200ms | Backend exclusivo |
| RNF-05 | Arquitectura de software | Backend: Hexagonal (Ports & Adapters) · Mobile: MVVM + Clean Architecture ligera — cada lado es dueño de su propia decisión, no hay acoplamiento entre ambas |

---

## 4. Resumen para agentes

- **`backend-java-senior`** puede asumir como terreno firme: Auth, Perfil, Rutinas y Registro/Progreso (incluyendo el motor de sugerencias) ya están implementados y probados. El siguiente trabajo pendiente de MVP en este repo es **Cardio Log** y **Analítica/Progreso** (RF-05, RF-07, CU-04, CU-05) — ninguno de los dos tiene ni endpoint ni entidad todavía.
- **`mobile-designer`** (en el repo `mobile`) parte de un scaffold con las pantallas ya creadas (Auth, Splash, Routines list/create/detail, Live Tracker, Performance, Profile) pero **sin confirmar que estén conectadas de punta a punta a la API real** — y con dos pendientes ya reportados en `Docs/decisiones-tecnicas.md` (ícono launcher genérico, pantallas no responsive). No existe todavía ninguna pantalla de Cardio.
- **`fitness-science-expert`** es el que valida que, cuando se construyan Cardio y Analítica, las reglas de negocio (ej. cómo se calcula 1RM estimado, qué cuenta como "volumen efectivo" por grupo muscular) sean fisiológicamente correctas antes de que `backend-java-senior` las implemente y `mobile-designer` las visualice.
