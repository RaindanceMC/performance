package de.raindancer.modules.performance.service;

import de.raindancer.modules.performance.model.ChunkCensus;
import de.raindancer.modules.performance.model.EntityGroup;
import de.raindancer.modules.performance.model.Sliced;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Counts what every loaded chunk holds, a few milliseconds per tick ({@link #begin}). Reads every world, so only on
 * a server with one main thread — Paper, called from the global region (which is that thread). No decisions here; FindingRule makes them.
 */
public final class Census {

    /** A chunk with fewer block entities than this is not worth listing them for. */
    private static final int FEWEST_BLOCK_ENTITIES = 10;

    /** All at once — for tests and tools that run off the tick; the lag watch uses {@link #begin}. */
    public List<ChunkCensus> take(Iterable<World> worlds) {
        Job job = begin(worlds);
        while (!job.step(Long.MAX_VALUE / 2)) {
            // step again until nothing is left
        }
        return job.result();
    }

    /** A count that is done a few milliseconds per tick: call {@link Job#step} each tick until it says it is done. */
    public Job begin(Iterable<World> worlds) {
        List<Chunk> chunks = new ArrayList<>();
        for (World world : worlds) {
            chunks.addAll(java.util.Arrays.asList(world.getLoadedChunks()));
        }
        return new Job(chunks);
    }

    public static final class Job {
        private final Map<String, ChunkCensus> found = new HashMap<>();
        private final Sliced<Chunk> work;

        private Job(List<Chunk> chunks) {
            this.work = new Sliced<>(chunks, this::count, System::nanoTime);
        }

        /** @return true when every chunk has been counted */
        public boolean step(long budgetNanos) {
            return work.step(budgetNanos);
        }

        public List<ChunkCensus> result() {
            return new ArrayList<>(found.values());
        }

        private void count(Chunk chunk) {
            if (!chunk.isLoaded()) {
                return;
            }
            String key = chunk.getWorld().getKey().asString();
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof Player) {
                    continue;
                }
                Location at = entity.getLocation();
                at(key, chunk).seen(typeOf(entity), groupOf(entity), at.getX(), at.getY(), at.getZ());
            }
            BlockState[] states = chunk.getTileEntities(false);
            if (states.length >= FEWEST_BLOCK_ENTITIES) {
                ChunkCensus census = at(key, chunk);
                for (BlockState state : states) {
                    census.blockEntities(state.getType().getKey().getKey(), 1);
                }
            }
        }

        private ChunkCensus at(String world, Chunk chunk) {
            return found.computeIfAbsent(world + "/" + chunk.getX() + "/" + chunk.getZ(),
                    ignored -> new ChunkCensus(world, chunk.getX(), chunk.getZ()));
        }
    }

    static String typeOf(Entity entity) {
        return entity.getType().getKey().getKey().toLowerCase(Locale.ROOT);
    }

    static EntityGroup groupOf(Entity entity) {
        if (entity instanceof Item || entity instanceof ExperienceOrb) {
            return EntityGroup.ITEM;
        }
        if (entity instanceof AbstractVillager) {
            return EntityGroup.VILLAGER;
        }
        if (entity instanceof Breedable) {
            return EntityGroup.ANIMAL;
        }
        if (entity instanceof Enemy) {
            return EntityGroup.MONSTER;
        }
        if (entity instanceof Vehicle) {
            return EntityGroup.VEHICLE;
        }
        return EntityGroup.OTHER;
    }
}
