package net.anotheria.rproxy.conf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

    public static List<ContentReplace> getReplacementRules() {
        if (instance == null || configuration == null || configuration.getContentReplacement() == null || configuration.getContentReplacement().isEmpty()) {
            return null;
        }

        List<ContentReplace> rules = new LinkedList<>();
        for (String[] arr : configuration.getContentReplacement()) {
            switch (arr[2]) {
                case "url":
                    String to = arr[0];
                    String with = arr[1];
                    rules.add(new ContentReplaceRelative(to, with));
                    break;
            }
        }
        return rules;
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
