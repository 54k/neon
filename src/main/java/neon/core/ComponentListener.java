package neon.core;

public interface ComponentListener {

    void componentAdded(Entity entity, Component component);

    void componentRemoved(Entity entity, Component component);
}
