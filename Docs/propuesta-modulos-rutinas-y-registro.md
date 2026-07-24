# Módulos priorizados para el MVP: Rutinas + Registro/Progreso

Arrancar por Rutinas y Registro tiene sentido: sin rutinas no hay nada que entrenar, y sin registro no hay progreso que ver. Esta propuesta aterriza esos 2 módulos en pantallas, datos y prioridades, respetando los principios de fricción mínima del flujo original.

---

## Módulo 1 — Rutinas (creación y visualización)

**Qué resuelve:** darle al usuario una rutina estructurada en días y ejercicios, para que el módulo de registro tenga de dónde tirar.

### Entidades mínimas

```
Rutina
 ├─ nombre (ej. "Heavy Duty")
 ├─ días[] (ej. Día A, Día B)
 │    └─ ejercicios[]
 │         ├─ nombre
 │         ├─ orden
 │         ├─ notas (opcional)
 │         └─ descanso_recomendado (opcional)
```

### Pantallas

1. **Lista de rutinas** — tarjetas simples (nombre + días), botón "Crear rutina".
2. **Crear/editar rutina** — nombre → agregar días → dentro de cada día, agregar ejercicios (nombre + orden, arrastrar para reordenar). Sin campos obligatorios más allá del nombre.
3. **Detalle de rutina** — muestra los días y ejercicios, botón para editar. Es la pantalla que enlaza con "Comenzar entrenamiento" desde el inicio.

### Prioridad dentro del módulo

1. Crear rutina con días y ejercicios (lo básico para que exista contenido).
2. Editar rutina existente (agregar/quitar/reordenar ejercicios).
3. Selección de "rutina activa" — la que aparece en la pantalla principal con el día correspondiente.
4. *(Después, no MVP)* plantillas prearmadas o duplicar rutinas.

---

## Módulo 2 — Registro + Progreso (performance en base a la rutina)

**Qué resuelve:** capturar la sesión con la menor fricción posible, y luego mostrar esa data en calendario y métricas, separado del flujo de entrenamiento.

### Entidades mínimas

```
Sesión
 ├─ fecha
 ├─ rutina_id / día
 ├─ sensación (opcional: 😀🙂😐😫)
 └─ registros[]
      ├─ ejercicio_id
      ├─ peso
      ├─ reps
      └─ notas (opcional)
```

**Autocompletado clave:** cada campo peso/reps se precarga con el último registro de ese mismo ejercicio (join por `ejercicio_id` ordenado por fecha desc). Esto es lo que hace que "escribir" sea solo ajustar un número, no tipear desde cero.

### Pantallas — flujo de entrenamiento

1. Ejercicio actual (peso/reps precargados + "último entrenamiento" como referencia) → Siguiente ejercicio → autoguardado.
2. Pantalla de cierre con sensación opcional → Finalizar → guarda sesión completa → recalcula métricas → vuelve al inicio.

### Pantallas — progreso (módulo separado, no interfiere con el flujo anterior)

1. **Calendario mensual** por rutina, con puntos en las fechas con sesión registrada.
2. **Detalle de fecha** — qué se hizo ese día: ejercicios, peso, reps, sensación, notas.
3. **Historial por ejercicio** — lista cronológica (semana x semana o fecha x fecha) + gráfico simple de peso/reps a lo largo del tiempo.

### Prioridad dentro del módulo

1. Guardar sesión completa (registro rápido) — es el corazón del producto.
2. Autocompletado desde el último registro (sin esto, la app pierde su propuesta de valor).
3. Calendario con marcas de sesiones.
4. Detalle de sesión por fecha.
5. Historial + gráfico por ejercicio.
6. *(Después, no MVP)* recomendaciones automáticas basadas en tendencia.

---

## Cómo se conectan

La pantalla principal solo necesita: `rutina_activa`, `día_correspondiente` (calculado por última sesión completada) y el botón de inicio. Todo lo demás vive dentro de Rutinas o de Registro/Progreso, sin cruzarse.
