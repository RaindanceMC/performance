package de.raindancer.modules.performance.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class SlicedTest {

    @Test
    @DisplayName("work is spread: a step stops once its budget is spent, and the next one carries on")
    void spreadOverSteps() {
        AtomicLong clock = new AtomicLong();
        List<Integer> done = new ArrayList<>();
        Sliced<Integer> work = new Sliced<>(List.of(1, 2, 3, 4, 5, 6), item -> {
            done.add(item);
            clock.addAndGet(1_000_000);
        }, clock::get);

        assertThat(work.step(2_000_000)).isFalse();
        assertThat(done).containsExactly(1, 2);
        assertThat(work.step(2_000_000)).isFalse();
        assertThat(work.step(2_000_000)).isTrue();
        assertThat(done).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(work.left()).isZero();
    }

    @Test
    @DisplayName("a zero budget still does one item, so it can never stall")
    void alwaysAdvances() {
        List<Integer> done = new ArrayList<>();
        Sliced<Integer> work = new Sliced<>(List.of(1, 2), done::add, () -> 0);
        assertThat(work.step(0)).isFalse();
        assertThat(done).hasSize(1);
        assertThat(work.step(0)).isTrue();
    }

    @Test
    @DisplayName("nothing to do is done at once")
    void empty() {
        assertThat(new Sliced<Integer>(List.of(), item -> { }, () -> 0).step(1)).isTrue();
    }
}
