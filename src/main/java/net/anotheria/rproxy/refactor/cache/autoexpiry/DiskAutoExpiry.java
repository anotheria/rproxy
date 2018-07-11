package net.anotheria.rproxy.refactor.cache.autoexpiry;

import net.anotheria.rproxy.refactor.cache.ICacheStrategy;
import net.anotheria.rproxy.utils.FileUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DiskAutoExpiry<K, V> extends BaseAutoExpiry<K, V> implements ICacheStrategy<K, V> {

    private String fileDirectory;
    private String metaDataFileName;


    public DiskAutoExpiry(int intervalSeconds, int timeToLiveSeconds, String fileDirectory, String metaDataFileName) {
        super(intervalSeconds, timeToLiveSeconds);
        this.fileDirectory = fileDirectory;
        this.metaDataFileName = metaDataFileName;
        loadPreviousData(metaDataFileName);
    }

    private void loadPreviousData(String metaDataFileName) {
        ConcurrentHashMap<K, Long> meta = (ConcurrentHashMap<K, Long>) FileUtils.deserializeObjectFromFileFromDirectory(metaDataFileName, fileDirectory);
        if(meta != null){
//            //Map<K, Long> m = new ConcurrentHashMap<>();
//            for(MetaData metaData : meta){
//                super.getExpiryMap().put((K) metaData.getFileName(), metaData.getCreationTimestampSeconds());
//                System.out.println(metaData.getFileName() + " -> " + metaData.getCreationTimestampSeconds());
//            }
            super.setExpiryMap(meta);
        }
    }

    @Override
    public void add(K key, V value) {
        if (FileUtils.serializeObjectIntoFileInDirectory(value, key.toString(), fileDirectory)) {
            super.add(key, value);
            FileUtils.serializeObjectIntoFileInDirectory(super.getExpiryMap(), metaDataFileName, fileDirectory);
        }
    }

    @Override
    public V get(K key) {
        //check if in memory key present
        V value = null;
        if (super.isFileInCache(key)) {
            value = (V) FileUtils.deserializeObjectFromFileFromDirectory(key.toString(), fileDirectory);
            //if no file on disk - remove from memory
            if (value == null) {
                super.remove(key);
            } else {
                super.updateCreationTimestamp(key);
                FileUtils.serializeObjectIntoFileInDirectory(super.getExpiryMap(), metaDataFileName, fileDirectory);
            }
            return value;
        }

        return null;
    }

    @Override
    public void remove(K key) {
        if (FileUtils.removeFileFromDirectory(key.toString(), fileDirectory)) {
            super.remove(key);
        }
    }

    @Override
    public LinkedHashMap<K, V> getAllElements() {
        return null;
    }

    @Override
    public void printElements() {

    }
}
