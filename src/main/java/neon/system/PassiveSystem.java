package neon.system;

import neon.core.EntitySystem;

public abstract class PassiveSystem extends EntitySystem {

    public PassiveSystem() {
        this(0);
    }

    public PassiveSystem(int priority) {
        super(priority);
        setEnabled(false);
    }
}
