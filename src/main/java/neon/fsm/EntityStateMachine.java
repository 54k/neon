package neon.fsm;

import neon.core.Component;
import neon.core.Entity;

import java.util.HashMap;
import java.util.Map;

public class EntityStateMachine {

    private final Map<String, EntityState> states;
    private final Entity entity;

    private EntityState currentState;

    public EntityStateMachine(Entity entity) {
        this.entity = entity;
        states = new HashMap<>();
    }

    public EntityStateMachine addState(String name, EntityState state) {
        states.put(name, state);
        return this;
    }

    public EntityState createState(String name) {
        EntityState state = new EntityState();
        addState(name, state);
        return state;
    }

    public void changeState(String name) {
        EntityState newState = getState(name);
        if (newState == currentState) {
            return;
        }

        Map<Class<? extends Component>, ComponentProvider<? extends Component>> toAdd = new HashMap<>(newState.providers);

        if (currentState != null) {
            for (Map.Entry<Class<? extends Component>, ComponentProvider<? extends Component>> entry :
                    currentState.providers.entrySet()) {

                Class<? extends Component> componentClass = entry.getKey();
                ComponentProvider<? extends Component> provider = toAdd.get(componentClass);
                if (provider != null && provider.getComponentType() == entry.getValue().getComponentType()) {
                    toAdd.remove(componentClass);
                } else {
                    entity.removeComponent(componentClass);
                }
            }
        }

        for (ComponentProvider<? extends Component> componentProvider : toAdd.values()) {
            entity.addComponent(componentProvider.getComponent());
        }

        currentState = newState;
    }

    private EntityState getState(String name) {
        EntityState state = states.get(name);
        if (state == null) {
            throw new NullPointerException("State with name " + name + " does not exists");
        }
        return state;
    }
}
