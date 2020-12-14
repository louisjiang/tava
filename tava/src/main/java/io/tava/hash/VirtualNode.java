package io.tava.hash;

public class VirtualNode<T extends Node> implements Node {

    private final T physicalNode;
    private final int replicaIndex;

    public VirtualNode(T physicalNode, int replicaIndex) {
        this.replicaIndex = replicaIndex;
        this.physicalNode = physicalNode;
    }

    @Override
    public String geValue() {
        return physicalNode.geValue() + "@" + replicaIndex;
    }

    public boolean isVirtualNodeOf(T pNode) {
        return physicalNode.geValue().equals(pNode.geValue());
    }

    public T getPhysicalNode() {
        return physicalNode;
    }
}
