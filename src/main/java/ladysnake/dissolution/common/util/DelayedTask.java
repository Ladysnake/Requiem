package ladysnake.dissolution.common.util;

public class DelayedTask implements Runnable {
    private final Runnable action;
    private int timer;

    public DelayedTask(Runnable action, int timer) {
        this.action = action;
        this.timer = timer;
    }

    /**
     *
     * @return <task>true</task> if the task has been completed
     */
    public boolean tick() {
        if (--this.timer < 0) {
            this.run();
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        action.run();
    }
}
