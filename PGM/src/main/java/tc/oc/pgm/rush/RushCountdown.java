package tc.oc.pgm.rush;

import java.time.Duration;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.pgm.rush.states.RushWaitState;
import tc.oc.pgm.start.PreMatchCountdown;

public class RushCountdown extends PreMatchCountdown {

    private final RushMatchModule rushMatchModule;

    public RushCountdown(RushMatchModule rushMatchModule) {
        super(rushMatchModule.getMatch());
        this.rushMatchModule = rushMatchModule;
    }

    @Override
    public BaseComponent barText(Player viewer) {
        return secondsRemaining(ChatColor.GREEN);
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
    }

    @Override
    public @Nullable Duration timeUntilMatchStart() {
        return remaining;
    }
}
