package neon.fsm;

import neon.core.Component;
import neon.core.ComponentType;
import neon.util.reflection.ClassReflection;

public class ComponentTypeProvider<T extends Component> implements ComponentProvider<T> {

    private final Class<T> componentClass;

    public ComponentTypeProvider(Class<T> componentClass) {
        this.componentClass = componentClass;
    }

    @Override
    public T getComponent() {
        return ClassReflection.newInstance(componentClass);
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.getFor(componentClass);
    }
}
