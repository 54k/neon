package neon.core;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class ComponentType {

    private static final Map<Class<? extends Component>, ComponentType> componentTypes = new HashMap<>();
    private static int typeIndex = 0;

    private final int index;

    private ComponentType() {
        index = typeIndex++;
    }

    public int getIndex() {
        return index;
    }

    public static ComponentType getFor(Class<? extends Component> componentClass) {
        ComponentType type = componentTypes.get(componentClass);

        if (type == null) {
            type = new ComponentType();
            componentTypes.put(componentClass, type);
        }

        return type;
    }

    public static int getIndexFor(Class<? extends Component> componentClass) {
        return getFor(componentClass).getIndex();
    }

    @SafeVarargs
    public static BitSet getBitsFor(Class<? extends Component>... componentClasses) {
        BitSet bits = new BitSet();

        for (Class<? extends Component> componentClass : componentClasses) {
            bits.set(ComponentType.getIndexFor(componentClass));
        }

        return bits;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ComponentType other = (ComponentType) obj;
        return index == other.index;
    }
}
