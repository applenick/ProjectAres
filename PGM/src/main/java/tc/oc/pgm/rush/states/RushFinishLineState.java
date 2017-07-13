package tc.oc.pgm.rush.states;

import tc.oc.pgm.rush.RushMatchModule;
import tc.oc.pgm.rush.RushTransitionState;

/**
 * This transition state is triggered when the current participator passes over
 * the finish line. The participator will be given a score based on how fast
 * they completed the course. The next transition state will be triggered
 * manually, and will decide whether the match should continue.
 */
public class RushFinishLineState extends RushTransitionState {

    public RushFinishLineState(RushMatchModule rushMatchModule) {
        super(rushMatchModule);
    }

    @Override
    protected boolean canTransition() {
        return rushMatchModule.getCurrentState() instanceof RushStartLineState
               && rushMatchModule.hasCurrentParticipator()
               && rushMatchModule.getCurrentParticipator().collidesWith(rushMatchModule.getConfig().getFinishLine());
    }

    @Override
    protected void transition() {
        rushMatchModule.transitionToBlank();
    }
}
