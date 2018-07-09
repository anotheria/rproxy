package net.anotheria.rproxy.refactor.cache;

import net.anotheria.rproxy.refactor.config.PermanentConfig;

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
public class PermanentStrategy<K, V> implements ICacheStrategy<K, V> {

    private String path;
    /**
     * Map with keys for O(1) retrieving of keys and\or search
     */
    private Map<K, K> keys = new HashMap<>();

    public PermanentStrategy(String path) {
        this.path = path;
    }

    public PermanentStrategy(PermanentConfig config) {

    }

    @Override
    public void add(K key, V value) {
        try {
            FileOutputStream fileOut = new FileOutputStream(path + key);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(value);
            out.close();
            fileOut.close();
            keys.put(key, null);
            //System.out.println("Saved to " + path + key);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    @Override
    public V get(K key) {
        if (!keys.keySet().contains(key)) {
            return null;
        }
        V e;
        try {
            FileInputStream fileIn = new FileInputStream(path + key);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            e = (V) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException i) {
            i.printStackTrace();
            return null;
        }
        //System.out.println("Got from  " + path + key);
        return e;
    }

    @Override
    public void remove(K key) {
        try {
            File file = new File(path + key);
            Files.deleteIfExists(file.toPath());
            keys.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
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
