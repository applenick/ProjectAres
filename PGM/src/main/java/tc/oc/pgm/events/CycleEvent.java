package tc.oc.pgm.events;

import org.bukkit.event.HandlerList;

import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.Match;

/**
 * Fires after a {@link Match} has completely loaded and all online players have joined.
 * If cycling from a previous match, it will be completely unloaded when this fires.
 */
public class CycleEvent extends MatchEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Match old;
    private PGMMap newMap;

    public CycleEvent(Match match, Match old, PGMMap newMap) {
        super(match);
        this.old = old;
        this.newMap = newMap;
    }

    public Match getOldMatch() {
        return this.old;
    }
    
    public PGMMap getMap(){
    	return newMap;
    }
    
    public int getMaxPlayers(){
    	return newMap.getDocument().max_players();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
