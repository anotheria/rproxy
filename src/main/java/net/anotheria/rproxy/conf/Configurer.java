package net.anotheria.rproxy.conf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Get configuration from json file.
 */
public class Configurer {

    private static ConfigurationEntity configuration;
    private static Configurer instance;

    private Configurer() {

    }

    /**
     * Get parsed configuration.
     *
     * @return ConfigurationEntity instance
     */
    public static ConfigurationEntity getConfiguration() {
        return configuration;
    }

    /**
     * Force to parse document and update configuration for Configurer.class
     *
     * @param fileName path to the config file.
     */
    public static ConfigurationEntity parseConfigurationFile(String fileName) {
        if (instance == null) {
            instance = new Configurer();
        }
        configuration = instance.parseConfiguration(fileName);
        return configuration;
    }

    public static Map<Integer, List<ContentReplace>> getReplacementRules() {
        Map<Integer, List<ContentReplace>> map = new HashMap<>();
        if (instance == null || configuration == null || configuration.getContentReplacement() == null || configuration.getContentReplacement().isEmpty()) {
            return null;
        }


        for (String[] arr : configuration.getContentReplacement()) {
            switch (arr[2]) {
                case "url":
                    String to = arr[0];
                    String with = arr[1];
                    ContentReplace conRep = new ContentReplaceRelative(to, with);
                    //rules.add(new ContentReplaceRelative(to, with));
                    int index = Integer.parseInt(arr[3]);
                    if(map.get(index) == null){
                        List<ContentReplace> l = new LinkedList<>();
                        map.put(index, l);
                    }
                    map.get(index).add(conRep);
                    break;
            }
        }
        return map;
    }

    private ConfigurationEntity parseConfiguration(String fileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = getFile(fileName);
        if (file == null) {
            return null;
        }
        try {
            return objectMapper.readValue(file, ConfigurationEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private File getFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            return new File(classLoader.getResource(fileName).getFile());
        } catch (Exception e) {
            return null;
        }
    }
}
