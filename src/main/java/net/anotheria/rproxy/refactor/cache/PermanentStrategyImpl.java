package net.anotheria.rproxy.refactor.cache;

import net.anotheria.rproxy.refactor.config.PermanentConfigImpl;
import net.anotheria.rproxy.utils.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This strategy provides to store objects on local hard drive storage using serialization.
 *
 * @param <K>
 * @param <V>
 */
public class PermanentStrategyImpl<K, V> implements ICacheStrategy<K, V> {

    private String path;
    /**
     * Map with keys for O(1) retrieving of keys and\or search
     */
    private Map<K, K> keys = new HashMap<>();

    public PermanentStrategyImpl(String path) {
        this.path = path;
    }

    public PermanentStrategyImpl(PermanentConfigImpl config) {

    }

    @Override
    public void add(K key, V value) {
        if (FileUtils.serializeObjectIntoFileInDirectory(value, key.toString(), path)) {
            keys.put(key, null);
        }

    }

    @Override
    public V get(K key) {
        if (!keys.keySet().contains(key)) {
            return null;
        }
        return (V) FileUtils.deserializeObjectFromFileFromDirectory(key.toString(), path);
    }

    @Override
    public void remove(K key) {
        if (FileUtils.removeFileFromDirectory(key.toString(), path)) {
            keys.remove(key);
        }
    }

    @Override
    public LinkedHashMap<K, V> getAllElements() {
        LinkedHashMap<K, V> m = new LinkedHashMap<>();
        for (K key : keys.keySet()) {
            m.put(key, this.get(key));
        }
        return m;
    }

    @Override
    public void printElements() {
        Map<K, V> map = this.getAllElements();
        for (K key : map.keySet()) {
            System.out.println("MD5 : " + key + "\r\n" + map.get(key));
        }
    }

    public void fillAfterRestart(String path) {
        final File folder = new File(path);
        if (folder.listFiles() == null || folder.listFiles().length == 0) {
            //System.out.println("Nothing to fill with in " + path);
            return;
        }
        for (final File f : folder.listFiles()) {
            if (f.isFile()) {
                K k = (K) f.getName();
                keys.put(k, null);
            }
        }
    }
}
