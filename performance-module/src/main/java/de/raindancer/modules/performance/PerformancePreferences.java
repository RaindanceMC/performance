package de.raindancer.modules.performance;

import de.raindancer.core.ui.messages.Messages;
import de.raindancer.core.ui.preference.Preference;
import de.raindancer.modules.performance.service.Notifier;
import de.raindancer.modules.performance.util.PermissionNodes;

import java.util.function.Supplier;

/** What /preferences offers for lag reports: the same switch /perf alerts flips. */
public final class PerformancePreferences {

    private PerformancePreferences() {
    }

    /** Offered to whoever the notifier tells (the notify permission), and not while the lag watch is off. */
    public static Preference alerts(Messages messages, Supplier<PerformanceSettings> settings) {
        return Preference.toggle("performance:alerts", "Lag reports", "Lag reports in chat",
                        "Tells you in chat when the server lags, with buttons to look into it.",
                        Notifier.ALERTS::isOn,
                        (player, on) -> {
                            Notifier.ALERTS.set(player, on);
                            messages.send(player, on ? "performance.alerts-on" : "performance.alerts-off");
                        })
                .shownTo(player -> settings.get().watch() && player.hasPermission(PermissionNodes.NOTIFY));
    }
}
