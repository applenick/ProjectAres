package tc.oc.pgm.proximity;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import com.google.common.collect.Sets;

import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.fireworks.FireworkUtil;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.spawns.events.ParticipantSpawnEvent;

@ListenerScope(MatchScope.LOADED)
public class ProximityAlarm implements Listener {
    private static final long MESSAGE_INTERVAL = 5000;
    private static final float FLARE_CHANCE = 0.25f;

    private static final BukkitSound SOUND = new BukkitSound(Sound.ENTITY_FIREWORK_LARGE_BLAST_FAR, 1f, 0.7f);

    protected final Random random;
    protected final Match match;
    protected final ProximityAlarmDefinition definition;
    protected final Set<MatchPlayer> playersInside = Sets.newHashSet();
    protected long lastMessageTime = 0;

    public ProximityAlarm(Match match, ProximityAlarmDefinition definition, Random random) {
        this.random = random;
        this.match = match;
        this.definition = definition;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final CoarsePlayerMoveEvent event) {
        updatePlayer(this.match.getPlayer(event.getPlayer()), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerSpawn(final ParticipantSpawnEvent event) {
        updatePlayer(event.getPlayer(), event.getPlayer().getBukkit().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDespawn(final ParticipantDespawnEvent event) {
        this.playersInside.remove(event.getPlayer());
    }

    private void updatePlayer(MatchPlayer player, Location location) {
        if(player != null && player.canInteract() && this.definition.detectFilter.query(player).isAllowed()) {
            if(!player.isDead() && this.definition.detectRegion.contains(location.toVector())) {
                this.playersInside.add(player);
            } else {
                this.playersInside.remove(player);
            }
        }
    }

    public void showAlarm() {
        if(this.random.nextFloat() < FLARE_CHANCE) {
            if(!this.playersInside.isEmpty()) {
                this.showFlare();
                this.showMessage();
            }
        }
    }

    private void showFlare() {
        float angle = (float) (this.random.nextFloat() * Math.PI * 2);
        Location location = this.definition.detectRegion.getBounds().center()
                                .plus(
                                    new Vector(
                                        Math.sin(angle) * this.definition.flareRadius,
                                        0,
                                        Math.cos(angle) * this.definition.flareRadius
                                    )
                                ).toLocation(this.match.getWorld());

        Set<Color> colors = this.playersInside.stream()
                                              .map(MatchPlayer::getParty)
                                              .map(Party::getFullColor)
                                              .collect(Collectors.toSet());

        Firework firework = FireworkUtil.spawnFirework(location,
                                                       FireworkEffect.builder()
                                                           .with(FireworkEffect.Type.BALL)
                                                           .withColor(colors)
                                                           .build(),
                                                       0);
        NMSHacks.skipFireworksLaunch(firework);
    }

    private void showMessage() {
        if(this.definition.alertMessage == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if(this.lastMessageTime + MESSAGE_INTERVAL < now) {
            this.lastMessageTime = now;

            this.match.players()
                      .filter(player -> this.definition.alertFilter.query(player).isAllowed())
                      .forEach(this::showMessage);
        }
    }
    
    private void showMessage(MatchPlayer player) {
        player.sendMessage(ChatColor.RED + this.definition.alertMessage);
        player.playSound(SOUND);
    }
}
