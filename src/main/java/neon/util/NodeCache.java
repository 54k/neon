package neon.util;

import neon.core.Node;

import java.util.HashMap;
import java.util.Map;

public final class NodeCache {

    private final Map<Class<? extends Node>, Node> nodeCache;

    public NodeCache() {
        nodeCache = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> T get(Class<T> nodeClass) {
        return (T) nodeCache.get(nodeClass);
    }

    public <T extends Node> void put(Class<T> nodeClass, T node) {
        nodeCache.put(nodeClass, node);
    }

    public <T extends Node> void remove(Class<T> nodeClass) {
        nodeCache.remove(nodeClass);
    }
}
