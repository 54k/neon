package neon.fsm;

import neon.core.Component;

import java.util.Map;

final class DefaultComponentMapping<T extends Component> implements ComponentMapping<T> {

    private final Map<Class<? extends Component>, ComponentProvider<? extends Component>> providers;
    private final Class<T> componentClass;

    DefaultComponentMapping(Map<Class<? extends Component>, ComponentProvider<? extends Component>> providers, Class<T> componentClass) {
        this.providers = providers;
        this.componentClass = componentClass;
        withType(componentClass);
    }

    public DefaultComponentMapping<T> withInstance(T component) {
        setProvider(new ComponentInstanceProvider<>(component));
        return this;
    }

    public DefaultComponentMapping<T> withType(Class<T> componentClass) {
        setProvider(new ComponentTypeProvider<>(componentClass));
        return this;
    }

    public DefaultComponentMapping<T> withSingleton(Class<T> componentClass) {
        setProvider(new ComponentSingletonProvider<>(componentClass));
        return this;
    }

    public DefaultComponentMapping<T> withProvider(ComponentProvider<T> provider) {
        setProvider(provider);
        return this;
    }

    private void setProvider(ComponentProvider<T> provider) {
        providers.put(componentClass, provider);
    }
}
