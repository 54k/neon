package neon.core;

import neon.event.Event;
import neon.event.EventListener;
import neon.event.Signal;
import neon.util.ImmutableList;
import neon.util.NodeCache;
import neon.util.Pool;
import neon.util.Pool.Disposable;

import java.util.*;
import java.util.Map.Entry;

public class Engine {

    private static final SystemComparator systemComparator = new SystemComparator();
    private static final Processor wireProcessor = new WireProcessor();

    private long nextEntityId = 1;

    private final Map<Long, Entity> entitiesById;
    private final List<Entity> entities;
    private final ImmutableList<Entity> immutableEntities;
    private final List<EntityListener> entityListeners;

    private final Queue<EntityOperation> entityOperations;
    private final EntityOperationPool entityOperationPool;

    private final List<EntitySystem> systems;
    private final ImmutableList<EntitySystem> immutableSystems;
    private final Map<Class<? extends EntitySystem>, EntitySystem> systemsByClass;

    private final Map<Aspect, List<Entity>> aspects;
    private final Map<Aspect, ImmutableList<Entity>> immutableAspects;
    private final Map<Aspect, List<EntityListener>> aspectListeners;

    private final Map<Entity, NodeCache> nodeCaches;
    private final Map<NodeFamily, List<Node>> nodes;
    private final Map<NodeFamily, ImmutableList<Node>> immutableNodes;
    private final Map<NodeFamily, List<NodeListener>> nodeListeners;

    private final ComponentListener componentListener;

    private boolean updating;
    private boolean notifying;

    private final ComponentOperationPool componentOperationsPool;
    private final Queue<ComponentOperation> componentOperations;

    private final ComponentOperationHandler componentOperationHandler;

    private final Map<Class<?>, Signal<?>> signals;
    private final Map<Class<? extends EventListener>, Event<? extends EventListener>> events;

    private final List<Processor> processors;

    private boolean initialized;

    public Engine() {
        entities = new ArrayList<>();
        immutableEntities = new ImmutableList<>(entities);
        entitiesById = new HashMap<>();
        entityOperations = new LinkedList<>();
        entityOperationPool = new EntityOperationPool();

        systems = new ArrayList<>(16);
        immutableSystems = new ImmutableList<>(systems);
        systemsByClass = new HashMap<>();

        aspects = new HashMap<>();
        immutableAspects = new HashMap<>();
        entityListeners = new ArrayList<>(16);
        aspectListeners = new HashMap<>();

        nodeCaches = new HashMap<>();
        nodes = new HashMap<>();
        immutableNodes = new HashMap<>();
        nodeListeners = new HashMap<>();

        componentListener = new MembershipUpdater(this);

        updating = false;
        notifying = false;

        componentOperationsPool = new ComponentOperationPool();
        componentOperations = new LinkedList<>();
        componentOperationHandler = new ComponentOperationHandler(this);

        signals = new HashMap<>();
        events = new HashMap<>();

        processors = new ArrayList<>();
        processors.add(wireProcessor);

        initialized = false;
    }

    private long obtainEntityId() {
        return nextEntityId++;
    }

    public void addEntity(Entity entity) {
        entity.id = obtainEntityId();
        if (updating || notifying) {
            EntityOperation operation = entityOperationPool.obtain();
            operation.entity = entity;
            operation.type = EntityOperation.Type.Add;
            entityOperations.add(operation);
        } else {
            addEntityInternal(entity);
        }
    }

    public void removeEntity(Entity entity) {
        if (updating || notifying) {
            if (entity.scheduledForRemoval) {
                return;
            }
            entity.scheduledForRemoval = true;
            EntityOperation operation = entityOperationPool.obtain();
            operation.entity = entity;
            operation.type = EntityOperation.Type.Remove;
            entityOperations.add(operation);
        } else {
            removeEntityInternal(entity);
        }
    }

    public void removeAllEntities() {
        if (updating || notifying) {
            for (Entity entity : entities) {
                entity.scheduledForRemoval = true;
            }
            EntityOperation operation = entityOperationPool.obtain();
            operation.type = EntityOperation.Type.RemoveAll;
            entityOperations.add(operation);
        } else {
            while (entities.size() > 0) {
                removeEntity(entities.get(0));
            }
        }
    }

    public Entity getEntity(long id) {
        return entitiesById.get(id);
    }

    public ImmutableList<Entity> getEntities() {
        return immutableEntities;
    }

    public void addSystem(EntitySystem system) {
        checkInitialized();
        Class<? extends EntitySystem> systemType = system.getClass();
        if (!systemsByClass.containsKey(systemType)) {
            systems.add(system);
            systemsByClass.put(systemType, system);
            system.engine = this;
            system.addedToEngine();
            Collections.sort(systems, systemComparator);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends EntitySystem> T getSystem(Class<T> systemClass) {
        return (T) systemsByClass.get(systemClass);
    }

    public ImmutableList<EntitySystem> getSystems() {
        return immutableSystems;
    }

    public ImmutableList<Entity> getEntitiesFor(Aspect aspect) {
        return registerAspect(aspect);
    }

    public <T extends Node> ImmutableList<T> getNodesFor(Class<T> nodeClass) {
        return registerNodeFamily(NodeFamily.getFor(nodeClass));
    }

    public void addEntityListener(EntityListener listener) {
        entityListeners.add(listener);
    }

    public void addEntityListener(Aspect aspect, EntityListener listener) {
        registerAspect(aspect);
        List<EntityListener> listeners = aspectListeners.get(aspect);

        if (listeners == null) {
            listeners = new ArrayList<>(16);
            aspectListeners.put(aspect, listeners);
        }

        listeners.add(listener);
    }

    public void removeEntityListener(EntityListener listener) {
        entityListeners.remove(listener);
        for (List<EntityListener> familyListenerArray : aspectListeners.values()) {
            familyListenerArray.remove(listener);
        }
    }

    public void addNodeListener(Class<? extends Node> nodeClass, NodeListener listener) {
        NodeFamily nodeFamily = NodeFamily.getFor(nodeClass);
        registerNodeFamily(nodeFamily);
        List<NodeListener> listeners = nodeListeners.get(nodeFamily);

        if (listeners == null) {
            listeners = new ArrayList<>(16);
            nodeListeners.put(nodeFamily, listeners);
        }

        listeners.add(listener);
    }

    public void removeNodeListener(NodeListener listener) {
        for (List<NodeListener> listeners : nodeListeners.values()) {
            listeners.remove(listener);
        }
    }

    public void addProcessor(Processor processor) {
        checkInitialized();
        processors.add(processor);
    }

    public void processObject(Object object) {
        for (Processor processor : processors) {
            processor.processObject(object, this);
        }
    }

    private void checkInitialized() {
        if (initialized) {
            throw new IllegalStateException("Engine has been initialized");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Signal<T> signal(Class<T> type) {
        Signal<?> signal = signals.get(type);
        if (signal == null) {
            signal = new Signal<>();
            signals.put(type, signal);
        }
        return (Signal<T>) signal;
    }

    @SuppressWarnings("unchecked")
    public <T extends EventListener> Event<T> event(Class<T> listenerClass) {
        Event<? extends EventListener> event = events.get(listenerClass);
        if (event == null) {
            event = new Event<>(listenerClass);
            events.put(listenerClass, event);
        }
        return (Event<T>) event;
    }

    public void update(float deltaTime) {
        initialize();
        updating = true;
        for (EntitySystem system : systems) {
            if (system.isEnabled()) {
                system.update(deltaTime);
            }
            processComponentOperations();
            processEntityOperations();
        }
        updating = false;
    }

    public void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        processSystems();
        for (EntitySystem system : systems) {
            system.initialize();
        }
    }

    private void processSystems() {
        for (Processor processor : processors) {
            for (EntitySystem system : immutableSystems) {
                processor.processObject(system, this);
            }
        }
    }

    private void processComponentOperations() {
        for (ComponentOperation operation : componentOperations) {
            switch (operation.type) {
                case Add:
                    operation.entity.addInternal(operation.component);
                    break;
                case Remove:
                    operation.entity.removeInternal(operation.componentClass);
                    break;
                case RemoveAll:
                    operation.entity.removeAllInternal();
                    break;
            }
            componentOperationsPool.free(operation);
        }
        componentOperations.clear();
    }

    private void processEntityOperations() {
        while (!entityOperations.isEmpty()) {
            EntityOperation operation = entityOperations.poll();
            Entity entity = operation.entity;

            switch (operation.type) {
                case Add:
                    addEntityInternal(entity);
                    break;
                case Remove:
                    removeEntityInternal(entity);
                    break;
                case RemoveAll:
                    while (entities.size() > 0) {
                        removeEntityInternal(entities.get(0));
                    }
                    break;
            }
            entityOperationPool.free(operation);
        }
    }

    protected void addEntityInternal(Entity entity) {
        entities.add(entity);
        entitiesById.put(entity.getId(), entity);
        NodeCache nodeCache = new NodeCache();
        nodeCaches.put(entity, nodeCache);
        updateMembership(entity);

        entity.addComponentListener(componentListener);
        entity.componentOperationHandler = componentOperationHandler;
        entity.nodeCache = nodeCache;

        notifying = true;
        for (EntityListener listener : new ArrayList<>(entityListeners)) {
            listener.entityAdded(entity);
        }
        notifying = false;
    }

    protected void removeEntityInternal(Entity entity) {
        entity.scheduledForRemoval = false;
        entities.remove(entity);
        entitiesById.remove(entity.getId());
        nodeCaches.remove(entity);

        if (!entity.getAspectBits().isEmpty()) {
            for (Entry<Aspect, List<Entity>> entry : aspects.entrySet()) {
                Aspect aspect = entry.getKey();
                List<Entity> aspectEntities = entry.getValue();

                if (aspect.matches(entity)) {
                    aspectEntities.remove(entity);
                    entity.getAspectBits().clear(aspect.getIndex());
                    notifyAspectListenersRemove(aspect, entity);
                }
            }
        }

        if (!entity.getNodeBits().isEmpty()) {
            for (Entry<NodeFamily, List<Node>> entry : nodes.entrySet()) {
                NodeFamily nodeFamily = entry.getKey();
                List<Node> nodeEntities = entry.getValue();

                if (nodeFamily.matches(entity)) {
                    for (int i = nodeEntities.size() - 1; i >= 0; i--) {
                        Node node = nodeEntities.get(i);
                        if (node.getEntity() == entity) {
                            nodeEntities.remove(i);
                            entity.getNodeBits().clear(nodeFamily.getIndex());
                            notifyNodeListenersRemove(nodeFamily, node);
                            break;
                        }
                    }
                }
            }
        }

        entity.removeComponentListener(componentListener);
        entity.componentOperationHandler = null;
        entity.nodeCache = null;

        notifying = true;
        for (EntityListener listener : new ArrayList<>(entityListeners)) {
            listener.entityRemoved(entity);
        }
        notifying = false;
    }

    private void updateMembership(Entity entity) {
        updateAspectMembership(entity);
        updateNodeMembership(entity);
    }

    private void updateAspectMembership(Entity entity) {
        for (Entry<Aspect, List<Entity>> entry : aspects.entrySet()) {
            Aspect aspect = entry.getKey();
            List<Entity> aspectEntities = entry.getValue();
            int aspectIndex = aspect.getIndex();

            boolean belongsToAspect = entity.getAspectBits().get(aspectIndex);
            boolean matches = aspect.matches(entity);

            if (!belongsToAspect && matches) {
                aspectEntities.add(entity);
                entity.getAspectBits().set(aspectIndex);

                notifyAspectListenersAdd(aspect, entity);
            } else if (belongsToAspect && !matches) {
                aspectEntities.remove(entity);
                entity.getAspectBits().clear(aspectIndex);

                notifyAspectListenersRemove(aspect, entity);
            }
        }
    }

    private void notifyAspectListenersAdd(Aspect aspect, Entity entity) {
        List<EntityListener> listeners = aspectListeners.get(aspect);

        if (listeners != null) {
            notifying = true;
            for (EntityListener listener : new ArrayList<>(listeners)) {
                listener.entityAdded(entity);
            }
            notifying = false;
        }
    }

    private void notifyAspectListenersRemove(Aspect aspect, Entity entity) {
        List<EntityListener> listeners = aspectListeners.get(aspect);

        if (listeners != null) {
            notifying = true;
            for (EntityListener listener : new ArrayList<>(listeners)) {
                listener.entityRemoved(entity);
            }
            notifying = false;
        }
    }

    private void updateNodeMembership(Entity entity) {
        for (Entry<NodeFamily, List<Node>> entry : nodes.entrySet()) {
            NodeFamily nodeFamily = entry.getKey();
            List<Node> nodeEntities = entry.getValue();
            int nodeIndex = nodeFamily.getIndex();

            boolean belongsToNode = entity.getNodeBits().get(nodeIndex);
            boolean matches = nodeFamily.matches(entity);

            if (!belongsToNode && matches) {
                Node node = nodeFamily.get(entity);
                nodeEntities.add(node);
                nodeCaches.get(entity).put(nodeFamily.getNodeClass(), node);
                entity.getNodeBits().set(nodeIndex);
                notifyNodeListenersAdd(nodeFamily, node);
            } else if (belongsToNode && !matches) {
                for (int i = nodeEntities.size() - 1; i >= 0; i--) {
                    Node node = nodeEntities.get(i);
                    if (node.getEntity() == entity) {
                        nodeEntities.remove(i);
                        nodeCaches.get(entity).remove(node.getClass());
                        entity.getNodeBits().clear(nodeIndex);
                        notifyNodeListenersRemove(nodeFamily, node);
                        break;
                    }
                }
            }
        }
    }

    private void notifyNodeListenersAdd(NodeFamily nodeFamily, Node node) {
        List<NodeListener> listeners = nodeListeners.get(nodeFamily);

        if (listeners != null) {
            notifying = true;
            for (NodeListener listener : new ArrayList<>(listeners)) {
                listener.nodeAdded(node);
            }
            notifying = false;
        }
    }

    private void notifyNodeListenersRemove(NodeFamily nodeFamily, Node node) {
        List<NodeListener> listeners = nodeListeners.get(nodeFamily);

        if (listeners != null) {
            notifying = true;
            for (NodeListener listener : new ArrayList<>(listeners)) {
                listener.nodeRemoved(node);
            }
            notifying = false;
        }
    }

    private ImmutableList<Entity> registerAspect(Aspect aspect) {
        ImmutableList<Entity> immutableEntities = immutableAspects.get(aspect);

        if (immutableEntities == null) {
            List<Entity> familyEntities = new ArrayList<>(16);
            immutableEntities = new ImmutableList<>(familyEntities);
            aspects.put(aspect, familyEntities);
            immutableAspects.put(aspect, immutableEntities);

            for (Entity e : entities) {
                if (aspect.matches(e)) {
                    familyEntities.add(e);
                    e.getAspectBits().set(aspect.getIndex());
                }
            }
        }

        return immutableEntities;
    }

    @SuppressWarnings("unchecked")
    private <T extends Node> ImmutableList<T> registerNodeFamily(NodeFamily<T> nodeFamily) {
        ImmutableList<T> immutableNodeEntities = (ImmutableList<T>) immutableNodes.get(nodeFamily);
        if (immutableNodeEntities == null) {
            List<T> nodeEntities = new ArrayList<>(16);
            immutableNodeEntities = new ImmutableList<>(nodeEntities);
            nodes.put(nodeFamily, (List<Node>) nodeEntities);
            immutableNodes.put(nodeFamily, (ImmutableList<Node>) immutableNodeEntities);

            for (Entity e : entities) {
                if (nodeFamily.matches(e)) {
                    T node = nodeFamily.get(e);
                    nodeCaches.get(e).put(nodeFamily.getNodeClass(), node);
                    nodeEntities.add(node);
                    e.getNodeBits().set(nodeFamily.getIndex());
                }
            }
        }
        return immutableNodeEntities;
    }

    private static class SystemComparator implements Comparator<EntitySystem> {
        @Override
        public int compare(EntitySystem a, EntitySystem b) {
            return a.priority > b.priority ? 1 : (a.priority == b.priority) ? 0 : -1;
        }
    }

    private static final class MembershipUpdater implements ComponentListener {
        private Engine engine;

        public MembershipUpdater(Engine engine) {
            this.engine = engine;
        }

        @Override
        public void componentAdded(Entity entity, Component component) {
            engine.updateMembership(entity);
        }

        @Override
        public void componentRemoved(Entity entity, Component component) {
            engine.updateMembership(entity);
        }
    }

    static final class ComponentOperationHandler {
        private Engine engine;

        private ComponentOperationHandler(Engine engine) {
            this.engine = engine;
        }

        void add(Entity entity, Component component) {
            if (engine.updating) {
                ComponentOperation operation = engine.componentOperationsPool.obtain();
                operation.makeAdd(entity, component);
                engine.componentOperations.add(operation);
            } else {
                entity.addInternal(component);
            }
        }

        void remove(Entity entity, Class<? extends Component> componentClass) {
            if (engine.updating) {
                ComponentOperation operation = engine.componentOperationsPool.obtain();
                operation.makeRemove(entity, componentClass);
                engine.componentOperations.add(operation);
            } else {
                entity.removeInternal(componentClass);
            }
        }

        void removeAll(Entity entity) {
            if (engine.updating) {
                ComponentOperation operation = engine.componentOperationsPool.obtain();
                operation.makeRemoveAll(entity);
                engine.componentOperations.add(operation);
            } else {
                entity.removeAllInternal();
            }
        }
    }

    private static final class ComponentOperation implements Disposable {
        public enum Type {
            Add,
            Remove,
            RemoveAll
        }

        Type type;
        Entity entity;
        Component component;
        Class<? extends Component> componentClass;

        void makeAdd(Entity entity, Component component) {
            this.type = Type.Add;
            this.entity = entity;
            this.component = component;
            this.componentClass = null;
        }

        void makeRemove(Entity entity, Class<? extends Component> componentClass) {
            this.type = Type.Remove;
            this.entity = entity;
            this.component = null;
            this.componentClass = componentClass;
        }

        void makeRemoveAll(Entity entity) {
            this.type = Type.RemoveAll;
            this.entity = entity;
        }

        @Override
        public void dispose() {
            type = null;
            entity = null;
            component = null;
            componentClass = null;
        }
    }

    private static final class ComponentOperationPool extends Pool<ComponentOperation> {
        @Override
        protected ComponentOperation newObject() {
            return new ComponentOperation();
        }
    }

    private static final class EntityOperation implements Disposable {
        enum Type {
            Add,
            Remove,
            RemoveAll
        }

        Type type;
        Entity entity;

        @Override
        public void dispose() {
            type = null;
            entity = null;
        }
    }

    private static final class EntityOperationPool extends Pool<EntityOperation> {
        @Override
        protected EntityOperation newObject() {
            return new EntityOperation();
        }
    }
}
