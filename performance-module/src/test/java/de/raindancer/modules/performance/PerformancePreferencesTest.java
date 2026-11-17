package de.raindancer.modules.performance;

import de.raindancer.core.ui.messages.Messages;
import de.raindancer.core.ui.preference.Preference;
import de.raindancer.modules.performance.util.PermissionNodes;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PerformancePreferencesTest {

    private final Messages messages = mock(Messages.class);
    private final Player player = mock(Player.class);
    private final PerformanceSettings settings = mock(PerformanceSettings.class);
    private final Preference preference = PerformancePreferences.alerts(messages, () -> settings);

    @Test
    void isTheAlertsSwitchWithOnAndOff() {
        assertThat(preference.id()).isEqualTo("performance:alerts");
        assertThat(preference.options()).containsExactly("On", "Off");
        assertThat(preference.isPage()).isFalse();
    }

    @Test
    void choosingSetsTheSwitchAndSaysSo() {
        when(player.getPersistentDataContainer()).thenReturn(mock(PersistentDataContainer.class));
        preference.choose().accept(player, 1);
        verify(messages).send(player, "performance.alerts-off");
        preference.choose().accept(player, 0);
        verify(messages).send(player, "performance.alerts-on");
    }

    @Test
    void offeredOnlyToWhoIsToldAndWhileTheWatchIsOn() {
        when(settings.watch()).thenReturn(true);
        when(player.hasPermission(PermissionNodes.NOTIFY)).thenReturn(true);
        assertThat(preference.shown().test(player)).isTrue();
        when(player.hasPermission(PermissionNodes.NOTIFY)).thenReturn(false);
        assertThat(preference.shown().test(player)).isFalse();
        when(player.hasPermission(PermissionNodes.NOTIFY)).thenReturn(true);
        when(settings.watch()).thenReturn(false);
        assertThat(preference.shown().test(player)).isFalse();
    }
}
