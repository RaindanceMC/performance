package de.raindancer.modules.performance.command;

import de.raindancer.core.ui.messages.Messages;
import de.raindancer.modules.performance.PerformanceServices;
import de.raindancer.modules.performance.util.PermissionNodes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Looking at reports is one right; acting on them — fixing, teleporting there — are others. */
class PerfCommandTest {

    @Test
    @DisplayName("somebody who may only look is not teleported to a report's place")
    void teleportNeedsItsOwnRight() {
        Messages messages = mock(Messages.class);
        PerformanceServices services = mock(PerformanceServices.class);
        when(services.messages()).thenReturn(messages);
        Player looker = mock(Player.class);
        when(looker.hasPermission(PermissionNodes.INSPECT)).thenReturn(true);
        CommandSourceStack source = mock(CommandSourceStack.class);
        when(source.getSender()).thenReturn(looker);

        new PerfCommand(() -> services).execute(source, new String[]{"tp", "1", "1"});

        verify(messages).send(looker, "performance.not-allowed");
        verify(looker, never()).teleportAsync(any());
    }

    @Test
    @DisplayName("fixing and undoing need the fix right")
    void fixNeedsItsRight() {
        Messages messages = mock(Messages.class);
        PerformanceServices services = mock(PerformanceServices.class);
        when(services.messages()).thenReturn(messages);
        Player looker = mock(Player.class);
        when(looker.hasPermission(PermissionNodes.INSPECT)).thenReturn(true);
        CommandSourceStack source = mock(CommandSourceStack.class);
        when(source.getSender()).thenReturn(looker);

        new PerfCommand(() -> services).execute(source, new String[]{"fix", "1", "1"});
        new PerfCommand(() -> services).execute(source, new String[]{"undo"});

        verify(messages, org.mockito.Mockito.times(2)).send(looker, "performance.not-allowed");
    }

    @Test
    @DisplayName("show, fix and tp without their number say how to use them instead of 'no such report'")
    void missingArgumentsShowUsage() {
        Messages messages = mock(Messages.class);
        PerformanceServices services = mock(PerformanceServices.class);
        when(services.messages()).thenReturn(messages);
        Player staff = mock(Player.class);
        when(staff.hasPermission(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        CommandSourceStack source = mock(CommandSourceStack.class);
        when(source.getSender()).thenReturn(staff);

        PerfCommand command = new PerfCommand(() -> services);
        command.execute(source, new String[]{"show"});
        command.execute(source, new String[]{"fix"});
        command.execute(source, new String[]{"tp"});

        verify(messages).send(staff, "performance.usage-show");
        verify(messages).send(staff, "performance.usage-fix");
        verify(messages).send(staff, "performance.usage-tp");
    }

    @Test
    @DisplayName("the third word of fix and tp suggests the report's finding numbers")
    void suggestsFindings() {
        PerformanceServices services = mock(PerformanceServices.class);
        de.raindancer.modules.performance.store.ReportStore store = mock(de.raindancer.modules.performance.store.ReportStore.class);
        de.raindancer.modules.performance.model.Report report = mock(de.raindancer.modules.performance.model.Report.class);
        when(report.number()).thenReturn(4);
        when(report.findings()).thenReturn(java.util.Collections.nCopies(3, null));
        when(store.get(4)).thenReturn(java.util.Optional.of(report));
        when(services.reports()).thenReturn(store);
        CommandSourceStack source = mock(CommandSourceStack.class);

        PerfCommand command = new PerfCommand(() -> services);
        org.assertj.core.api.Assertions.assertThat(command.suggest(source, new String[]{"tp", "4", ""}))
                .containsExactly("1", "2", "3");
        org.assertj.core.api.Assertions.assertThat(command.suggest(source, new String[]{"fix", "4", ""}))
                .containsExactly("1", "2", "3", "distance");
    }
}
