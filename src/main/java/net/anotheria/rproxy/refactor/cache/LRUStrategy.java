package net.anotheria.rproxy.refactor.cache;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUStrategy<K, V> implements ICacheStrategy<K, V> {

    private Map<K, Entry<K, V>> map = new HashMap<>();
    private int size;
    private Entry start;
    private Entry end;

    public LRUStrategy(int size) {
        this.size = size;
    }

    @Override
    public void add(K key, V value) {
        if (map.containsKey(key)) {
            Entry<K, V> e = map.get(key);
            e.value = value;
            removeNode(e);
            addToStart(e);
            //System.out.println(e);
        } else {
            Entry<K, V> e = new Entry<>(key, value, null, null);
            addToStart(e);
            map.put(key, e);

            //System.out.println(e);
        }

        if(map.size() > size){
            map.remove(end.key);
            removeLast();
        }
    }

    @Override
    public V get(K key) {
        if (!map.containsKey(key)) {
            return null;
        }

        Entry<K, V> e = map.get(key);
        removeNode(e);
        addToStart(e);
        return e.value;
    }

    @Override
    public void remove(K key) {
        if (map.containsKey(key)) {
            removeNode(map.get(key));
            map.remove(key);
        }
    }

    @Override
    public LinkedHashMap<K, V> getAllElements() {
        if(start == null){
            return null;
        }
        LinkedHashMap<K, V> elements = new LinkedHashMap<>();
        Entry<K, V> e = start;
        elements.put(e.key, e.value);
        while(hasNext(e)){
            e = e.right;
            elements.put(e.key, e.value);
        }

        return elements;
    }

    @Override
    public void printElements() {
        this.printSequence();
    }

    /**
     * Add node to start. Set neighbors.
     * @param node
     */
    private void addToStart(Entry node) {
        if (start != null) {
            start.left = node;
            node.right = start;
        }
        if(end == null){
            end = node;
        }
        node.left = null;
        start = node;
    }

    /**
     * Remove last node.
     */
    private void removeLast() {
        if (end != null) {
            end = end.left;
            end.right = null;
        }
    }

    /**
     * Remove node from list.
     */
    private void removeNode(Entry<K, V> node) {
        if (node.left != null) {
            node.left.right = node.right;
        } else {
            start = node.right;
        }

        if (node.right != null) {
            node.right.left = node.left;
        } else {
            end = node.left;
        }
    }

    private class Entry<K, V> {
        K key;
        V value;
        Entry left;
        Entry right;

        public Entry(K key, V value, Entry left, Entry right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key=" + this.key +
                    ", value=" + this.value +
                    '}';
        }
    }

    /**
     * Check if Entry has a neighbor next to it.
     * @param node
     * @return true if right != null, otherwise false
     */
    private boolean hasNext(Entry<K, V> node) {
        if (node.right != null) {
            return true;
        }
        return false;
    }

    private void printSequence() {
        System.out.println(map.size());
        Entry e = start;
        while (true) {
            System.out.println(e);
            if (!hasNext(e)) {
                break;
            }
            e = e.right;
        }
    }
}
