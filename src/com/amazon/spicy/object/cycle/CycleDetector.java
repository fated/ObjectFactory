package com.amazon.spicy.object.cycle;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class CycleDetector {

    private Map<Type, CycleNode> index = new HashMap<>();

    private CycleNode head;
    private CycleNode tail;

    public static final class CycleNode {

        private final Type type;

        private CycleNode next;
        private CycleNode previous;

        private String string;

        private CycleNode(Type type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof CycleNode)) {
                return false;
            }
            CycleNode other = (CycleNode) object;
            return type.equals(other.type);
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(type.getTypeName());
            CycleNode node = next;
            while (node != null) {
                builder.append(" -> ");
                builder.append(node.type.getTypeName());
                node = node.next;
            }
            return builder.toString();
        }

        public CycleNode next() {
            return next;
        }

        public CycleNode previous() {
            return previous;
        }
    }

    /**
     * Start to find dependency cycle, and return cycle nodes.
     * given cycle A -> B -> C -> A, returns A -> B -> C.
     *
     * @param type type to check
     * @return cycle node detected
     */
    public CycleNode start(Type type) {
        CycleNode start = index.get(type);
        if (start != null) {
            return start;
        }

        CycleNode node = new CycleNode(type);

        if (head == null) {
            head = node;
        }

        if (tail != null) {
            tail.next = node;
            node.previous = tail;
        }

        tail = node;

        index.put(type, node);

        return start;
    }

    /**
     * Only called when no cycle found.
     */
    public void end() {
        if (tail == null) {
            throw new IllegalStateException("No nodes in graph");
        }

        index.remove(tail.type);
        if (tail.previous != null) {
            CycleNode newTail = tail.previous;
            tail.previous = null;
            newTail.next = null;
            tail = newTail;
        } else if (head == tail) {
            head = null;
            tail = null;
        }
    }
}
