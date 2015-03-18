package neon.fsm;

import neon.core.Component;
import neon.core.ComponentType;


public interface ComponentProvider<T extends Component> {

    T getComponent();

    ComponentType getComponentType();
}
