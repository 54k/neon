package neon.core;

import neon.system.IteratingNodeSystem;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NodeTest extends Assert {

    @Test
    public void testNodes() throws Exception {
        Engine engine = new Engine();
        TestSystem system = new TestSystem();
        engine.addSystem(system);

        ComponentA componentA = new ComponentA();
        ComponentB componentB = new ComponentB();

        Entity entity = new Entity()
                .addComponent(componentA)
                .addComponent(componentB);

        engine.addEntity(entity);
        assertFalse(system.getNodes().isEmpty());

        assertSame(componentA, entity.asNode(ABNode.class).getA());
        assertSame(componentB, entity.asNode(ABNode.class).getB());
        engine.update(0);
    }

    private static interface ABNode extends Node {
        ComponentA getA();

        ComponentB getB();
    }

    private static class ComponentA extends Component {
    }

    private static class ComponentB extends Component {
    }

    private static class TestSystem extends IteratingNodeSystem<ABNode> {
        @Override
        protected void processNode(ABNode node, float deltaTime) {
            assertSame(node.getEntity().asNode(ABNode.class), node);
        }
    }
}
