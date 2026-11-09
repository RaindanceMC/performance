package de.raindancer.modules.performance.rules;

import de.raindancer.modules.performance.PerformanceSettings;
import de.raindancer.modules.performance.model.Watcher;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AwayFarmRuleTest {

    private static final UUID WORLD = UUID.randomUUID();
    private static final UUID NETHER = UUID.randomUUID();
    private static final PerformanceSettings TENTH = PerformanceSettings.away(true, 0.1);

    private final AwayFarmRule rule = new AwayFarmRule();

    private static Watcher here(boolean away) {
        return new Watcher(WORLD, 0, 0, 10, away);
    }

    @Test
    @DisplayName("with everybody near away, about one in ten goes through")
    void everybodyAwayKeepsATenth() {
        List<Watcher> near = List.of(here(true), new Watcher(WORLD, 3, -4, 10, true));
        assertThat(rule.keeps(near, WORLD, 1, 1, 0.05, TENTH)).isTrue();
        assertThat(rule.keeps(near, WORLD, 1, 1, 0.0999, TENTH)).isTrue();
        assertThat(rule.keeps(near, WORLD, 1, 1, 0.1, TENTH)).isFalse();
        assertThat(rule.keeps(near, WORLD, 1, 1, 0.95, TENTH)).isFalse();
    }

    @Test
    @DisplayName("one player near who is here keeps the farm at full speed")
    void oneActiveIsEnough() {
        List<Watcher> near = List.of(here(true), here(false), here(true));
        assertThat(rule.keeps(near, WORLD, 0, 0, 0.99, TENTH)).isTrue();
    }

    @Test
    @DisplayName("nobody near: not a farm of an away player, left alone")
    void nobodyNear() {
        assertThat(rule.keeps(List.of(), WORLD, 0, 0, 0.99, TENTH)).isTrue();
    }

    @Test
    @DisplayName("an active player only counts within their simulation distance and in the same world")
    void distanceAndWorld() {
        Watcher farmer = new Watcher(WORLD, 0, 0, 10, true);
        Watcher farAway = new Watcher(WORLD, 11, 0, 10, false);
        Watcher diagonalEdge = new Watcher(WORLD, -10, 10, 10, false);
        Watcher otherWorld = new Watcher(NETHER, 0, 0, 10, false);
        assertThat(rule.keeps(List.of(farmer, farAway, otherWorld), WORLD, 0, 0, 0.99, TENTH)).isFalse();
        assertThat(rule.keeps(List.of(farmer, diagonalEdge), WORLD, 0, 0, 0.99, TENTH)).isTrue();
    }

    @Test
    @DisplayName("an away player beyond simulation distance does not slow anything down")
    void awayButFar() {
        Watcher far = new Watcher(WORLD, 40, 40, 10, true);
        assertThat(rule.keeps(List.of(far), WORLD, 0, 0, 0.99, TENTH)).isTrue();
    }

    @Test
    @DisplayName("each player's own simulation distance decides how far they reach")
    void ownSimulationDistance() {
        Watcher shortSighted = new Watcher(WORLD, 5, 0, 4, true);
        assertThat(rule.keeps(List.of(shortSighted), WORLD, 0, 0, 0.99, TENTH)).isTrue();
        assertThat(rule.keeps(List.of(shortSighted), WORLD, 1, 0, 0.99, TENTH)).isFalse();
    }

    @Test
    @DisplayName("rate 0 stops an away farm entirely, rate 1 leaves it alone, switched off nothing changes")
    void bounds() {
        List<Watcher> near = List.of(here(true));
        assertThat(rule.keeps(near, WORLD, 0, 0, 0.0, PerformanceSettings.away(true, 0.0))).isFalse();
        assertThat(rule.keeps(near, WORLD, 0, 0, 0.9999, PerformanceSettings.away(true, 1.0))).isTrue();
        assertThat(rule.keeps(near, WORLD, 0, 0, 0.99, PerformanceSettings.away(false, 0.1))).isTrue();
    }

    @Test
    @DisplayName("the default slows an away player's farms to a tenth")
    void defaults() {
        assertThat(PerformanceSettings.DEFAULTS.awayFarms()).isTrue();
        assertThat(PerformanceSettings.DEFAULTS.awayFarmRate()).isEqualTo(0.1);
    }

    @ParameterizedTest
    @EnumSource(value = SpawnReason.class, names = {"NATURAL", "SPAWNER", "TRIAL_SPAWNER", "BREEDING",
            "VILLAGE_DEFENSE", "RAID", "PATROL", "NETHER_PORTAL"})
    @DisplayName("what farms are made of is slowed")
    void farmReasons(SpawnReason reason) {
        assertThat(rule.slows(reason)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = SpawnReason.class, names = {"SPAWNER_EGG", "COMMAND", "CUSTOM", "CHUNK_GEN",
            "BUILD_IRONGOLEM", "BUILD_SNOWMAN", "BUILD_WITHER", "DEFAULT", "CURED", "INFECTION", "SHOULDER_ENTITY", "EGG", "DISPENSE_EGG", "SLIME_SPLIT"})
    @DisplayName("somebody meaning it, world generation, conversions and a farm's second stage are not slowed")
    void otherReasons(SpawnReason reason) {
        assertThat(rule.slows(reason)).isFalse();
    }
}
