package neon.system;

import neon.core.Node;
import neon.util.ImmutableList;
import neon.util.IntervalTimer;

public abstract class IntervalNodeSystem<T extends Node> extends NodeSystem<T> {

    private final IntervalTimer timer;

    public IntervalNodeSystem(float interval) {
        this(interval, 0);
    }

    public IntervalNodeSystem(float interval, int priority) {
        super(priority);
        timer = new IntervalTimer(interval) {
            @Override
            protected void ready() {
                IntervalNodeSystem.this.updateInterval();
            }
        };
    }

    @Override
    public void update(float deltaTime) {
        timer.update(deltaTime);
    }

    private void updateInterval() {
        ImmutableList<T> nodes = getNodes();
        int size = nodes.size();
        for (int i = 0; i < size; ++i) {
            processNode(nodes.get(i));
        }
    }

    protected abstract void processNode(T node);
}
