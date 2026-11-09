package de.raindancer.modules.performance.service;

import de.raindancer.modules.performance.model.Watcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AwayWatchersTest {

    private static final UUID WORLD = UUID.randomUUID();

    private final AwayWatchers watchers = new AwayWatchers();

    @Test
    @DisplayName("nobody away: the cheap answer for every event is no")
    void nobodyAway() {
        watchers.seen(UUID.randomUUID(), new Watcher(WORLD, 0, 0, 10, false));
        assertThat(watchers.anyAway()).isFalse();
        assertThat(watchers.all()).hasSize(1);
    }

    @Test
    @DisplayName("a player seen again replaces where they were; one who comes back ends the slowdown")
    void seenAgain() {
        UUID tom = UUID.randomUUID();
        watchers.seen(tom, new Watcher(WORLD, 0, 0, 10, true));
        assertThat(watchers.anyAway()).isTrue();
        watchers.seen(tom, new Watcher(WORLD, 5, 5, 10, false));
        assertThat(watchers.anyAway()).isFalse();
        assertThat(watchers.all()).containsExactly(new Watcher(WORLD, 5, 5, 10, false));
    }

    @Test
    @DisplayName("a player who left slows nothing down any more")
    void gone() {
        UUID tom = UUID.randomUUID();
        watchers.seen(tom, new Watcher(WORLD, 0, 0, 10, true));
        watchers.gone(tom);
        assertThat(watchers.anyAway()).isFalse();
        assertThat(watchers.all()).isEmpty();
    }
}
