package de.raindancer.modules.performance.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaceRulesTest {

    @Test
    @DisplayName("the chunks are counted at once the first time, then not again within the gap")
    void censusGap() {
        CensusPace pace = new CensusPace(60_000);
        assertThat(pace.mayStart(1_000, CensusPace.NEVER)).isTrue();
        assertThat(pace.mayStart(30_000, 1_000)).isFalse();
        assertThat(pace.mayStart(61_000, 1_000)).isTrue();
    }

    @Test
    @DisplayName("a quick tick is not sampled, a long one is; between ticks only the trickle")
    void samplePace() {
        SamplePace pace = new SamplePace();
        assertThat(pace.due(5, 20)).isFalse();
        assertThat(pace.due(-1, 100)).isFalse();
        assertThat(pace.due(30, 20)).isTrue();
        assertThat(pace.due(-1, 500)).isTrue();
    }
}
