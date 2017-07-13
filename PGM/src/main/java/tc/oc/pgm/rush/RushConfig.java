package tc.oc.pgm.rush;

import org.bukkit.Location;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.points.PointProvider;
import tc.oc.pgm.regions.Region;

public class RushConfig {

    public final int timeLimit;
    public final int countdown;
    public final boolean regenerate;

    public final PointProvider spawnPoint;
    public final Region startLine;
    public final Region finishLine;

    public RushConfig(int timeLimit,
                      int countdown,
                      boolean regenerate,
                      PointProvider spawnPoint,
                      Region startLine,
                      Region finishLine) {
        this.timeLimit = timeLimit;
        this.countdown = countdown;
        this.regenerate = regenerate;
        this.spawnPoint = spawnPoint;
        this.startLine = startLine;
        this.finishLine = finishLine;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getCountdown() {
        return countdown;
    }

    public boolean isRegenerate() {
        return regenerate;
    }

    public PointProvider getSpawnPoint() {
        return spawnPoint;
    }

    public Location getSpawnLocation(Match match) {
        return getSpawnPoint().getPoint(match, null);
    }

    public Region getStartLine() {
        return startLine;
    }

    public Region getFinishLine() {
        return finishLine;
    }
}
