package de.raindancer.modules.performance.model;

import java.util.UUID;

/** Where a player keeps the world ticking, and whether they are at the keyboard while it does. */
public record Watcher(UUID world, int chunkX, int chunkZ, int simulationDistance, boolean away) {

    /** Whether the chunk at {@code x}, {@code z} is ticked because of this player — the game measures in a square. */
    public boolean ticks(UUID inWorld, int x, int z) {
        return world.equals(inWorld)
                && Math.max(Math.abs(chunkX - x), Math.abs(chunkZ - z)) <= simulationDistance;
    }
}
