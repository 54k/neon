package neon.fsm;

import neon.core.Component;
import neon.core.ComponentType;
import neon.util.reflection.ClassReflection;

public class ComponentSingletonProvider<T extends Component> implements ComponentProvider<T> {

    private final Class<T> componentClass;
    private T instance;

    public ComponentSingletonProvider(Class<T> componentClass) {
        this.componentClass = componentClass;
    }

    @Override
    public T getComponent() {
        if (instance == null) {
            instance = ClassReflection.newInstance(componentClass);
        }
        return instance;
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.getFor(componentClass);
    }
}
