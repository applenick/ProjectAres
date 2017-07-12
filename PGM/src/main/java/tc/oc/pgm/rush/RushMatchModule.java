package tc.oc.pgm.rush;

import static tc.oc.pgm.rush.RushState.COUNTDOWN;
import static tc.oc.pgm.rush.RushState.PLAYER_RUNNING;
import static tc.oc.pgm.rush.RushState.WAITING_FOR_PLAYER;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.pgm.bossbar.BossBarMatchModule;
import tc.oc.pgm.bossbar.BossBarSource;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchStateChangeEvent;
import tc.oc.pgm.join.JoinDenied;
import tc.oc.pgm.join.JoinHandler;
import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.join.JoinMethod;
import tc.oc.pgm.join.JoinRequest;
import tc.oc.pgm.join.JoinResult;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.score.ScoreMatchModule;
import tc.oc.pgm.utils.MatchPlayers;
import tc.oc.pgm.utils.WrappedTimer;

@ListenerScope(MatchScope.RUNNING)
public class RushMatchModule extends MatchModule implements Listener, JoinHandler {

    private static final int COUNTDOWN_SECONDS = 5;
    private static final WrappedTimer timer = new WrappedTimer();

    private final RushConfig config;
    
    private final ScoreMatchModule scoreModule;
    private final BossBarMatchModule bossBarModule;

    private MatchPlayer currentPlayer;
    private RushState rushState;
    
    private Task countdownTask;
    private Task timelimitTask;
    private int countdown;
    private int timeLeft;
    
    private BossBarSource spectatorBossBar;

    public RushMatchModule(Match match, RushConfig config) {
        super(match);
        this.config = config;
        this.scoreModule = match.needMatchModule(ScoreMatchModule.class);
        this.bossBarModule = match.needMatchModule(BossBarMatchModule.class);
        this.rushState = COUNTDOWN;
        this.countdown = COUNTDOWN_SECONDS;
        this.timeLeft = config.getTimeLimit();
        match.registerEvents(this);
    }
    
    @Override
    public void load() {
        super.load();
        match.needMatchModule(JoinMatchModule.class).registerHandler(this);
    }

    @Override
    public JoinResult queryJoin(MatchPlayer joining, JoinRequest request) {
        if (match.hasStarted() && request.method() != JoinMethod.FORCE) {
            return JoinDenied.friendly("command.gameplay.join.matchStarted");
        }

        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMatchStateChange(final MatchStateChangeEvent event) {
        if(event.getNewState() == MatchState.Running) {
            this.currentPlayer = newPlayer().orElse(null);
            prepare(this.currentPlayer, config.getStartLine(), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final CoarsePlayerMoveEvent event) {
        this.handlePlayerMove(event.getPlayer(), event.getTo().toVector(), event);
    }

    private void handlePlayerMove(Player bukkit, Vector to, CoarsePlayerMoveEvent event) {
        MatchPlayer player = this.match.getPlayer(bukkit);
        Competitor competitor = player.getCompetitor();

        if (player != currentPlayer || !MatchPlayers.canInteract(player)
            || player.getBukkit().isDead()) {
            return;
        }

        if (checkState(RushState.COUNTDOWN)) {
            if (event != null) {
                event.setCancelled(true);
            }
            
            return;
        }
        
        if (timer.start(checkState(WAITING_FOR_PLAYER))) {
            if (config.getStartLine().contains(to.toBlockVector())) {
                rushState = PLAYER_RUNNING;
            }
            
            return;
        } else if (checkState(PLAYER_RUNNING)) {
            if (config.getFinishLine().contains(to.toBlockVector())) {
                reset(player, scoreModule.getScore(competitor));
                timer.reset();
            }
            
            return;
        }

        if(currentPlayer == null) {
            end();
            return;
        }

        prepare(currentPlayer, config.getStartLine(), false);
    }

    private void updateCountdown() {
        if (--countdown <= 0) {
            countdown = COUNTDOWN_SECONDS;
            timeLeft = config.getTimeLimit();
            rushState = WAITING_FOR_PLAYER;
            countdownTask.cancel();
            currentPlayer.showTitle(new TextComponent("GO!"), null, 5, 10, 5);
        } else {
            currentPlayer.showTitle(new TextComponent(Integer.toString(countdown)), null, 5, 10, 5);
        }
    }
    
    private void reset(MatchPlayer player, double score) {
        timelimitTask.cancel();
        scoreModule.setScore(player.getCompetitor(), score);
        resetPlayer();
    }

    private void resetPlayer() {
        currentPlayer.setVisible(false);
        currentPlayer = newPlayer().orElse(null);
        prepare(currentPlayer, config.getStartLine(), false);
    }

    private void prepare(MatchPlayer player, Region region, boolean force) {
        if(currentPlayer == null) {
            end();
            return;
        }
        
        if(state() == COUNTDOWN && !force) return;
        
        player.setVisible(true);
        player.getBukkit().setGameMode(GameMode.ADVENTURE);
        player.getBukkit().teleport(config.getSpawnLine().getBounds().center().toLocation(player.getWorld()));

        rushState = COUNTDOWN;
        countdownTask = match.getScheduler(MatchScope.RUNNING).createRepeatingTask(Duration.ofSeconds(1), () -> updateCountdown());
        timelimitTask = match.getScheduler(MatchScope.RUNNING).createRepeatingTask(Duration.ofSeconds(1), () -> {
            if(--timeLeft <= 0) {
                reset(player, 1);
            } else {
                long score = (TimeUnit.SECONDS.toMillis(config.getTimeLimit()) - timer.elapsed(TimeUnit.MILLISECONDS));
                scoreModule.setScore(player.getCompetitor(), score);
            }
        });
        
        removeBossBar();
        spectatorBossBar = new BossBarSource() {
            @Override
            public BaseComponent barText(Player viewer) {
                return new TextComponent("You are currently waiting for " + player.getDisplayName() + " to finish");
            }
            
            @Override
            public float barProgress(Player viewer) {
                return Math.max(0f, Math.min(1f, 1f - ((timer.elapsed(TimeUnit.MILLISECONDS) * 100 / config.getTimeLimit() * 1000) / 100)));
            }
        };
        bossBarModule.add(spectatorBossBar, match.players().filter(other -> other != player).map(MatchPlayer::getBukkit));
        match.players().filter(other -> other != player).forEach(other -> {
            other.setVisible(false);
            other.getBukkit().setGameMode(GameMode.SPECTATOR);
        });
    }
    
    private void end() {
        removeBossBar();
        match.end();
    }
    
    private void removeBossBar() {
        if(spectatorBossBar != null) {
            bossBarModule.invalidate(spectatorBossBar);
            bossBarModule.remove(spectatorBossBar);
        }
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