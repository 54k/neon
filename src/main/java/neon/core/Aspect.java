package neon.core;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public final class Aspect {

    private static int aspectIndex = 0;

    private static final Map<String, Aspect> aspects = new HashMap<>();
    private static final BitSet zeroBits = new BitSet();
    private static final Builder builder = new Builder();

    private final BitSet all;
    private final BitSet one;
    private final BitSet exclude;
    private final int index;

    private Aspect(BitSet all, BitSet any, BitSet exclude) {
        this.all = all;
        this.one = any;
        this.exclude = exclude;
        this.index = aspectIndex++;
    }

    public int getIndex() {
        return this.index;
    }

    public boolean matches(Entity entity) {
        BitSet entityComponentBits = entity.getComponentBits();

        if (entityComponentBits.isEmpty()) {
            return false;
        }

        for (int i = all.nextSetBit(0); i >= 0; i = all.nextSetBit(i + 1)) {
            if (!entityComponentBits.get(i)) {
                return false;
            }
        }

        if (!one.isEmpty() && !one.intersects(entityComponentBits)) {
            return false;
        }

        if (!exclude.isEmpty() && exclude.intersects(entityComponentBits)) {
            return false;
        }

        return true;
    }

    @SafeVarargs
    public static Builder allOf(Class<? extends Component>... componentTypes) {
        return builder.reset().allOf(componentTypes);
    }

    @SafeVarargs
    public static Builder allOfNodes(Class<? extends Node>... nodeClasses) {
        return builder.reset().allOfNodes(nodeClasses);
    }

    @SafeVarargs
    public static Builder oneOf(Class<? extends Component>... componentTypes) {
        return builder.reset().oneOf(componentTypes);
    }

    @SafeVarargs
    public static Builder oneOfNodes(Class<? extends Node>... nodeClasses) {
        return builder.reset().oneOfNodes(nodeClasses);
    }

    @SafeVarargs
    public static Builder excludeOf(Class<? extends Component>... componentTypes) {
        return builder.reset().excludeOf(componentTypes);
    }

    @SafeVarargs
    public static Builder excludeOfNodes(Class<? extends Node>... nodeClasses) {
        return builder.reset().excludeOfNodes(nodeClasses);
    }

    public static class Builder {
        private BitSet all = zeroBits;
        private BitSet one = zeroBits;
        private BitSet exclude = zeroBits;

        public Builder reset() {
            all = zeroBits;
            one = zeroBits;
            exclude = zeroBits;
            return this;
        }

        @SafeVarargs
        public final Builder allOf(Class<? extends Component>... componentClasses) {
            BitSet bits = ComponentType.getBitsFor(componentClasses);
            return all(bits);
        }

        @SafeVarargs
        public final Builder allOfNodes(Class<? extends Node>... nodeClasses) {
            BitSet bits = NodeFamily.getBitsFor(nodeClasses);
            return all(bits);
        }

        Builder all(BitSet bits) {
            bits.or(all);
            all = bits;
            return this;
        }

        @SafeVarargs
        public final Builder oneOf(Class<? extends Component>... componentClasses) {
            BitSet bits = ComponentType.getBitsFor(componentClasses);
            return one(bits);
        }

        @SafeVarargs
        public final Builder oneOfNodes(Class<? extends Node>... nodeClasses) {
            BitSet bits = NodeFamily.getBitsFor(nodeClasses);
            return one(bits);
        }

        Builder one(BitSet bits) {
            bits.or(one);
            one = bits;
            return this;
        }

        @SafeVarargs
        public final Builder excludeOf(Class<? extends Component>... componentClasses) {
            BitSet bits = ComponentType.getBitsFor(componentClasses);
            return exclude(bits);
        }

        @SafeVarargs
        public final Builder excludeOfNodes(Class<? extends Node>... nodeClasses) {
            BitSet bits = NodeFamily.getBitsFor(nodeClasses);
            return exclude(bits);
        }

        Builder exclude(BitSet bits) {
            bits.or(exclude);
            exclude = bits;
            return this;
        }

        public Aspect get() {
            String hash = getHash(all, one, exclude);
            Aspect aspect = aspects.get(hash);
            if (aspect == null) {
                aspect = new Aspect(all, one, exclude);
                aspects.put(hash, aspect);
            }
            return aspect;
        }
    }

    private static String getHash(BitSet all, BitSet one, BitSet exclude) {
        StringBuilder builder = new StringBuilder();
        if (!all.isEmpty()) {
            builder.append("{all:").append(getBitsString(all)).append("}");
        }
        if (!one.isEmpty()) {
            builder.append("{one:").append(getBitsString(one)).append("}");
        }
        if (!exclude.isEmpty()) {
            builder.append("{exclude:").append(getBitsString(exclude)).append("}");
        }
        return builder.toString();
    }

    private static String getBitsString(BitSet bits) {
        StringBuilder builder = new StringBuilder();

        int numBits = bits.length();
        for (int i = 0; i < numBits; ++i) {
            builder.append(bits.get(i) ? "1" : "0");
        }

        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + all.hashCode();
        result = prime * result + one.hashCode();
        result = prime * result + exclude.hashCode();
        result = prime * result + index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Aspect)) {
            return false;
        }
        Aspect other = (Aspect) obj;
        return index == other.index && all.equals(other.all) && one.equals(other.one) && exclude.equals(other.exclude);
    }
}
