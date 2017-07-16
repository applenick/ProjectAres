package tc.oc.pgm.observer;

import java.time.Duration;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import com.google.api.client.util.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import me.anxuiz.settings.SettingManager;
import me.anxuiz.settings.bukkit.PlayerSettings;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.pgm.PGM;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;
import tc.oc.time.Time;

@ListenerScope(MatchScope.LOADED)
public class ObserverPlayerFacet implements MatchPlayerFacet, Listener {

    private static final Duration RECOVERY = Duration.ofSeconds(30);

    private final Match match;
    private final MatchPlayer matchPlayer;
    private final Player player;
    private final SettingManager settings;

    private final Map<BlockVector, Long> brokenBlocks;

    @Inject ObserverPlayerFacet(Match match, MatchPlayer matchPlayer, Player player) {
        this.match = match;
        this.matchPlayer = matchPlayer;
        this.player = player;
        this.settings = PlayerSettings.getManager(player);
        this.brokenBlocks = Maps.newConcurrentMap();
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onObserverInteract(ObserverInteractEvent event) {
        // TODO: Implement ObserverBlockBreakEvent
        if (event.getClickType() != ClickType.LEFT) return;
        if (event.getClickedBlock() == null) return;
        
        if(!enabled(event.getPlayer())) return;

        Location blockLocation = event.getClickedBlock().getLocation();
        runSync(() -> player.sendBlockChange(blockLocation, 0, (byte) 0));
        brokenBlocks.put(blockLocation.toBlockVector(), System.currentTimeMillis());
    }
    
    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPartyChange(final PlayerChangePartyEvent event) {
        if (!Objects.equal(event.getPlayer(), player)) return;
        restoreAll();
    }

    @Repeatable(scope = MatchScope.LOADED, interval = @Time(seconds = 1))
    public void tick() {
        ImmutableMap.copyOf(brokenBlocks)
                    .entrySet()
                    .stream()
                    .filter(entry -> System.currentTimeMillis() - entry.getValue() >= RECOVERY.toMillis())
                    .forEach(entry -> {
                        restore(entry.getKey());
                    });
    }

    public void restore(BlockVector blockVector) {
        Location location = blockVector.toLocation(match.getWorld());
        MaterialData current = location.getBlock().getState().getData();
        runSync(() -> player.sendBlockChange(location, current.getItemType(), current.getData()));
        brokenBlocks.remove(blockVector);
    }

    public void restoreAll() {
        brokenBlocks.keySet().forEach(this::restore);
    }
    
    public boolean enabled(MatchPlayer matchPlayer) {
        return Objects.equal(matchPlayer, this.matchPlayer)
               && settings.getValue(ObserverSettings.BREAK, Boolean.class, false)
               && matchPlayer.isObserving();
    }

    /**
     * Block changes must be done after ObserverInteractEvent, and on the Bukkit
     * thread. Otherwise PGM's scheduler system would be a plausible option.
     */
    private void runSync(Runnable runnable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTask(PGM.get());
    }
}
