package tc.oc.pgm.rush;

import static tc.oc.pgm.rush.RushState.COUNTDOWN;
import static tc.oc.pgm.rush.RushState.PLAYER_RUNNING;
import static tc.oc.pgm.rush.RushState.WAITING_FOR_PLAYER;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.chat.TextComponent;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.score.ScoreMatchModule;
import tc.oc.pgm.utils.MatchPlayers;
import tc.oc.pgm.utils.WrappedTimer;

@ListenerScope(MatchScope.RUNNING)
public class RushMatchModule extends MatchModule implements Listener {

    private static final WrappedTimer timer = new WrappedTimer();

    private final RushConfig config;
    
    private final ScoreMatchModule scoreModule;

    private MatchPlayer currentPlayer;
    private RushState rushState;
    
    private Task countdownTask;
    private Task timelimitTask;
    private int countdown;

    public RushMatchModule(Match match, RushConfig config) {
        super(match);
        this.config = config;
        this.scoreModule = match.needMatchModule(ScoreMatchModule.class);
        this.currentPlayer = newPlayer().orElseThrow(() -> new IllegalStateException("no players"));
        teleportCenter(this.currentPlayer, config.getStartLine());
        this.rushState = WAITING_FOR_PLAYER;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final CoarsePlayerMoveEvent event) {
        this.handlePlayerMove(event.getPlayer(), event.getTo().toVector(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        this.handlePlayerMove(event.getPlayer(), event.getTo().toVector(), null);
    }

    private void handlePlayerMove(Player bukkit, Vector to, CoarsePlayerMoveEvent event) {
        MatchPlayer player = this.match.getPlayer(bukkit);
        
        if (player != currentPlayer || !MatchPlayers.canInteract(player)
            || player.getBukkit().isDead()) {
            return;
        }
        
        if(checkState(RushState.COUNTDOWN) && event != null) {
            event.setCancelled(true);
            return;
        }
        
        if(timer.start(checkState(WAITING_FOR_PLAYER) && config.getStartLine().contains(to.toBlockVector()))) {
            rushState = PLAYER_RUNNING;
            return;
        } else if (checkState(PLAYER_RUNNING)) {
            if (config.getFinishLine().contains(to.toBlockVector())) {
                incrementAndReset(player, TimeUnit.SECONDS.toMillis(config.getTimeLimit()) - timer.reset(TimeUnit.MILLISECONDS));
            } else {
                return;
            }
        }

        if(currentPlayer == null) {
            match.end();
            return;
        }
        
        rushState = COUNTDOWN;
        currentPlayer.setVisible(true);
        teleportCenter(currentPlayer, config.getStartLine());
        
        countdownTask = match.getScheduler(MatchScope.RUNNING).createRepeatingTask(Duration.ofSeconds(5), () -> updateCountdown());
        timelimitTask = match.getScheduler(MatchScope.RUNNING).createDelayedTask(Duration.ofSeconds(config.getTimeLimit()), () -> incrementAndReset(player, 1));
    }
    
    private void updateCountdown() {
        if (--countdown <= 0) {
            countdown = 5;
            rushState = WAITING_FOR_PLAYER;
            countdownTask.cancel();
        } else {
            currentPlayer.showTitle(new TextComponent(Integer.toString(countdown)), null, 5, 10, 5);
        }
    }
    
    private void incrementAndReset(MatchPlayer player, long score) {
        timelimitTask.cancel();
        scoreModule.incrementScore(player.getCompetitor(), score);
        resetPlayer();
    }

    private void resetPlayer() {
        currentPlayer.setVisible(false);
        teleportCenter(currentPlayer, config.getStartLine());
        currentPlayer = newPlayer().orElse(null);
    }

    private void teleportCenter(MatchPlayer player, Region region) {
        player.getBukkit().teleport(region.getBounds().center().toLocation(player.getWorld()));
    }

    private Optional<MatchPlayer> newPlayer() {
        return match.players()
                    .filter(player -> scoreModule.getScore(player.getCompetitor()) == 0d)
                    .findAny();
    }

    public RushState state() {
        return rushState;
    }

    public boolean checkState(RushState rushState) {
        return state() == rushState;
    }
}
