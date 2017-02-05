package tc.oc.commons.bukkit.sparklings;

public abstract class SparklingResult implements Runnable {
    protected boolean success = false;

    public void setSuccess(boolean newValue) {
        this.success = newValue;
        run();
    }

    @Override
    public abstract void run();
}
