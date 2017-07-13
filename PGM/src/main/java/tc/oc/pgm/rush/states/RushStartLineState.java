package tc.oc.pgm.rush.states;

import tc.oc.pgm.rush.RushMatchModule;
import tc.oc.pgm.rush.RushTransitionState;

/**
 * This transition state is triggered when the current participator passes over
 * the start line. The timer will be started, which will be used to track how
 * long it takes for the participator to finish the course.
 */
public class RushStartLineState extends RushTransitionState {

    public RushStartLineState(RushMatchModule rushMatchModule) {
        super(rushMatchModule);
    }

    @Override
    protected boolean canTransition() {
        return rushMatchModule.getCurrentState() instanceof RushWaitState && rushMatchModule.hasCurrentParticipator()
               && rushMatchModule.getCurrentParticipator().collidesWith(rushMatchModule.getConfig().getStartLine());
    }

    @Override
    protected void transition() {
        rushMatchModule.getTimer().start();
    }
}
