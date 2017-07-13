package tc.oc.pgm.rush;

import org.bukkit.util.Vector;

import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.regions.Region;

public class RushParticipator {

    private final MatchPlayer player;
    private Vector lastTo;

    public RushParticipator(MatchPlayer player) {
        this.player = player;
    }

    public Vector getLastTo() {
        return lastTo;
    }

    public void setLastTo(Vector lastTo) {
        this.lastTo = lastTo;
    }

    public MatchPlayer getPlayer() {
        return player;
    }

    public boolean collidesWith(Region region) {
        return lastTo != null && region.contains(lastTo.toBlockVector());
    }
}
