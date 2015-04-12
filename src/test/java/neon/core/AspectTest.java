package neon.core;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AspectTest extends Assert {

    @DataProvider(name = "aspects")
    public static Object[][] aspects() {
        return new Object[][]{
                {getAspectForAll(A.class, B.class, C.class)},
                {getAspectForAllNodes(ANode.class, BNode.class, CNode.class)}
        };
    }

    @Test(dataProvider = "aspects")
    public void testAspect(Aspect aspect) throws Exception {
        Entity entity = new Entity();
        entity.addComponent(new A()).addComponent(new B()).addComponent(new C());
        assertTrue(aspect.matches(entity));
        entity.removeComponent(A.class);
        assertFalse(aspect.matches(entity));
    }

    @SafeVarargs
    private static Aspect getAspectForAll(Class<? extends Component>... componentClasses) {
        return Aspect.all(componentClasses).get();
    }

    @SafeVarargs
    private static Aspect getAspectForAllNodes(Class<? extends Node>... nodeClasses) {
        return Aspect.allNodes(nodeClasses).get();
    }

    private static class A extends Component {
    }

    private static class B extends Component {
    }

    private static class C extends Component {
    }

    private static interface ANode extends Node {
        A a();
    }

    private static interface BNode extends Node {
        B b();
    }

    private static interface CNode extends Node {
        C c();
    }
}
