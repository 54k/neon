package neon.core;

import neon.event.Event;
import neon.event.EventListener;
import neon.event.Signal;

public abstract class EntitySystem {

    final int priority;
    private boolean enabled;
    Engine engine;

    public EntitySystem() {
        this(0);
    }

    public EntitySystem(int priority) {
        this.priority = priority;
        this.enabled = true;
    }

    public void addedToEngine() {
    }

    public void initialize() {
    }

    public void update(float deltaTime) {
    }

    public final Engine getEngine() {
        return engine;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public final <T> Signal<T> signal(Class<T> type) {
        return engine.signal(type);
    }

    public final <T extends EventListener> Event<T> event(Class<T> listenerType) {
        return engine.event(listenerType);
    }
}
