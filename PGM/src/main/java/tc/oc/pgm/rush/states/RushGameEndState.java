package tc.oc.pgm.rush.states;

import tc.oc.pgm.rush.RushMatchModule;
import tc.oc.pgm.rush.RushTransitionState;

/**
 * This transition state is triggered after the current participator passes over
 * the finish line, and there are no more participators left. This will end the
 * match.
 */
public class RushGameEndState extends RushTransitionState {

    public RushGameEndState(RushMatchModule rushMatchModule) {
        super(rushMatchModule);
    }

    @Override
    protected boolean canTransition() {
        return rushMatchModule.getCurrentState() instanceof RushBlankState && !rushMatchModule.hasNewParticipator();
    }

    @Override
    protected void transition() {
        rushMatchModule.removeBossbar();
        rushMatchModule.getMatch().end();
    }
}
