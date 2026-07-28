# Módulos y Flujos

## Auth (backend)

**Objetivo:** dar de alta usuarios y emitir/validar JWT para que el resto de los módulos puedan asociar datos a un `userId` autenticado.

**Compone:** `POST /api/v1/auth/register`, `POST /api/v1/auth/login` (ver `Docs/api-endpoints.md`).

**Flujo de datos (registro):**
```
AuthController (adaptador in)
  -> RegisterUserUseCase (puerto in)
  -> RegisterUserService (application)
       -> UserRepositoryPort.existsByEmail  -> si existe: EmailAlreadyRegisteredException (409)
       -> PasswordHasherPort.hash           (BCrypt, adaptador infra)
       -> User.register(...)                (dominio puro, sin JPA/Spring)
       -> UserRepositoryPort.save           (adaptador JPA -> tabla `users`, migración V1)
       -> TokenProviderPort.generate*Token  (JWT HS256, adaptador infra)
  <- AuthResult { userId, email, accessToken, refreshToken }
```

**Flujo de datos (login):** igual pero vía `AuthenticateUserUseCase` — busca por email, compara hash con `PasswordHasherPort.matches`, mismo `InvalidCredentialsException` genérico tanto si el email no existe como si la password es incorrecta (evita enumeración de usuarios).

**Requests protegidos:** `JwtAuthenticationFilter` (adaptador in, infraestructura pura — no pasa por ningún puerto de aplicación) valida el `Bearer` token en cada request no-auth y puebla el `SecurityContext` con el `userId` como principal.

**Refresh (2026-07-23):** `POST /auth/refresh` — `RefreshAccessTokenService` valida el refresh token vía `TokenProviderPort.validateRefreshTokenAndGetUserId` (implementado en `JwtTokenAdapter` reutilizando `parseClaims` + el claim `type`), busca el usuario y emite un `accessToken` nuevo. Sin rotación del refresh token (no hay revocación en el MVP). Habilita el diseño offline-first: el access token dura 15 min pero el refresh (90 días) sostiene sesiones largas sin señal. Detalle y motivo completo en `Docs/decisiones-tecnicas.md`.

**Estado:** implementado y verificado end-to-end (compilación, tests unitarios de `RegisterUserService`/`AuthenticateUserService`, y prueba manual contra Postgres real vía docker-compose: registro, duplicado, login OK, login con password incorrecta, validación de input, endpoint protegido sin token).

**Próximo módulo a construir:** ver estado global al final de este documento.

---

## Rutinas (backend)

**Objetivo:** permitir que el usuario cree, liste, consulte y elimine sus rutinas de entrenamiento (nombre + días ordenados + ejercicios con targets propios), y mantener un catálogo de ejercicios (predefinido + personalizado por usuario).

**Compone:** `POST/GET /api/v1/routines`, `GET/DELETE /api/v1/routines/{id}`, `GET/POST /api/v1/exercises` (ver `Docs/api-endpoints.md`). Tablas `exercises`, `routines`, `routine_days`, `routine_exercises` (migraciones `V2`, seed en `V3`).

**Modelo:** `Routine` es el agregado raíz (dominio puro) con lista inmutable de `RoutineDay`, cada uno con lista inmutable de `RoutineExercise`. Se modela como un único agregado (no entidades sueltas) porque en el MVP una rutina completa se crea, lee y elimina siempre como unidad.

`RoutineExercise` vs `Exercise`: `Exercise` es el catálogo global (nombre, músculo, categoría — puede ser predefinido o personalizado vía `createdByUserId` nullable). `RoutineExercise` es la personalización real del usuario dentro de una rutina: apunta a un `exerciseId` del catálogo pero define sus propios `targetSets`/`targetRepMin`/`targetRepMax`/`restSeconds`. Esta separación es la base de la decisión documentada en `Docs/decisiones-tecnicas.md` sobre qué ancla usa el registro de progreso.

**Idempotencia y offline (2026-07-23):** `routineId` lo genera el **cliente**, no el servidor — permite crear una rutina offline y sincronizarla después sin duplicar si el request se reintenta. Detalle y motivo en `Docs/decisiones-tecnicas.md`.

**Flujo de datos (creación):**
```
RoutineController.create
  -> CreateRoutineUseCase / CreateRoutineService
       -> RoutineRepositoryPort.findByIdAndUserId(routineId, userId)
            -> si ya existe: devuelve la rutina existente tal cual (idempotencia de reintento)
            -> si existe para otro usuario: 400 (colisión de ID)
       -> ExerciseRepositoryPort.findAllByIds  (valida que TODOS los exerciseId referenciados existan)
            -> si falta alguno: ExerciseNotFoundException (404)
       -> Routine.create + RoutineDay.create + RoutineExercise.create  (dominio puro, valida invariantes: rangos de reps, targetSets >= 1, etc.)
       -> RoutineRepositoryPort.save  (adaptador JPA -> routines/routine_days/routine_exercises)
  <- RoutineDetailResult (incluye exerciseName resuelto contra el catálogo)
```

**Ownership (decisión de diseño):** `GetRoutineDetailService`/`DeleteRoutineService` filtran por dueño a nivel de query (`findByIdAndUserId` / `deleteByIdAndUserId`). Si la rutina existe pero es de otro usuario, se trata igual que "no existe" y se lanza `RoutineNotFoundException` → `404` (no `403`), siguiendo el mismo criterio anti-enumeración que `InvalidCredentialsException` en Auth. Detalle en `Docs/decisiones-tecnicas.md`.

**Estado:** implementado (tests unitarios `CreateRoutineServiceTest`, `GetRoutineDetailServiceTest`).

---

## Registro/Progreso — Workout Execution (backend)

**Objetivo:** registrar la ejecución real de una sesión de entrenamiento (series, peso, reps, RPE por ejercicio) contra los targets definidos en la rutina, y devolver una sugerencia de progresión determinista por ejercicio al finalizar (motor de reglas, no IA).

**Compone:** `POST /api/v1/workouts/session/start`, `GET /api/v1/workouts/previous-log`, `POST /api/v1/workouts/session/{id}/finish` (ver `Docs/api-endpoints.md`). Tablas `workout_sessions`, `workout_logs`, `log_sets` (migración `V4`).

**Conexión con Rutinas:** el registro no apunta a `Exercise` sino a `routineExerciseId` (el `RoutineExercise` de la rutina activa), tanto al iniciar sesión (`routineDayId`) como al loguear cada ejercicio (`routineExerciseId` dentro de `finish`). Así, el motor de sugerencias siempre compara contra los targets que el propio usuario configuró, no contra un target genérico del catálogo.

**Idempotencia y offline (2026-07-23):** `sessionId` (en `start`) y `startTime`/`endTime` (en `start`/`finish`) los provee el **cliente**, no el servidor — ver `Docs/decisiones-tecnicas.md`. Esto permite que "iniciar" y "finalizar" una sesión entrenada offline se sincronicen después preservando el momento real en que ocurrieron, y que reintentar `start` tras un corte de red sea un no-op en vez de crear una sesión duplicada. Un `409` en un reintento de `finish` se interpreta del lado del cliente como "ya se sincronizó con éxito", no como error.

**Flujo de datos (start → finish):**
```
POST /session/start { sessionId, routineDayId, startTime }
  -> WorkoutSessionRepositoryPort.findByIdAndUserId(sessionId, userId)
       -> si ya existe: devuelve la sesión existente tal cual (idempotencia de reintento)
       -> si existe para otro usuario: 400 (colisión de ID)
  -> RoutineRepositoryPort.existsRoutineDayOwnedByUser  (reusa el ownership check de Rutinas, no lo duplica)
       -> si no pertenece: RoutineDayNotFoundException (404)
  -> WorkoutSession.start (status = IN_PROGRESS)
  <- { sessionId, routineDayId, startTime }

[cliente entrena, opcionalmente consulta GET /previous-log?routineExerciseId=... para prellenar pesos/reps de la sesión anterior]

POST /session/{id}/finish { overallFeeling, exerciseLogs: [...], endTime }
  -> WorkoutSessionRepositoryPort.findByIdAndUserId  -> 404 si no es del usuario; 409 si ya estaba COMPLETED
  -> por cada exerciseLog:
       -> RoutineRepositoryPort.findRoutineExerciseTargets  -> 404 si el routineExerciseId no es del usuario
       -> WorkoutLog.create + LogSet.create               (dominio puro)
       -> WorkoutLogRepositoryPort.findRecentSessionSummaries(lookback=2)  (histórico ANTES de persistir la sesión actual, la excluye naturalmente)
       -> ProgressionRuleEngine.evaluate(ExerciseEvaluationInput)  -> Recommendation
  -> WorkoutSession.finish(overallFeeling, logs)  (status = COMPLETED)
  -> WorkoutSessionRepositoryPort.save
  <- { sessionId, startTime, endTime, overallFeeling, suggestions: [{ routineExerciseId, recommendation, message }] }
```

**Motor de sugerencias (`ProgressionRuleEngine`, PRD §14):** evalúa 5 reglas en orden de prioridad **5 → 4 → 3 → 2 → 1** (de mayor a menor severidad), no 1 → 5. Razón: las señales de riesgo/fatiga (Regla 5 Deload, Regla 4 Reducir Peso) deben ganar siempre sobre cualquier señal de progreso (Reglas 1-3), aunque varias condiciones se cumplan a la vez — progresar sobre fatiga no detectada sería el peor resultado posible del motor. Entre las señales de progreso, gana el criterio más conservador (Regla 3 Mantener) sobre los más agresivos.

La Regla 5 (Deload) necesita una ventana de 3 sesiones consecutivas del mismo ejercicio: 2 sesiones históricas `COMPLETED` (consultadas antes de persistir la sesión actual) + la sesión que se está finalizando. Si no hay histórico suficiente, la regla simplemente no aplica. Un caso no cubierto por la matriz del PRD (minoría de series bajo `target_min` sin que ninguna otra condición aplique) se resolvió como `Recommendation.MAINTAIN` por defecto — decisión confirmada por el usuario, no queda pendiente. Detalle completo de supuestos (RPE máximo/mínimo por ejercicio, etc.) en el Javadoc de `ProgressionRuleEngine` y resumen en `Docs/decisiones-tecnicas.md`.

**Estado:** implementado (tests unitarios `StartWorkoutSessionServiceTest`, `GetPreviousLogServiceTest`, `FinishWorkoutSessionServiceTest`, `ProgressionRuleEngineTest`).

---

## Perfil de Usuario (backend)

**Objetivo:** exponer y permitir actualizar los datos de perfil del usuario autenticado, en particular el `sportGoal` (objetivo deportivo) capturado en el registro.

**Compone:** `GET /api/v1/users/me`, `PUT /api/v1/users/me/goal` (ver `Docs/api-endpoints.md`).

**Flujo de datos:**
```
UserController.getMyProfile / updateMySportGoal
  -> GetUserProfileUseCase / UpdateSportGoalUseCase
       -> UserRepositoryPort.findById  -> 404 si no existe (UserNotFoundException)
       -> [update] User.withSportGoal(newGoal)  (dominio puro, devuelve instancia nueva inmutable)
       -> UserRepositoryPort.save
  <- UserProfileResult { id, email, fullName, sportGoal, createdAt }
```

**Decisión de diseño:** este módulo no crea infraestructura de persistencia propia — reusa `UserRepositoryPort` (puerto de salida) ya definido y adaptado por Auth (`application/auth/port/out`). Evita duplicar el adaptador JPA de `users` para un módulo que solo necesita lectura/actualización simple sobre la misma tabla.

**Estado:** implementado (tests unitarios `GetUserProfileServiceTest`, `UpdateSportGoalServiceTest`).

---

## Analítica y Progreso (backend)

**Objetivo:** dar visibilidad de desempeño histórico sobre datos ya registrados en Registro/Progreso: tendencia de fuerza por ejercicio, balance de tonelaje por grupo muscular, y progreso por ejercicio dentro de una rutina (RF-07, CU-05).

**Compone:** `GET /api/v1/analytics/exercise/{exerciseId}`, `GET /api/v1/analytics/muscle-group`, `GET /api/v1/analytics/routine/{routineId}` (ver `Docs/api-endpoints.md`). No agrega tablas ni migración propia — lee `workout_sessions`/`workout_logs`/`log_sets` (`V4`) y `exercises`/`routine_exercises` (`V2`/`V3`) vía `AnalyticsRepositoryPort`, con índices ya existentes.

**Conexión con Rutinas y Registro/Progreso:** no duplica el motor de sugerencias (`ProgressionRuleEngine`) ni sus excepciones — reusa `ExerciseNotFoundException` (catálogo) y `RoutineNotFoundException` (ownership) tal cual las define el módulo Rutinas. El endpoint "por rutina" resuelve el ejercicio siempre vía `RoutineExercise` (mismo ancla que Registro/Progreso, ver `Docs/decisiones-tecnicas.md` entrada 2026-07-22), nunca contra `Exercise` directamente.

**Dominio puro compartido por los 3 endpoints:**
- `domain/workout/OneRepMaxCalculator` — estima 1RM (fórmula de Epley) solo sobre series con 1-12 reps.
- `domain/analytics/SessionPerformanceAggregator` — reduce todas las series de una sesión a `SessionAggregate` (volumen total, peso máximo, promedios de reps/RPE, serie de referencia y su 1RM estimado).
- `domain/analytics/ProgressClassifier` — compara la serie de referencia de 2 sesiones consecutivas y clasifica `PROGRESSED|MAINTAINED|REGRESSED` (nunca decide `INSUFFICIENT_DATA`, eso lo resuelve el caller según cuántas sesiones históricas hay).

Las 3 clases son deterministas, sin dependencias de Spring/JPA — mismo criterio arquitectónico que `ProgressionRuleEngine`. Detalle de cada regla/decisión en `Docs/decisiones-tecnicas.md` (entradas 2026-07-28).

**Flujo de datos (por ejercicio):**
```
GET /analytics/exercise/{exerciseId}?from&to
  -> GetExerciseAnalyticsService
       -> ExerciseRepositoryPort.findById  -> 404 (ExerciseNotFoundException) si no existe en el catálogo
       -> ventana default: histórico completo (EPOCH -> now) si from/to no vienen
       -> AnalyticsRepositoryPort.findCompletedSessionSetsForExercise(exerciseId, userId, from, to)
            (combina en un solo punto las series de distintos routineExercise del mismo exerciseId
             si aparecen en la misma sesión)
       -> por cada sesión: SessionPerformanceAggregator.aggregate(sets) -> SessionAggregate
  <- { exerciseId, exerciseName, points: [...] }  (ordenados por sessionDate ascendente)
```

**Flujo de datos (por grupo muscular):**
```
GET /analytics/muscle-group?from&to
  -> GetMuscleGroupVolumeService
       -> ventana default: últimos 7 días si from/to no vienen
       -> AnalyticsRepositoryPort.sumVolumeByMuscleGroup(userId, from, to)  (agregación SQL directa, sin pasar por el dominio)
  <- [{ targetMuscle, totalVolumeKg }]  (ordenado por volumen descendente)
```

**Flujo de datos (por rutina):**
```
GET /analytics/routine/{routineId}?from&to
  -> GetRoutineAnalyticsService
       -> RoutineRepositoryPort.findByIdAndUserId  -> 404 (RoutineNotFoundException) si no existe o no es del usuario
       -> ventana default: últimos 30 días si from/to no vienen
       -> AnalyticsRepositoryPort.sumVolumeForRoutine / findCompletedSessionEndTimesForRoutine  (acotado por from/to)
       -> AnalyticsRepositoryPort.findLastTwoSessionsPerRoutineExercise(TODOS los routineExerciseId de la rutina, userId)
            (SIN filtrar por from/to: la clasificación de progreso siempre mira las 2 últimas sesiones reales,
             una sola llamada para toda la rutina para evitar N+1)
       -> por cada RoutineDay/RoutineExercise configurado en la rutina (orderIndex, igual que RoutineDetailResponse):
            -> si hay <2 sesiones históricas: ProgressStatus.INSUFFICIENT_DATA
            -> si hay >=2: SessionPerformanceAggregator.aggregate() sobre cada una de las 2 últimas
                            -> ProgressClassifier.classify(previousAggregate.referenceSet(), lastAggregate.referenceSet())
  <- { routineId, routineName, window, totalVolumeKg, summary, byDay: [...] }
```

**Estado:** implementado (tests unitarios `SessionPerformanceAggregatorTest`, `ProgressClassifierTest`, `GetExerciseAnalyticsServiceTest`, `GetMuscleGroupVolumeServiceTest`, `GetRoutineAnalyticsServiceTest`). Build y suite completa del backend (84 tests) en verde. **Sin commitear todavía** al momento de esta entrada (2026-07-28).

---

**Próximo módulo a construir (estado real, 2026-07-28):** queda por implementar Cardio Log (PRD §12.5, RF-05, CU-04) — ver alcance en `Docs/propuesta-modulos-rutinas-y-registro.md`. Va a seguir el mismo patrón hexagonal ya usado en el resto de los módulos.
