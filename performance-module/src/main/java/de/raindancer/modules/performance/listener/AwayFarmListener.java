package de.raindancer.modules.performance.listener;

import de.raindancer.core.platform.util.Scheduling;
import de.raindancer.core.social.presence.Away;
import de.raindancer.modules.performance.PerformanceSettings;
import de.raindancer.modules.performance.model.Watcher;
import de.raindancer.modules.performance.rules.AwayFarmRule;
import de.raindancer.modules.performance.service.AwayWatchers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Armadillo;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Slows what only runs because a player keeps the chunks around it ticking — crops, saplings, vines, mob
 * spawning, spawners, breeding, iron golems, raids, laid eggs, piglin bartering — while every player that close
 * is away.
 *
 * <p>Each thing is let through with the configured chance, so a crop's every stage takes about ten times
 * as long and a tenth of the mobs appear. Events arrive on whichever region ticks the farm and read only
 * {@link AwayWatchers}' snapshot: no world lookup, and nothing at all while nobody is away.
 */
public final class AwayFarmListener implements IPerformanceListener {

    private final Plugin plugin;
    private final Supplier<PerformanceSettings> settings;
    private final AwayFarmRule rule;
    private final AwayWatchers watchers;

    public AwayFarmListener(Plugin plugin, Supplier<PerformanceSettings> settings, AwayFarmRule rule,
                            AwayWatchers watchers) {
        this.plugin = plugin;
        this.settings = settings;
        this.rule = rule;
        this.watchers = watchers;
    }

    /** Looks where everybody is, each player on their own thread. Called once a second. */
    public void look(Iterable<? extends Player> online) {
        for (Player player : online) {
            Scheduling.onOwner(plugin, player, () -> {
                if (!player.isOnline()) {
                    return;
                }
                Location at = player.getLocation();
                watchers.seen(player.getUniqueId(), new Watcher(at.getWorld().getUID(), at.getBlockX() >> 4,
                        at.getBlockZ() >> 4, player.getSimulationDistance(), Away.isAway(player.getUniqueId())));
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        slow(event, event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getZ());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event) {
        Material source = event.getSource().getType();
        if (source == Material.FIRE || source == Material.SOUL_FIRE) {
            return;
        }
        slow(event, event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getZ());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTree(StructureGrowEvent event) {
        if (event.getPlayer() != null) {
            return;
        }
        Location at = event.getLocation();
        slow(event, at.getWorld(), at.getBlockX(), at.getBlockZ());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!rule.slows(event.getSpawnReason())) {
            return;
        }
        Location at = event.getLocation();
        slow(event, at.getWorld(), at.getBlockX(), at.getBlockZ());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLay(EntityDropItemEvent event) {
        if (!(event.getEntity() instanceof Chicken) && !(event.getEntity() instanceof Armadillo)) {
            return;
        }
        Location at = event.getEntity().getLocation();
        slow(event, at.getWorld(), at.getBlockX(), at.getBlockZ());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBarter(PiglinBarterEvent event) {
        Location at = event.getEntity().getLocation();
        if (slow(event, at.getWorld(), at.getBlockX(), at.getBlockZ())) {
            // The piglin let go of the gold before asking; cancelled alone, the barter would eat it. Dropped at its
            // feet, it is picked up and tried again — slower, not lost.
            at.getWorld().dropItem(at, event.getInput().clone());
        }
    }

    /** Whether the event was cancelled. */
    private boolean slow(Cancellable event, World world, int blockX, int blockZ) {
        if (!watchers.anyAway() || world == null) {
            return false;
        }
        if (rule.keeps(watchers.all(), world.getUID(), blockX >> 4, blockZ >> 4,
                ThreadLocalRandom.current().nextDouble(), settings.get())) {
            return false;
        }
        event.setCancelled(true);
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        forget(event.getPlayer().getUniqueId());
    }

    @Override
    public void forget(UUID player) {
        watchers.gone(player);
    }
}
