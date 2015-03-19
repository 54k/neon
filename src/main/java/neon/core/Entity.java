package neon.core;

import neon.util.Bag;
import neon.util.ImmutableList;
import neon.util.NodeCache;

import java.util.*;

public class Entity {

	long id;
	boolean scheduledForRemoval;
	NodeCache nodeCache;
	Engine.ComponentOperationHandler componentOperationHandler;

	private final List<ComponentListener> listeners;
	private final Map<Class<? extends Component>, List<ComponentListener>> listenersByComponent;

	private final Bag<Component> components;
	private final List<Component> componentsArray;
	private final ImmutableList<Component> immutableComponentsArray;
	private final BitSet componentBits;
	private final BitSet aspectBits;
	private final BitSet nodeBits;

	public Entity() {
		listeners = new ArrayList<>(16);
		listenersByComponent = new HashMap<>();

		components = new Bag<>();
		componentsArray = new ArrayList<>(16);
		immutableComponentsArray = new ImmutableList<>(componentsArray);
		componentBits = new BitSet();
		aspectBits = new BitSet();
		nodeBits = new BitSet();
	}

	public long getId() {
		return id;
	}

	public void addComponentListener(ComponentListener listener) {
		listeners.add(listener);
	}

	public void addComponentListener(Class<? extends Component> componentClass, ComponentListener listener) {
		List<ComponentListener> listeners = listenersByComponent.get(componentClass);

		if (listeners == null) {
			listeners = new ArrayList<>(16);
			listenersByComponent.put(componentClass, listeners);
		}

		listeners.add(listener);
	}

	public void removeComponentListener(ComponentListener listener) {
		listeners.remove(listener);

		for (List<ComponentListener> listeners : listenersByComponent.values()) {
			listeners.remove(listener);
		}
	}

	public Entity addComponent(Component component) {
		if (componentOperationHandler != null) {
			componentOperationHandler.add(this, component);
		} else {
			addInternal(component);
		}
		return this;
	}

	public Component removeComponent(Class<? extends Component> componentClass) {
		ComponentType componentType = ComponentType.getFor(componentClass);
		int componentTypeIndex = componentType.getIndex();
		Component removeComponent = components.get(componentTypeIndex);

		if (componentOperationHandler != null) {
			componentOperationHandler.remove(this, componentClass);
		} else {
			removeInternal(componentClass);
		}

		return removeComponent;
	}

	public void removeAllComponents() {
		if (componentOperationHandler != null) {
			componentOperationHandler.removeAll(this);
		} else {
			removeAllInternal();
		}
	}

	void removeAllInternal() {
		while (componentsArray.size() > 0) {
			removeInternal(componentsArray.get(0).getClass());
		}
	}

	public ImmutableList<Component> getComponents() {
		return immutableComponentsArray;
	}

	public <T extends Component> T getComponent(Class<T> componentClass) {
		return getComponent(ComponentType.getFor(componentClass));
	}

	@SuppressWarnings("unchecked")
	<T extends Component> T getComponent(ComponentType componentType) {
		int componentTypeIndex = componentType.getIndex();

		if (components.isIndexWithinBounds(componentTypeIndex)) {
			return (T) components.get(componentType.getIndex());
		} else {
			return null;
		}
	}

	public boolean hasComponent(Class<? extends Component> componentClass) {
		return hasComponent(ComponentType.getFor(componentClass));
	}

	boolean hasComponent(ComponentType componentType) {
		return componentBits.get(componentType.getIndex());
	}

	public <T extends Node> T asNode(Class<T> nodeClass) {
		if (nodeCache == null) {
			return NodeFamily.getFor(nodeClass).get(this);
		}

		T node = nodeCache.get(nodeClass);
		return node != null ? node : NodeFamily.getFor(nodeClass).get(this);
	}

	public boolean matchesNode(Class<? extends Node> nodeClass) {
		return NodeFamily.getFor(nodeClass).matches(this);
	}

	BitSet getComponentBits() {
		return componentBits;
	}

	BitSet getAspectBits() {
		return aspectBits;
	}

	BitSet getNodeBits() {
		return nodeBits;
	}

	Entity addInternal(Component component) {
		Class<? extends Component> componentClass = component.getClass();

		Component oldComponent = getComponent(componentClass);

		if (component == oldComponent) {
			return this;
		}

		if (oldComponent != null) {
			removeInternal(componentClass);
		}

		int componentTypeIndex = ComponentType.getIndexFor(componentClass);

		components.set(componentTypeIndex, component);
		componentsArray.add(component);
		componentBits.set(componentTypeIndex);
		notifyComponentAdded(component);
		return this;
	}

	Component removeInternal(Class<? extends Component> componentClass) {
		ComponentType componentType = ComponentType.getFor(componentClass);
		int componentTypeIndex = componentType.getIndex();
		Component removeComponent = components.get(componentTypeIndex);

		if (removeComponent != null) {
			components.remove(componentTypeIndex);
			componentsArray.remove(removeComponent);
			componentBits.clear(componentTypeIndex);
			notifyComponentRemoved(removeComponent);
		}

		return removeComponent;
	}

	private void notifyComponentAdded(Component component) {
		for (ComponentListener listener : listeners) {
			listener.componentAdded(this, component);
		}

		List<ComponentListener> listeners = listenersByComponent.get(component.getClass());
		if (listeners != null) {
			for (ComponentListener listener : listeners) {
				listener.componentAdded(this, component);
			}
		}
	}

	private void notifyComponentRemoved(Component component) {
		for (ComponentListener listener : listeners) {
			listener.componentRemoved(this, component);
		}

		List<ComponentListener> listeners = listenersByComponent.get(component.getClass());
		if (listeners != null) {
			for (ComponentListener listener : listeners) {
				listener.componentRemoved(this, component);
			}
		}
	}

	public boolean isScheduledForRemoval() {
		return scheduledForRemoval;
	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Entity)) {
			return false;
		}
		Entity other = (Entity) obj;
		return id == other.id;
	}
}
