package com.powertrack.backend.domain.analytics;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionPerformanceAggregatorTest {

    private static SetSample set(int weight, int reps, int rpe) {
        return new SetSample(BigDecimal.valueOf(weight), reps, rpe);
    }

    @Test
    void eligeElSetDeMayorE1rmEntreLosQueCalificanAunqueNoSeaElDeMayorPeso() {
        // 100kg x1 -> e1RM = 100 * (1 + 1/30) = 103.33
        // 90kg x5  -> e1RM = 90 * (1 + 5/30) = 105.00 (mayor, aunque el peso sea menor)
        SetSample heaviest = set(100, 1, 9);
        SetSample bestE1rm = set(90, 5, 8);
        SessionAggregate result = SessionPerformanceAggregator.aggregate(List.of(heaviest, bestE1rm));

        assertThat(result.referenceSet()).isEqualTo(bestE1rm);
        assertThat(result.estimated1RM()).isEqualByComparingTo(BigDecimal.valueOf(105.00));
        assertThat(result.maxWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void siNingunaSerieCalificaElReferenceSetEsElDeMayorPesoYElE1rmEsNulo() {
        SetSample lowWeight = set(50, 15, 6);
        SetSample highWeight = set(80, 20, 7);
        SessionAggregate result = SessionPerformanceAggregator.aggregate(List.of(lowWeight, highWeight));

        assertThat(result.referenceSet()).isEqualTo(highWeight);
        assertThat(result.estimated1RM()).isNull();
    }

    @Test
    void elVolumenSumaTodasLasSeriesIncluidasLasQueNoCalificanParaUnRm() {
        SetSample qualifying = set(100, 8, 8);
        SetSample nonQualifying = set(60, 20, 6);
        SessionAggregate result = SessionPerformanceAggregator.aggregate(List.of(qualifying, nonQualifying));

        BigDecimal expectedVolume = BigDecimal.valueOf(100 * 8).add(BigDecimal.valueOf(60 * 20));
        assertThat(result.volumeKg()).isEqualByComparingTo(expectedVolume);
        // A pesar de que la serie que no califica tiene mayor volumen individual, sí
        // debe estar incluida en el total.
        assertThat(result.referenceSet()).isEqualTo(qualifying);
    }

    @Test
    void lanzaExcepcionConListaVacia() {
        assertThatThrownBy(() -> SessionPerformanceAggregator.aggregate(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
