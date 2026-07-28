package com.powertrack.backend.domain.workout;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OneRepMaxCalculatorTest {

    @Test
    void unaRepeticionCalifica() {
        assertThat(OneRepMaxCalculator.qualifies(1)).isTrue();
    }

    @Test
    void doceRepeticionesCalifican() {
        assertThat(OneRepMaxCalculator.qualifies(12)).isTrue();
    }

    @Test
    void ceroRepeticionesNoCalifica() {
        assertThat(OneRepMaxCalculator.qualifies(0)).isFalse();
    }

    @Test
    void treceRepeticionesNoCalifica() {
        assertThat(OneRepMaxCalculator.qualifies(13)).isFalse();
    }

    @Test
    void calculaLaFormulaDeEpleyExacta() {
        // Epley: 100 * (1 + 10/30) = 100 * 1.333... = 133.33
        BigDecimal result = OneRepMaxCalculator.estimate(BigDecimal.valueOf(100), 10);
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(133.33));
    }

    @Test
    void unaRepeticionDaElMismoPesoComoEstimacion() {
        BigDecimal result = OneRepMaxCalculator.estimate(BigDecimal.valueOf(100), 1);
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(103.33));
    }

    @Test
    void rechazaRepsFueraDeRango() {
        assertThatThrownBy(() -> OneRepMaxCalculator.estimate(BigDecimal.valueOf(100), 13))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OneRepMaxCalculator.estimate(BigDecimal.valueOf(100), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
