package de.raindancer.modules.performance.service;

import de.raindancer.modules.performance.model.Watcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Where every player was at the last look, and whether they were away — read by growth and spawn events on
 * any region thread, written once a second from each player's own thread. Readers get an immutable snapshot,
 * so an event costs one volatile read while nobody is away and a walk over the players otherwise.
 */
public final class AwayWatchers {

    private record Snapshot(List<Watcher> all, boolean anyAway) {
    }

    private final Map<UUID, Watcher> byPlayer = new HashMap<>();
    private volatile Snapshot snapshot = new Snapshot(List.of(), false);

    public synchronized void seen(UUID player, Watcher where) {
        if (where.equals(byPlayer.put(player, where))) {
            return;
        }
        publish();
    }

    public synchronized void gone(UUID player) {
        if (byPlayer.remove(player) != null) {
            publish();
        }
    }

    private void publish() {
        List<Watcher> all = List.copyOf(byPlayer.values());
        snapshot = new Snapshot(all, all.stream().anyMatch(Watcher::away));
    }

    public boolean anyAway() {
        return snapshot.anyAway();
    }

    public List<Watcher> all() {
        return snapshot.all();
    }
}
