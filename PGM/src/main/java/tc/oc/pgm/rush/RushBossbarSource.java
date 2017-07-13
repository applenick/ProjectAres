package tc.oc.pgm.rush;

import org.bukkit.entity.Player;

import com.google.api.client.util.Objects;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import tc.oc.pgm.bossbar.BossBarSource;

public class RushBossbarSource implements BossBarSource {

    private final String participatorMessage = "Run to the end as fast as you can, you have %d seconds left!";
    private final String spectatorMessage = "You are currently waiting for %s to finish";

    private final RushMatchModule rushMatchModule;

    public RushBossbarSource(tc.oc.pgm.rush.RushMatchModule rushMatchModule) {
        this.rushMatchModule = rushMatchModule;
    }

    @Override
    public BaseComponent barText(Player viewer) {
        if (rushMatchModule.getCurrentParticipator() == null) {
            return new TextComponent();
        }

        if (Objects.equal(viewer, rushMatchModule.getCurrentParticipator().getPlayer().getBukkit())) {
            long elapsed = System.currentTimeMillis() - rushMatchModule.getTimelimitStart();
            long timelimit = rushMatchModule.getConfig().getTimeLimit();
            return new TextComponent(String.format(participatorMessage, timelimit - elapsed / 1000));
        }

        return new TextComponent(rushMatchModule.hasCurrentParticipator() ? String.format(spectatorMessage,
                rushMatchModule.getCurrentParticipator().getPlayer().getDisplayName()) : "");
    }

    @Override
    public float barProgress(Player viewer) {
        long elapsed = System.currentTimeMillis() - rushMatchModule.getTimelimitStart();
        long timelimit = rushMatchModule.getConfig().getTimeLimit() * 1000;
        float progress = 1f - Math.max(0f, Math.min(1f, elapsed * 100 / timelimit / 100f));
        return progress;
    }
}