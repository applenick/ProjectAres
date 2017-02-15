package tc.oc.pgm.destroyable;

import com.google.common.base.Preconditions;
import tc.oc.pgm.match.MatchPlayerState;
import tc.oc.pgm.goals.Contribution;

public class DestroyableContribution extends Contribution {
    private final int blocks;
    private final String name;

    public DestroyableContribution(MatchPlayerState player, double percentage, int blocks, String name) {
        super(player, percentage);
        Preconditions.checkArgument(blocks > 0, "blocks must be greater than zero");
        this.blocks = blocks;
        this.name = name;
    }
    
    public String getName(){
    	return name;
    }
    
    public int getBlocks() {
        return this.blocks;
    }
}
