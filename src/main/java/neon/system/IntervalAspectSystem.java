package neon.system;

import neon.core.Aspect;
import neon.core.Entity;
import neon.util.ImmutableList;

public abstract class IntervalAspectSystem extends IntervalSystem {

    private Aspect aspect;
    private ImmutableList<Entity> entities;

    public IntervalAspectSystem(Aspect aspect, float interval) {
        this(aspect, interval, 0);
    }

    public IntervalAspectSystem(Aspect aspect, float interval, int priority) {
        super(interval, priority);
        this.aspect = aspect;
    }

    @Override
    public void addedToEngine() {
        entities = getEngine().getEntitiesFor(aspect);
    }

    @Override
    protected void updateInterval() {
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            processEntity(entities.get(i));
        }
    }

    protected abstract void processEntity(Entity entity);
}
