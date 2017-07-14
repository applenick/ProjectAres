package tc.oc.pgm.rush;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.google.common.base.Stopwatch;

import tc.oc.commons.core.scheduler.Task;
import tc.oc.pgm.bossbar.BossBarMatchModule;
import tc.oc.pgm.bossbar.BossBarSource;
import tc.oc.pgm.countdowns.CountdownContext;
import tc.oc.pgm.events.ListenerScope;
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
import tc.oc.time.Time;

@ListenerScope(MatchScope.RUNNING)
public class RushMatchModule extends MatchModule implements Listener {

    private final RushConfig config;

    private final ScoreMatchModule scoreModule;
    private final BossBarMatchModule bossBarModule;
    private final CountdownContext countdownContext;

    private final RushTransitionState[] transitionStates;
    private final Stopwatch timer;

    private final RushPlayerTracker playerTracker;

    private RushTransitionState currentState;
    private RushParticipator currentParticipator;
    private BossBarSource rushBossbar;

    private Task timelimitTask;
    private long timelimitStart;

    public RushMatchModule(Match match, RushConfig config) {
        super(match);
        this.config = config;
        this.scoreModule = match.needMatchModule(ScoreMatchModule.class);
        this.bossBarModule = match.needMatchModule(BossBarMatchModule.class);
        this.countdownContext = match.countdowns();
        this.transitionStates = new RushTransitionState[] { createState(RushBlankState.class),
                                                            createState(RushCountdownState.class),
                                                            createState(RushFinishLineState.class),
                                                            createState(RushGameEndState.class),
                                                            createState(RushStartLineState.class),
                                                            createState(RushWaitState.class) };
        this.timer = Stopwatch.createUnstarted();
        this.playerTracker = new RushPlayerTracker(this);
        this.currentState = transitionStates[0];
    }

    @Override
    public void load() {
        super.load();
        getMatch().registerEvents(playerTracker);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(playerTracker);
        super.unload();
    }

    @Repeatable(interval = @Time(milliseconds = 100), scope = MatchScope.RUNNING)
    private void tick() {
        if (rushBossbar != null) {
            bossBarModule.render(rushBossbar);
            bossBarModule.remove(rushBossbar, match.observers().map(MatchPlayer::getBukkit));
        }

        updateScore();
        updateState();
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
        timelimitTask = scheduler.createDelayedTask(Duration.ofSeconds(config.getTimeLimit()), () -> {
            if (timer.isRunning()) {
                timer.stop();
            }

            transitionToBlank();
        });
        timelimitStart = System.currentTimeMillis();
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
        if (timelimitTask != null) {
            timelimitTask.cancel();
        }

        timelimitStart = 0;
    }

    private <T extends RushTransitionState> T createState(Class<T> state) {
        try {
            return state.getDeclaredConstructor(getClass()).newInstance(this);
        } catch (Exception any) {
            // TODO: Error handling
            return null;
        }
    }

    public void updateState() {
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

    public RushPlayerTracker getPlayerTracker() {
        return playerTracker;
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

    public CountdownContext getCountdownContext() {
        return countdownContext;
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

    public long getTimelimitStart() {
        return timelimitStart == 0 ? System.currentTimeMillis() : timelimitStart;
    }
}
