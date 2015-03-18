package neon.fsm;

import neon.core.Component;

import java.util.HashMap;
import java.util.Map;

public class EntityState {

    final Map<Class<? extends Component>, ComponentProvider<? extends Component>> providers;

    public EntityState() {
        providers = new HashMap<>();
    }

    public <T extends Component> ComponentMapping<T> add(Class<T> componentClass) {
        return new DefaultComponentMapping<>(providers, componentClass);
    }
}
