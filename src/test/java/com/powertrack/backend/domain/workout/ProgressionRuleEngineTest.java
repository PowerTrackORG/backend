package com.powertrack.backend.domain.workout;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cobertura exhaustiva de las 5 reglas del motor determinista de sugerencias (PRD §14),
 * incluyendo casos límite exactos (RPE 7 vs 8 vs 9 vs 10, reps exactamente en el borde de
 * target_min/target_max) y el orden de prioridad 5 → 4 → 3 → 2 → 1 documentado en
 * {@link ProgressionRuleEngine}.
 */
class ProgressionRuleEngineTest {

    private static final int TARGET_MIN = 8;
    private static final int TARGET_MAX = 12;

    private final ProgressionRuleEngine engine = new ProgressionRuleEngine();

    private static ExerciseEvaluationInput.SetPerformance set(int weight, int reps, int rpe) {
        return new ExerciseEvaluationInput.SetPerformance(BigDecimal.valueOf(weight), reps, rpe);
    }

    private static ExerciseEvaluationInput input(List<ExerciseEvaluationInput.SetPerformance> sets,
                                                  OverallFeeling feeling,
                                                  List<HistoricalExerciseSummary> history) {
        return new ExerciseEvaluationInput(TARGET_MIN, TARGET_MAX, sets, feeling, history);
    }

    private static ExerciseEvaluationInput input(List<ExerciseEvaluationInput.SetPerformance> sets, OverallFeeling feeling) {
        return input(sets, feeling, List.of());
    }

    @Nested
    @DisplayName("Regla 1: Aumentar Peso")
    class Rule1IncreaseWeight {

        @Test
        void aumentaPesoCuandoTodasLasSeriesCumplenElMaximoConRpeBajoYSensacionBuena() {
            var sets = List.of(set(100, 12, 8), set(100, 13, 7));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isEqualTo(Recommendation.INCREASE_WEIGHT);
        }

        @Test
        void aumentaPesoConSensacionExcelente() {
            var sets = List.of(set(100, 12, 6));
            var result = engine.evaluate(input(sets, OverallFeeling.EXCELENTE));
            assertThat(result).isEqualTo(Recommendation.INCREASE_WEIGHT);
        }

        @Test
        void limiteRpeExactamenteOchoTodaviaCalifica() {
            var sets = List.of(set(100, 12, 8));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isEqualTo(Recommendation.INCREASE_WEIGHT);
        }

        @Test
        void rpeNueveYaNoCalificaParaAumentarPesoYCaeEnMantener() {
            // Todas las series cumplen target_max, pero RPE=9 rompe la condición de
            // Regla 1 (RPE <= 8) y activa la Regla 3 (RPE == 9).
            var sets = List.of(set(100, 12, 9));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isEqualTo(Recommendation.MAINTAIN);
        }

        @Test
        void unaSolaSerieQueNoLlegaAlMaximoRompeLaRegla1() {
            var sets = List.of(set(100, 12, 7), set(100, 11, 7));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isNotEqualTo(Recommendation.INCREASE_WEIGHT);
        }

        @Test
        void sensacionRegularNoCalificaParaAumentarPeso() {
            var sets = List.of(set(100, 12, 7));
            var result = engine.evaluate(input(sets, OverallFeeling.REGULAR));
            assertThat(result).isNotEqualTo(Recommendation.INCREASE_WEIGHT);
        }
    }

    @Nested
    @DisplayName("Regla 2: Mantener Peso y Aumentar Repeticiones")
    class Rule2IncreaseReps {

        @Test
        void aumentaRepsCuandoRepsEntreMinYMaxConRpeSiete() {
            var sets = List.of(set(100, 10, 7));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isEqualTo(Recommendation.INCREASE_REPS);
        }

        @Test
        void aumentaRepsConRpeOcho() {
            var sets = List.of(set(100, 10, 8));
            var result = engine.evaluate(input(sets, OverallFeeling.EXCELENTE));
            assertThat(result).isEqualTo(Recommendation.INCREASE_REPS);
        }

        @Test
        void limiteRepsExactamenteEnTargetMinCalifica() {
            var sets = List.of(set(100, TARGET_MIN, 7));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isEqualTo(Recommendation.INCREASE_REPS);
        }

        @Test
        void repsUnoPorDebajoDeTargetMinNoCalifica() {
            var sets = List.of(set(100, TARGET_MIN - 1, 7));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isNotEqualTo(Recommendation.INCREASE_REPS);
        }

        @Test
        void repsIgualATargetMaxNoCalificaParaRegla2SinoParaRegla1SiRpeBajo() {
            var sets = List.of(set(100, TARGET_MAX, 7));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isEqualTo(Recommendation.INCREASE_WEIGHT);
        }

        @Test
        void rpeSeisNoCalificaParaAumentarReps() {
            var sets = List.of(set(100, 10, 6));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isNotEqualTo(Recommendation.INCREASE_REPS);
        }

        @Test
        void rpeNueveNoCalificaParaAumentarRepsSinoParaMantener() {
            var sets = List.of(set(100, 10, 9));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isEqualTo(Recommendation.MAINTAIN);
        }
    }

    @Nested
    @DisplayName("Regla 3: Mantener Peso y Repeticiones")
    class Rule3Maintain {

        @Test
        void mantieneCuandoRpeEsExactamenteNueve() {
            var sets = List.of(set(100, 10, 9));
            var result = engine.evaluate(input(sets, OverallFeeling.EXCELENTE));
            assertThat(result).isEqualTo(Recommendation.MAINTAIN);
        }

        @Test
        void mantieneCuandoSensacionEsRegularAunqueRpeSeaBajo() {
            var sets = List.of(set(100, 10, 6));
            var result = engine.evaluate(input(sets, OverallFeeling.REGULAR));
            assertThat(result).isEqualTo(Recommendation.MAINTAIN);
        }

        @Test
        void noMantieneSiRepsNoLlegaATargetMin() {
            var sets = List.of(set(100, TARGET_MIN - 1, 9));
            var result = engine.evaluate(input(sets, OverallFeeling.EXCELENTE));
            assertThat(result).isNotEqualTo(Recommendation.MAINTAIN);
        }
    }

    @Nested
    @DisplayName("Regla 4: Reducir Peso")
    class Rule4DecreaseWeight {

        @Test
        void reduceCuandoMasDelCincuentaPorCientoDeSeriesNoLlegaAlMinimo() {
            // 3 de 4 series (75%) por debajo de target_min, con RPE/sensación neutrales:
            // demuestra que esta cláusula por sí sola dispara la regla.
            var sets = List.of(set(100, 5, 6), set(100, 6, 6), set(100, 7, 6), set(100, 11, 6));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isEqualTo(Recommendation.DECREASE_WEIGHT);
        }

        @Test
        void exactamenteCincuentaPorCientoNoEsMayoriaYNoDispara() {
            // 2 de 4 series (50%) por debajo del mínimo: "más del 50%" exige estrictamente
            // mayor, así que esto NO debe disparar la Regla 4 por esta cláusula.
            var sets = List.of(set(100, 5, 6), set(100, 6, 6), set(100, 11, 6), set(100, 11, 6));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isNotEqualTo(Recommendation.DECREASE_WEIGHT);
        }

        @Test
        void reduceCuandoRpeDiezConSensacionMalaAunqueLasRepsSeanExcelentes() {
            // Reps perfectas (cumplirían Regla 1), pero RPE=10 + sensación Mala prevalece
            // por la prioridad 4 > 1.
            var sets = List.of(set(100, 12, 10), set(100, 13, 10));
            var result = engine.evaluate(input(sets, OverallFeeling.MALA));
            assertThat(result).isEqualTo(Recommendation.DECREASE_WEIGHT);
        }

        @Test
        void rpeDiezConSensacionBuenaNoDisparaLaClausulaDeFatigaAguda() {
            var sets = List.of(set(100, 12, 10));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA));
            assertThat(result).isNotEqualTo(Recommendation.DECREASE_WEIGHT);
        }

        @Test
        void rpeNueveConSensacionMalaNoDisparaLaClausulaDeFatigaAguda() {
            // Límite exacto: la cláusula exige RPE == 10, no >= 10.
            var sets = List.of(set(100, 12, 9));
            var result = engine.evaluate(input(sets, OverallFeeling.MALA));
            assertThat(result).isNotEqualTo(Recommendation.DECREASE_WEIGHT);
        }
    }

    @Nested
    @DisplayName("Regla 5: Recomendar Descarga (Deload)")
    class Rule5Deload {

        @Test
        void nombreDeSesionesInsuficienteNoActivaDeloadAunqueLaSesionActualSeaMala() {
            var history = List.of(
                    new HistoricalExerciseSummary(BigDecimal.valueOf(90), 10, 9.8, OverallFeeling.MALA));
            var sets = List.of(set(80, 12, 6));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA, history));
            assertThat(result).isEqualTo(Recommendation.INCREASE_WEIGHT);
        }

        @Test
        void deloadPorTendenciaDeCaidaDePesoEnTresSesionesConsecutivasGanaSobreAumentarPeso() {
            // recentHistory: más reciente primero. oldest=100 > newest=90 > current=80.
            var history = List.of(
                    new HistoricalExerciseSummary(BigDecimal.valueOf(90), 12, 7, OverallFeeling.BUENA),
                    new HistoricalExerciseSummary(BigDecimal.valueOf(100), 12, 7, OverallFeeling.BUENA));
            // La sesión actual, aislada, cumpliría totalmente la Regla 1 (Aumentar Peso).
            var sets = List.of(set(80, 12, 6), set(80, 13, 6));
            var result = engine.evaluate(input(sets, OverallFeeling.EXCELENTE, history));
            assertThat(result).isEqualTo(Recommendation.DELOAD);
        }

        @Test
        void deloadPorTendenciaDeCaidaDeRepeticiones() {
            var history = List.of(
                    new HistoricalExerciseSummary(BigDecimal.valueOf(100), 10, 7, OverallFeeling.BUENA),
                    new HistoricalExerciseSummary(BigDecimal.valueOf(100), 12, 7, OverallFeeling.BUENA));
            var sets = List.of(set(100, 8, 7));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA, history));
            assertThat(result).isEqualTo(Recommendation.DELOAD);
        }

        @Test
        void deloadPorRpePromedioSostenidoMayorOIgualANueveYMedioConSensacionMala() {
            var history = List.of(
                    new HistoricalExerciseSummary(BigDecimal.valueOf(100), 10, 9.5, OverallFeeling.MALA),
                    new HistoricalExerciseSummary(BigDecimal.valueOf(100), 10, 9.5, OverallFeeling.MALA));
            // Promedio de la ventana: (9.5 + 9.5 + 9.5) / 3 = 9.5 exacto (límite, >= dispara).
            var sets = List.of(set(100, 10, 9), set(100, 10, 10));
            var result = engine.evaluate(input(sets, OverallFeeling.MALA, history));
            assertThat(result).isEqualTo(Recommendation.DELOAD);
        }

        @Test
        void promedioDeRpeLigeramentePorDebajoDelUmbralNoActivaFatigaSostenida() {
            var history = List.of(
                    new HistoricalExerciseSummary(BigDecimal.valueOf(100), 12, 9.0, OverallFeeling.MALA),
                    new HistoricalExerciseSummary(BigDecimal.valueOf(100), 12, 9.0, OverallFeeling.MALA));
            var sets = List.of(set(100, 12, 9));
            var result = engine.evaluate(input(sets, OverallFeeling.MALA, history));
            assertThat(result).isNotEqualTo(Recommendation.DELOAD);
        }

        @Test
        void sinTendenciaDeCaidaYSinFatigaSostenidaNoActivaDeload() {
            var history = List.of(
                    new HistoricalExerciseSummary(BigDecimal.valueOf(90), 10, 7, OverallFeeling.BUENA),
                    new HistoricalExerciseSummary(BigDecimal.valueOf(95), 10, 7, OverallFeeling.BUENA));
            var sets = List.of(set(100, 12, 7));
            var result = engine.evaluate(input(sets, OverallFeeling.BUENA, history));
            assertThat(result).isNotEqualTo(Recommendation.DELOAD);
        }
    }

    @Nested
    @DisplayName("Casos por defecto / validación de entrada")
    class DefaultsAndValidation {

        @Test
        void casoNoContempladoPorLaMatrizCaeEnMantenerPorDefecto() {
            // Gap documentado: 1 de 4 series (25%, no mayoría) por debajo de target_min,
            // RPE bajo y sensación excelente. Ninguna de las 5 reglas aplica literalmente.
            var sets = List.of(set(100, 7, 6), set(100, 9, 6), set(100, 9, 6), set(100, 9, 6));
            var result = engine.evaluate(input(sets, OverallFeeling.EXCELENTE));
            assertThat(result).isEqualTo(Recommendation.MAINTAIN);
        }

        @Test
        void rechazaSetsVacios() {
            assertThatThrownBy(() -> new ExerciseEvaluationInput(TARGET_MIN, TARGET_MAX, List.of(),
                    OverallFeeling.BUENA, List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rechazaRpeFueraDeRango() {
            assertThatThrownBy(() -> set(100, 10, 11)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> set(100, 10, 0)).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
