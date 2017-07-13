package tc.oc.pgm.rush;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;

import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.pgm.bossbar.BossBarMatchModule;
import tc.oc.pgm.bossbar.BossBarSource;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScheduler;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;
import tc.oc.pgm.rush.states.RushBlankState;
import tc.oc.pgm.rush.states.RushCountdownState;
import tc.oc.pgm.rush.states.RushFinishLineState;
import tc.oc.pgm.rush.states.RushGameEndState;
import tc.oc.pgm.rush.states.RushStartLineState;
import tc.oc.pgm.rush.states.RushWaitState;
import tc.oc.pgm.score.ScoreMatchModule;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.utils.MatchPlayers;
import tc.oc.time.Time;

@ListenerScope(MatchScope.RUNNING)
public class RushMatchModule extends MatchModule implements Listener {

    private final RushConfig config;

    private final ScoreMatchModule scoreModule;
    private final BossBarMatchModule bossBarModule;

    private final RushTransitionState[] transitionStates;
    private final Stopwatch timer;

    private RushTransitionState currentState;
    private RushParticipator currentParticipator;
    private BossBarSource rushBossbar;
    private Task timelimitTask;

    public RushMatchModule(Match match, RushConfig config) {
        super(match);
        this.config = config;
        this.scoreModule = match.needMatchModule(ScoreMatchModule.class);
        this.bossBarModule = match.needMatchModule(BossBarMatchModule.class);
        this.transitionStates = new RushTransitionState[] { createState(RushBlankState.class),
                                                            createState(RushCountdownState.class),
                                                            createState(RushFinishLineState.class),
                                                            createState(RushGameEndState.class),
                                                            createState(RushStartLineState.class),
                                                            createState(RushWaitState.class) };
        this.timer = Stopwatch.createUnstarted();
        this.currentState = transitionStates[0];
        match.registerEvents(this);
    }

    @Repeatable(interval = @Time(milliseconds = 100), scope = MatchScope.RUNNING)
    private void tick() {
        if (rushBossbar != null) {
            bossBarModule.render(rushBossbar);
        }

        updateScore();
        updateState();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final CoarsePlayerMoveEvent event) {
        MatchPlayer player = this.match.getPlayer(event.getPlayer());

        if (!hasCurrentParticipator() || !Objects.equal(player, getCurrentParticipator().getPlayer())
            || !MatchPlayers.canInteract(player)
            || player.getBukkit().isDead()) {
            return;
        }

        if (getCurrentState() instanceof RushCountdownState) {
            event.setTo(event.getFrom());
        }

        currentParticipator.setLastTo(event.getTo().toVector());
        updateState();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDespawn(final ParticipantDespawnEvent event) {
        if (hasCurrentParticipator() && Objects.equal(event.getPlayer(), getCurrentParticipator().getPlayer())) {
            // TODO: Reset player back to default observer state
            event.getPlayer().getBukkit().setGameMode(GameMode.CREATIVE);

            transitionToBlank();
        }
    }

    private <T extends RushTransitionState> T createState(Class<T> state) {
        try {
            return state.getDeclaredConstructor(getClass()).newInstance(this);
        } catch (Exception any) {
            // TODO: Error handling
            return null;
        }
    }

    private void updateState() {
        Stream.of(transitionStates)
              .filter(RushTransitionState::canTransition)
              .findFirst()
              .map(RushTransitionState::getClass)
              .ifPresent(this::transitionTo);
    }

    private Optional<RushParticipator> newParticipator() {
        return match.players()
                    .filter(player -> player.isParticipating() && scoreModule.getScore(player.getCompetitor()) == 0d)
                    .map(RushParticipator::new)
                    .findAny();
    }

    public void setBossbar(BossBarSource bossBarSource, Stream<MatchPlayer> players) {
        removeBossbar();
        rushBossbar = bossBarSource;
        bossBarModule.add(bossBarSource, players.map(MatchPlayer::getBukkit));
    }

    public void removeBossbar() {
        if (rushBossbar != null) {
            bossBarModule.invalidate(rushBossbar);
            bossBarModule.remove(rushBossbar);
        }
    }

    public void startTimelimit() {
        stopTimelimit();
        MatchScheduler scheduler = match.getScheduler(MatchScope.RUNNING);
        timelimitTask = scheduler.createDelayedTask(Duration.ofSeconds(config.getTimeLimit()), this::transitionToBlank);
    }

    public void transitionToBlank() {
        stopTimelimit();

        if (timer.isRunning()) {
            timer.stop();
            updateScore();
        } else {
            setScore(1);
        }

        timer.reset();
        currentParticipator = null;
        transitionTo(RushBlankState.class);
    }

    public long calculateScore() {
        return TimeUnit.SECONDS.toMillis(config.getTimeLimit()) - timer.elapsed(TimeUnit.MILLISECONDS);
    }

    private void updateScore() {
        if (hasCurrentParticipator()) {
            setScore(calculateScore());
        }
    }

    private void setScore(long score) {
        scoreModule.setScore(currentParticipator.getPlayer().getCompetitor(), score);
    }

    private void stopTimelimit() {
        if (timelimitTask != null && timelimitTask.isRunning()) {
            timelimitTask.cancel();
        }
    }

    public boolean hasParticipator() {
        return currentParticipator != null;
    }

    public boolean hasNewParticipator() {
        return newParticipator().isPresent();
    }

    public void setNewParticipator() {
        currentParticipator = newParticipator().orElse(null);
    }

    public <T extends RushTransitionState> T transitionTo(Class<T> state) {
        if (currentState != null) {
            currentState.transitionFrom();
        }

        currentState = createState(state);
        currentState.transitionTo();
        return (T) currentState;
    }

    public Stopwatch getTimer() {
        return timer;
    }

    public RushConfig getConfig() {
        return config;
    }

    public BossBarMatchModule getBossBarModule() {
        return bossBarModule;
    }

    public ScoreMatchModule getScoreModule() {
        return scoreModule;
    }

    public boolean hasCurrentParticipator() {
        return getCurrentParticipator() != null;
    }

    public RushParticipator getCurrentParticipator() {
        return currentParticipator;
    }

    public MatchPlayer getCurrentPlayer() {
        return getCurrentParticipator().getPlayer();
    }

    public RushTransitionState getCurrentState() {
        return currentState;
    }
}
