package neon.core;

import neon.util.reflection.ClassReflection;
import neon.util.reflection.Method;

import java.lang.reflect.InvocationHandler;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public final class NodeFamily<T extends Node> {

	private static final Map<Class<? extends Node>, NodeFamily> nodeFamilies = new HashMap<>();
	private static int nodeIndex = 0;

	private final Class<T> nodeClass;
	private final BitSet bits;

	private final int index;

	private NodeFamily(Class<T> nodeClass) {
		this.nodeClass = nodeClass;
		this.bits = getBitsFor(nodeClass);
		index = nodeIndex++;
	}

	private static BitSet getBitsFor(Class<? extends Node> nodeClass) {
		BitSet bitSet = new BitSet();
		Iterable<Class<Component>> components = ClassReflection.getComponentsFor(nodeClass);
		for (Class<Component> component : components) {
			int index = ComponentType.getIndexFor(component);
			bitSet.set(index);
		}
		return bitSet;
	}

	@SafeVarargs
	public static BitSet getBitsFor(Class<? extends Node>... nodeClasses) {
		BitSet bitSet = new BitSet();
		for (Class<? extends Node> nodeClass : nodeClasses) {
			bitSet.or(getFor(nodeClass).bits);
		}
		return bitSet;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> NodeFamily<T> getFor(Class<T> nodeClass) {
		NodeFamily<T> nodeFamily = (NodeFamily<T>) nodeFamilies.get(nodeClass);
		if (nodeFamily == null) {
			nodeFamily = new NodeFamily<>(nodeClass);
			nodeFamilies.put(nodeClass, nodeFamily);
		}
		return nodeFamily;
	}

	public int getIndex() {
		return index;
	}

	public Class<T> getNodeClass() {
		return nodeClass;
	}

	public T get(Entity entity) {
		return ClassReflection.newProxyInstance(nodeClass, new NodeProxyHandler(entity));
	}

	public boolean matches(Entity entity) {
		BitSet entityComponentBits = entity.getComponentBits();
		for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
			if (!entityComponentBits.get(i)) {
				return false;
			}
		}
		return true;
	}

	private static final class NodeProxyHandler implements InvocationHandler {

		private final Entity entity;

		public NodeProxyHandler(Entity entity) {
			this.entity = entity;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) throws Throwable {
			Method method = new Method(m);

			if (method.isComponentMethod()) {
				Class<Component> componentClass = (Class<Component>) method.getReturnType();
				return entity.getComponent(ComponentType.getFor(componentClass));
			} else if (method.isGetEntityMethod()) {
				return entity;
			} else if (method.isEqualsMethod()) {
				return equals(args[0]);
			} else if (method.isHashCodeMethod()) {
				return hashCode();
			} else if (method.isToStringMethod()) {
				return entity.toString();
			}

			throw new RuntimeException("Unknown method " + method);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null) {
				return false;
			}

			if (o instanceof Entity) {
				return entity.equals(o);
			}
			Node that = (Node) o;
			return entity.equals(that.getEntity());
		}

		@Override
		public int hashCode() {
			return entity.hashCode();
		}
	}

	@Override
	public int hashCode() {
		return nodeClass.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		NodeFamily that = (NodeFamily) o;
		return nodeClass.equals(that.nodeClass);
	}
}
