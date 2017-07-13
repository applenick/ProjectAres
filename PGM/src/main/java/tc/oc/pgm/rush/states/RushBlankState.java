package tc.oc.pgm.rush.states;

import tc.oc.pgm.rush.RushMatchModule;
import tc.oc.pgm.rush.RushTransitionState;

/**
 * This transition state is triggered after the current participator passes over
 * the finish line (or time runs out), and before the next countdown may start.
 * While this transition state is active, it is decided whether the match should
 * continue.
 */
public class RushBlankState extends RushTransitionState {

    public RushBlankState(RushMatchModule rushMatchModule) {
        super(rushMatchModule);
    }

    @Override
    protected boolean canTransition() {
        return false;
    }

    @Override
    protected void transition() {
        if (rushMatchModule.getConfig().isRegenerate()) {
            rushMatchModule.getBlockTracker().renewAll();
        }
    }
}
