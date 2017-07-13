package tc.oc.pgm.rush;

import java.util.Optional;

public abstract class RushTransitionState {

    protected final RushMatchModule rushMatchModule;
    protected Optional<Runnable> onStart, onEnd;

    public RushTransitionState(RushMatchModule rushMatchModule) {
        this.rushMatchModule = rushMatchModule;
        this.onStart = Optional.empty();
        this.onEnd = Optional.empty();
    }

    protected void transitionTo() {
        onStart.ifPresent(Runnable::run);
        transition();
    }

    protected void transitionFrom() {
        onEnd.ifPresent(Runnable::run);
    }

    public RushMatchModule getRushMatchModule() {
        return rushMatchModule;
    }

    public void setOnStart(Runnable onStart) {
        this.onStart = Optional.ofNullable(onStart);
    }

    public void setOnEnd(Runnable onEnd) {
        this.onEnd = Optional.ofNullable(onEnd);
    }

    /**
     * Implementing classes can choose to always return false if this transition
     * should be manual.
     */
    protected abstract boolean canTransition();

    protected abstract void transition();
}
