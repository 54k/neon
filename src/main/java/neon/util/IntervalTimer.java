package neon.util;

public abstract class IntervalTimer {

    private float interval;
    private float accumulator;

    public IntervalTimer(float interval) {
        this.interval = interval;
    }

    public void update(float deltaTime) {
        accumulator += deltaTime;
        if (accumulator >= interval) {
            accumulator -= interval;
            ready();
        }
    }

    protected abstract void ready();
}
