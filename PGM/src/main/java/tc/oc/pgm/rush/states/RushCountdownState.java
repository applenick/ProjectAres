package tc.oc.pgm.rush.states;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.GameMode;

import com.google.api.client.util.Objects;

import net.md_5.bungee.api.chat.TextComponent;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScheduler;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.rush.RushBossbarSource;
import tc.oc.pgm.rush.RushMatchModule;
import tc.oc.pgm.rush.RushTransitionState;

/**
 * This transition state is triggered after the previous participator has
 * finished, and a new participator has been found. While this transition state
 * is active, the participator may not move. The participator will be shown a
 * new countdown before the next transition state, which will start the
 * timelimit.
 */
public class RushCountdownState extends RushTransitionState {

    private final AtomicInteger countdown = new AtomicInteger(5);
    private Task countdownTask;

    public RushCountdownState(RushMatchModule rushMatchModule) {
        super(rushMatchModule);
        countdown.set(rushMatchModule.getConfig().getCountdown());
    }

    @Override
    protected boolean canTransition() {
        return rushMatchModule.getCurrentState() instanceof RushBlankState && rushMatchModule.hasNewParticipator();
    }

    @Override
    protected void transition() {
        rushMatchModule.setNewParticipator();

        MatchScheduler scheduler = rushMatchModule.getMatch().getScheduler(MatchScope.RUNNING);
        countdownTask = scheduler.createRepeatingTask(Duration.ofSeconds(1), this::tick);

        MatchPlayer participator = rushMatchModule.getCurrentPlayer();
        participator.getBukkit().setGameMode(GameMode.ADVENTURE);
        participator.getBukkit().teleport(rushMatchModule.getConfig().getSpawnLocation(rushMatchModule.getMatch()));

        rushMatchModule.getMatch()
                       .players()
                       .filter(other -> other.isParticipating() && !Objects.equal(other, participator))
                       .forEach(other -> other.getBukkit().setGameMode(GameMode.SPECTATOR));
        rushMatchModule.setBossbar(new RushBossbarSource(rushMatchModule), rushMatchModule.getMatch().players());
    }

    private void tick() {
        MatchPlayer participator = rushMatchModule.getCurrentPlayer();

        if (!rushMatchModule.hasCurrentParticipator()
            || !Objects.equal(participator, rushMatchModule.getCurrentParticipator().getPlayer())) {
            return;
        }

        if (countdown.addAndGet(-1) <= 0) {
            countdownTask.cancel();
            rushMatchModule.transitionTo(RushWaitState.class);
            participator.showTitle(new TextComponent("GO!"), null, 5, 10, 5);
        } else {
            participator.showTitle(new TextComponent(Integer.toString(countdown.get())), null, 5, 10, 5);
        }
    }
}
