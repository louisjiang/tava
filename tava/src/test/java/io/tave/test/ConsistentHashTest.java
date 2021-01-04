package io.tave.test;


import io.tava.hash.ConsistentHash;
import io.tava.hash.Node;
import io.tava.util.ArrayList;
import io.tava.util.List;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-25 16:05
 */
public class ConsistentHashTest {

    static class TestNode implements Node {

        private final String key;

        TestNode(String key) {
            this.key = key;
        }

        @Override
        public String geValue() {
            return key;
        }
    }


    public static void main(String[] args) {
        List<TestNode> nodes = new ArrayList<>();
        nodes.add(new TestNode("value1"));
        nodes.add(new TestNode("value2"));
        ConsistentHash<TestNode> consistentHash = new ConsistentHash<>(nodes, 1000);
        int v1 = 0;
        int v2 = 0;

        long l = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            TestNode testNode = consistentHash.hashNode("key" + i);
            String key = testNode.geValue();
            if (key.equals("value1")) {
                v1 += 1;
            } else {
                v2 += 1;
            }
        }
        System.out.println(v1);
        System.out.println(v2);
        System.out.println(System.currentTimeMillis() - l);

    }

}
