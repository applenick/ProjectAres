package tc.oc.pgm.rush;

import tc.oc.pgm.regions.Region;

public class RushConfig {

    public final int timeLimit;
    public final boolean regenerate;

    public final Region spawnLine;
    public final Region startLine;
    public final Region finishLine;

    public RushConfig(int timeLimit, boolean regenerate, Region spawnLine, Region startLine, Region finishLine) {
        this.timeLimit = timeLimit;
        this.regenerate = regenerate;
        this.spawnLine  =spawnLine;
        this.startLine = startLine;
        this.finishLine = finishLine;
    }

    public int getTimeLimit() {
        return timeLimit;
    }
    
    public boolean isRegenerate() {
        return regenerate;
    }

    public Region getSpawnLine() {
        return spawnLine;
    }
    
    public Region getStartLine() {
        return startLine;
    }

    public Region getFinishLine() {
        return finishLine;
    }
}
