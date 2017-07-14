package tc.oc.pgm.rush;

import org.bukkit.Location;
import org.bukkit.util.ImVector;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.points.PointProvider;
import tc.oc.pgm.points.PointProviderAttributes;
import tc.oc.pgm.points.PointProviderLocation;
import tc.oc.pgm.points.RegionPointProvider;
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
        System.out.println(spawnPoint);
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
        if (spawnPoint instanceof RegionPointProvider) {
            RegionPointProvider pointProvider = (RegionPointProvider) spawnPoint;
            PointProviderAttributes attributes = pointProvider.getAttributes();

            ImVector center = pointProvider.getRegion().getBounds().center();
            PointProviderLocation location = new PointProviderLocation(match.getWorld(), center);

            if (attributes.getYawProvider() != null) {
                location.setYaw(attributes.getYawProvider().getAngle(center));
            }

            if (attributes.getPitchProvider() != null) {
                location.setPitch(attributes.getPitchProvider().getAngle(center));
            }

            return location.add(0, 0.75, 0);
        }

        return getSpawnPoint().getPoint(match, null);
    }

    public Region getStartLine() {
        return startLine;
    }

    public Region getFinishLine() {
        return finishLine;
    }
}
