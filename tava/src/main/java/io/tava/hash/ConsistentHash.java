package io.tava.hash;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHash<T extends Node> {

    private final SortedMap<Long, VirtualNode<T>> virtualNodes = new TreeMap<>();
    private final HashFunction hashFunction;

    public ConsistentHash(Collection<T> pNodes, int vNodeCount) {
        this(pNodes, vNodeCount, new MD5HashFunction());
    }


    public ConsistentHash(Collection<T> pNodes, int vNodeCount, HashFunction hashFunction) {
        if (hashFunction == null) {
            throw new NullPointerException("Hash Function is null");
        }
        this.hashFunction = hashFunction;
        if (pNodes != null) {
            for (T pNode : pNodes) {
                addNode(pNode, vNodeCount);
            }
        }
    }


    public void addNode(T pNode, int vNodeCount) {
        if (vNodeCount < 0) throw new IllegalArgumentException("illegal virtual node counts :" + vNodeCount);
        int existingReplicas = getExistingReplicas(pNode);
        for (int i = 0; i < vNodeCount; i++) {
            VirtualNode<T> vNode = new VirtualNode<>(pNode, i + existingReplicas);
            virtualNodes.put(hashFunction.hash(vNode.getValue()), vNode);
        }
    }


    public void removeNode(T pNode) {
        Iterator<Long> it = virtualNodes.keySet().iterator();
        while (it.hasNext()) {
            Long key = it.next();
            VirtualNode<T> virtualNode = virtualNodes.get(key);
            if (virtualNode.isVirtualNodeOf(pNode)) {
                it.remove();
            }
        }
    }


    public T hashNode(String key) {
        if (virtualNodes.isEmpty()) {
            return null;
        }
        Long hashVal = hashFunction.hash(key);
        SortedMap<Long, VirtualNode<T>> tailMap = virtualNodes.tailMap(hashVal);
        Long nodeHashVal = !tailMap.isEmpty() ? tailMap.firstKey() : virtualNodes.firstKey();
        return virtualNodes.get(nodeHashVal).getPhysicalNode();
    }


    public int getExistingReplicas(T pNode) {
        int replicas = 0;
        for (VirtualNode<T> vNode : virtualNodes.values()) {
            if (vNode.isVirtualNodeOf(pNode)) {
                replicas++;
            }
        }
        return replicas;
    }

}
