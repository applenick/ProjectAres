package tc.oc.pgm.rush.tracker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.google.common.base.Objects;

import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.rush.RushMatchModule;
import tc.oc.pgm.rush.states.RushCountdownState;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.utils.MatchPlayers;

public class RushPlayerTracker implements Listener {

    private final RushMatchModule rushMatchModule;

    public RushPlayerTracker(RushMatchModule rushMatchModule) {
        this.rushMatchModule = rushMatchModule;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final CoarsePlayerMoveEvent event) {
        MatchPlayer player = rushMatchModule.getMatch().getPlayer(event.getPlayer());

        if (!rushMatchModule.hasCurrentParticipator()
            || !Objects.equal(player, rushMatchModule.getCurrentParticipator().getPlayer())
            || !MatchPlayers.canInteract(player)
            || player.getBukkit().isDead()) {
            return;
        }

        if (rushMatchModule.getCurrentState() instanceof RushCountdownState) {
            event.setTo(event.getFrom());
        }

        rushMatchModule.getCurrentParticipator().setLastTo(event.getTo().toVector());
        rushMatchModule.updateState();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDespawn(final ParticipantDespawnEvent event) {
        if (rushMatchModule.hasCurrentParticipator()
            && Objects.equal(event.getPlayer(), rushMatchModule.getCurrentParticipator().getPlayer())) {
            rushMatchModule.transitionToBlank();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event) {
        MatchPlayer player = rushMatchModule.getMatch().getPlayer(event.getPlayer());

        if (!rushMatchModule.hasCurrentParticipator()
            || !Objects.equal(player, rushMatchModule.getCurrentParticipator().getPlayer())
            || !MatchPlayers.canInteract(player)
            || player.getBukkit().isDead()) {
            return;
        }

        if (rushMatchModule.getCurrentState() instanceof RushCountdownState) {
            event.setCancelled(true);
            return;
        }

        rushMatchModule.getBlockTracker().onBlockPlace(event.getBlockReplacedState());
    }
}
