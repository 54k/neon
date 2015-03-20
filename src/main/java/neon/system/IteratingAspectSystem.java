package neon.system;

import neon.core.Aspect;
import neon.core.Entity;
import neon.core.EntitySystem;
import neon.util.ImmutableList;

public abstract class IteratingAspectSystem extends EntitySystem {

    private final Aspect aspect;
    private ImmutableList<Entity> entities;

    public IteratingAspectSystem(Aspect aspect) {
        this(aspect, 0);
    }

    public IteratingAspectSystem(Aspect aspect, int priority) {
        super(priority);
        this.aspect = aspect;
    }

    @Override
    public void addedToEngine() {
        entities = getEngine().getEntitiesFor(aspect);
    }

    @Override
    public void update(float deltaTime) {
        int size = entities.size();
        for (int i = 0; i < size; ++i) {
            processEntity(entities.get(i), deltaTime);
        }
    }

    public ImmutableList<Entity> getEntities() {
        return entities;
    }

    protected abstract void processEntity(Entity entity, float deltaTime);
}
