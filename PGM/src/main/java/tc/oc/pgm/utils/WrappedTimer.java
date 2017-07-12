package tc.oc.pgm.utils;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class WrappedTimer {

    private final Stopwatch timer;

    public WrappedTimer() {
        this(Stopwatch.createUnstarted());
    }

    public WrappedTimer(Stopwatch timer) {
        this.timer = timer;
    }

    public boolean isRunning() {
        return timer.isRunning();
    }

    public Stopwatch start() {
        return timer.start();
    }
    
    public boolean start(boolean flag) {
        if(flag) {
            start();
        }
        
        return flag;
    }

    public Stopwatch stop() {
        return timer.stop();
    }

    public Stopwatch reset() {
        return timer.reset();
    }

    /**
     * Resets the stopwatch and returns the elapsed time shown on this stopwatch
     * before resetting.
     * 
     * @see Stopwatch#reset()
     * @see Stopwatch#elapsed(TimeUnit)
     */
    public long reset(TimeUnit desiredUnit) {
        long elapsed = elapsed(desiredUnit);
        reset();
        return elapsed;
    }

    public long elapsed(TimeUnit desiredUnit) {
        return timer.elapsed(desiredUnit);
    }
}
