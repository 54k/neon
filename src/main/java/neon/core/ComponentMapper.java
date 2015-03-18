package neon.core;

public class ComponentMapper<T extends Component> {

    private final ComponentType componentType;

    private ComponentMapper(Class<T> componentClass) {
        componentType = ComponentType.getFor(componentClass);
    }

    public static <T extends Component> ComponentMapper<T> getFor(Class<T> componentClass) {
        return new ComponentMapper<>(componentClass);
    }

    public T get(Entity entity) {
        return entity.getComponent(componentType);
    }

    public boolean has(Entity entity) {
        return entity.hasComponent(componentType);
    }
}
