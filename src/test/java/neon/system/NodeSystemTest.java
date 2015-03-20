package neon.system;

import neon.core.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NodeSystemTest extends Assert {

    @Test
    public void testNodeSystemTypeDiscovery() throws Exception {
        Class<TestNode> nodeClass = new NodeSystem<TestNode>() {
        }.getNodeClass();
        assertSame(nodeClass, TestNode.class);
    }

    interface TestNode extends Node {
    }
}
