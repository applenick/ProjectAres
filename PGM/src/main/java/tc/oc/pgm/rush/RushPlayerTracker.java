package tc.oc.pgm.rush;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import com.google.api.client.util.Sets;
import com.google.common.base.Objects;

import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.rush.states.RushCountdownState;
import tc.oc.pgm.snapshot.SnapshotMatchModule;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;

public class RushPlayerTracker implements Listener {

    private final RushMatchModule rushMatchModule;
    private final SnapshotMatchModule snapshotMatchModule;
    private final Set<Vector> blockUpdates = Sets.newHashSet();

    public RushPlayerTracker(RushMatchModule rushMatchModule) {
        this.rushMatchModule = rushMatchModule;
        this.snapshotMatchModule = rushMatchModule.getMatch().needMatchModule(SnapshotMatchModule.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCoarseMove(final CoarsePlayerMoveEvent event) {
        MatchPlayer player = rushMatchModule.getMatch().getPlayer(event.getPlayer());

        if (!canParticipate(player)) {
            return;
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
        event.setCancelled(handleBlockEvent(player, event.getBlock()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockBreakEvent event) {
        MatchPlayer player = rushMatchModule.getMatch().getPlayer(event.getPlayer());
        event.setCancelled(handleBlockEvent(player, event.getBlock()));
    }

    public void regenerateBlocks() {
        blockUpdates.forEach(vector -> {
            MaterialData original = snapshotMatchModule.getOriginalMaterial(vector);
            Block block = vector.toLocation(rushMatchModule.getMatch().getWorld()).getBlock();
            block.setTypeIdAndData(original.getItemTypeId(), original.getData(), false);
        });
        blockUpdates.clear();
    }

    private boolean handleBlockEvent(MatchPlayer player, Block block) {
        if (!canParticipate(player)) {
            return false;
        }

        if (rushMatchModule.getCurrentState() instanceof RushCountdownState) {
            return true;
        }

        blockUpdates.add(block.getLocation().toVector());
        return false;
    }

    private boolean canParticipate(MatchPlayer player) {
        return rushMatchModule.hasCurrentParticipator()
               && Objects.equal(player, rushMatchModule.getCurrentParticipator().getPlayer())
               && !player.getBukkit().isDead();
    }
}
