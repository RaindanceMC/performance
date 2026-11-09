package de.raindancer.modules.performance.rules;

import de.raindancer.modules.performance.PerformanceSettings;
import de.raindancer.modules.performance.model.Watcher;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Whether a farm near only away players still makes this one thing — a crop's next stage, a mob, a laid egg.
 * One player at the keyboard within reach keeps it at full speed.
 *
 * <p>Only a farm's first stage is slowed: thrown and dispensed eggs come from laid eggs, and a slime splits
 * because it was killed, so slowing those too would make a tenth of a tenth.
 */
public final class AwayFarmRule implements IPerformanceRule {

    private static final Set<SpawnReason> FARMED = EnumSet.of(SpawnReason.NATURAL, SpawnReason.SPAWNER,
            SpawnReason.TRIAL_SPAWNER, SpawnReason.BREEDING, SpawnReason.VILLAGE_DEFENSE, SpawnReason.RAID,
            SpawnReason.PATROL, SpawnReason.NETHER_PORTAL);

    public boolean slows(SpawnReason reason) {
        return FARMED.contains(reason);
    }

    /** {@code roll} is uniform in [0, 1); below the rate it goes through. */
    public boolean keeps(Iterable<Watcher> players, UUID world, int chunkX, int chunkZ, double roll,
                         PerformanceSettings settings) {
        if (!settings.awayFarms()) {
            return true;
        }
        boolean awayNear = false;
        for (Watcher player : players) {
            if (player.ticks(world, chunkX, chunkZ)) {
                if (!player.away()) {
                    return true;
                }
                awayNear = true;
            }
        }
        return !awayNear || roll < settings.awayFarmRate();
    }

    @Override
    public String describe() {
        return "farms near only away players run at a fraction of their speed";
    }
}
