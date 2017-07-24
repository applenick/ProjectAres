package tc.oc.pgm.rush;

import java.time.Duration;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import tc.oc.commons.bukkit.freeze.FrozenPlayer;
import tc.oc.commons.bukkit.freeze.PlayerFreezer;
import tc.oc.pgm.rush.states.RushWaitState;
import tc.oc.pgm.start.PreMatchCountdown;

public class RushCountdown extends PreMatchCountdown {

    // Hack, but it works
    @Inject private static PlayerFreezer freezer;

    private final RushMatchModule rushMatchModule;
    private FrozenPlayer frozenPlayer;

    public RushCountdown(RushMatchModule rushMatchModule) {
        super(rushMatchModule.getMatch());
        this.rushMatchModule = rushMatchModule;
    }

    
    @Override
    public void onStart(Duration remaining, Duration total) {
        super.onStart(remaining, total);
        frozenPlayer = freezer.freeze(rushMatchModule.getCurrentParticipator().getPlayer().getBukkit());
    }

    @Override
    public void onTick(Duration remaining, Duration total) {
        super.onTick(remaining, total);

        if (!rushMatchModule.hasCurrentParticipator()) {
            rushMatchModule.getCountdownContext().cancel(this);
        }
    }

    @Override
    public void onEnd(Duration total) {
        super.onEnd(total);
        rushMatchModule.transitionTo(RushWaitState.class);
        frozenPlayer.thaw();
    }

    @Override
    public void onCancel(Duration remaining, Duration total, boolean manual) {
        super.onCancel(remaining, total, manual);
        frozenPlayer.thaw();
    }

    @Override
    public @Nullable Duration timeUntilMatchStart() {
        return remaining;
    }
}
