package neon.system;

import neon.core.Engine;
import neon.core.EntitySystem;
import neon.core.Node;
import neon.util.ImmutableList;
import neon.util.reflection.ClassReflection;

abstract class NodeSystem<T extends Node> extends EntitySystem {

    private final Class<T> nodeClass;
    private ImmutableList<T> nodes;

    public NodeSystem() {
        this(0);
    }

    @SuppressWarnings("unchecked")
    public NodeSystem(int priority) {
        super(priority);
        nodeClass = (Class<T>) ClassReflection.getElementClass(getClass(), 0);
    }

    public final Class<T> getNodeClass() {
        return nodeClass;
    }

    @Override
    public void addedToEngine(Engine engine) {
        nodes = engine.getNodesFor(nodeClass);
    }

    public final ImmutableList<T> getNodes() {
        return nodes;
    }
}
