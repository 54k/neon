package neon.core;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EngineTest extends Assert {

    private Engine engine;

    @BeforeMethod
    public void setUp() throws Exception {
        engine = new Engine();
    }

    @Test
    public void testEngine() throws Exception {
        TestSystem system = new TestSystem();
        engine.addSystem(system);
        assertTrue(system.addedToEngine);
        engine.initialize();
        assertTrue(system.initialized);
        engine.update(0);
        assertTrue(system.updated);
    }

    private static class TestSystem extends EntitySystem {

        boolean initialized;
        boolean addedToEngine;
        boolean updated;

        @Override
        public void addedToEngine() {
            addedToEngine = true;
        }

        @Override
        public void initialize() {
            initialized = true;
        }

        @Override
        public void update(float deltaTime) {
            updated = true;
        }
    }
}
