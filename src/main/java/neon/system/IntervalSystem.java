package neon.system;

import neon.core.EntitySystem;
import neon.util.IntervalTimer;

public abstract class IntervalSystem extends EntitySystem {

    private final IntervalTimer timer;

    public IntervalSystem(float interval) {
        this(interval, 0);
    }

    public IntervalSystem(float interval, int priority) {
        super(priority);
        timer = new IntervalTimer(interval) {
            @Override
            protected void ready() {
                IntervalSystem.this.updateInterval();
            }
        };
    }

    @Override
    public void update(float deltaTime) {
        timer.update(deltaTime);
    }

    protected abstract void updateInterval();
}
