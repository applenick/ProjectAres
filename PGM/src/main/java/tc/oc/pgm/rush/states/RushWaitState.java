package tc.oc.pgm.rush.states;

import tc.oc.pgm.rush.RushMatchModule;
import tc.oc.pgm.rush.RushTransitionState;

/**
 * This transition state is triggered after the countdown finishes, and before
 * the participator steps on the start line. Timelimit has already started, but
 * score will not start depleting until player steps on the start line. If the
 * participator fails to step on the start line before they run out of time,
 * they will be given a score of 1.
 */
public class RushWaitState extends RushTransitionState {

    public RushWaitState(RushMatchModule rushMatchModule) {
        super(rushMatchModule);
    }

    @Override
    protected boolean canTransition() {
        return false;
    }

    @Override
    protected void transition() {
        rushMatchModule.startTimelimit();
    }
}
