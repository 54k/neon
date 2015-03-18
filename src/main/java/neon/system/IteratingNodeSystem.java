package neon.system;

import neon.core.Node;
import neon.util.ImmutableList;

public abstract class IteratingNodeSystem<T extends Node> extends NodeSystem<T> {

    public IteratingNodeSystem() {
        this(0);
    }

    public IteratingNodeSystem(int priority) {
        super(priority);
    }

    @Override
    public void update(float deltaTime) {
        ImmutableList<T> nodes = getNodes();
        int size = nodes.size();
        for (int i = 0; i < size; ++i) {
            processNode(nodes.get(i), deltaTime);
        }
    }

    protected abstract void processNode(T node, float deltaTime);
}
