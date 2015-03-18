package neon.fsm;

import neon.core.Component;
import neon.core.ComponentType;

public class ComponentInstanceProvider<T extends Component> implements ComponentProvider<T> {

    private final T component;

    public ComponentInstanceProvider(T component) {
        this.component = component;
    }

    @Override
    public T getComponent() {
        return component;
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.getFor(component.getClass());
    }
}
