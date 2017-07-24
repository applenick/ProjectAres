package tc.oc.pgm.rush.states;

import java.time.Duration;

import org.bukkit.GameMode;

import com.google.api.client.util.Objects;

import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.rush.RushBossbarSource;
import tc.oc.pgm.rush.RushCountdown;
import tc.oc.pgm.rush.RushMatchModule;
import tc.oc.pgm.rush.RushModule;
import tc.oc.pgm.rush.RushTransitionState;

/**
 * This transition state is triggered after the previous participator has
 * finished, and a new participator has been found. While this transition state
 * is active, the participator may not move. The participator will be shown a
 * new countdown before the next transition state, which will start the
 * timelimit.
 */
public class RushCountdownState extends RushTransitionState {

    public RushCountdownState(RushMatchModule rushMatchModule) {
        super(rushMatchModule);
    }

    @Override
    protected boolean canTransition() {
        return rushMatchModule.getCurrentState() instanceof RushBlankState && rushMatchModule.hasNewParticipator();
    }

    @Override
    protected void transition() {
        rushMatchModule.setNewParticipator();

        RushCountdown countdown = new RushCountdown(rushMatchModule);
        Duration countdownDuration = Duration.ofSeconds(rushMatchModule.getConfig().getCountdown());
        rushMatchModule.getCountdownContext().start(countdown, countdownDuration);
        
        MatchPlayer participator = rushMatchModule.getCurrentPlayer();
        
        //Prepare other players first
        rushMatchModule.getMatch()
        .players()
        .filter(other -> other.isParticipating() && !Objects.equal(other, participator))
        .forEach(other -> observePlayer(other, participator));
        rushMatchModule.setBossbar(new RushBossbarSource(rushMatchModule), rushMatchModule.getMatch().players());

        //Prepare the participator
        preparePlayer(participator);
        participator.getBukkit().teleport(rushMatchModule.getConfig().getSpawnLocation(rushMatchModule.getMatch()));
    }
    
    private void preparePlayer(MatchPlayer player){
    	player.reset();
    	player.refreshInteraction();
    	player.getBukkit().setGameMode(GameMode.SURVIVAL);
    	
    	player.refreshVisibility();
    }
    
    private void observePlayer(MatchPlayer player, MatchPlayer participator){
    	player.getBukkit().setGameMode(GameMode.SPECTATOR);
    	    	
    	player.sendHotbarMessage(new TranslatableComponent(RushModule.WAIT_KEY, rushMatchModule.getCurrentParticipator().getPlayer().getColoredName()));
    	
    	player.refreshVisibility();
    }
}
