package neon.system;

import neon.core.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NodeSystemTest extends Assert {

    @Test
    public void testNodeSystemTypeDiscovery() throws Exception {
        Class<MyNode> nodeClass = new NodeSystem<MyNode>() {
        }.getNodeClass();
        assertSame(nodeClass, MyNode.class);
    }

    interface MyNode extends Node {
    }
}
