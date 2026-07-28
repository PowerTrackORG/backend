package com.powertrack.backend.domain.analytics;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressClassifierTest {

    private static SetSample set(int weight, int reps, int rpe) {
        return new SetSample(BigDecimal.valueOf(weight), reps, rpe);
    }

    @Test
    void progresaCuandoSubeElPesoConRepsYRpeIgualesOMejores() {
        SetSample previous = set(90, 8, 8);
        SetSample last = set(100, 8, 7);
        assertThat(ProgressClassifier.classify(previous, last)).isEqualTo(ProgressStatus.PROGRESSED);
    }

    @Test
    void mantieneCuandoPesoRepsYRpeSonExactamenteIguales() {
        SetSample previous = set(100, 8, 8);
        SetSample last = set(100, 8, 8);
        assertThat(ProgressClassifier.classify(previous, last)).isEqualTo(ProgressStatus.MAINTAINED);
    }

    @Test
    void regresaCuandoBajaElPeso() {
        SetSample previous = set(100, 8, 8);
        SetSample last = set(90, 8, 8);
        assertThat(ProgressClassifier.classify(previous, last)).isEqualTo(ProgressStatus.REGRESSED);
    }

    @Test
    void casoDelGapPesoSubeRepsBajanRpeSubeCaeEnMantenerPorDefecto() {
        // Gap documentado en el Javadoc de ProgressClassifier: ninguna de las 3 reglas
        // aplica literalmente cuando el peso sube pero las reps bajan y el RPE también sube.
        SetSample previous = set(90, 10, 7);
        SetSample last = set(100, 8, 9);
        assertThat(ProgressClassifier.classify(previous, last)).isEqualTo(ProgressStatus.MAINTAINED);
    }
}
