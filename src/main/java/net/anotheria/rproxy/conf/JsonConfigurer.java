package net.anotheria.rproxy.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;

import java.io.File;
import java.io.IOException;

public class JsonConfigurer {

    private static ConfigJSON configuration;
    private static JsonConfigurer instance;

    private JsonConfigurer() {

    }

    /**
     * Get parsed configuration.
     *
     * @return ConfigJSON instance
     */
    public static ConfigJSON getConfiguration() {
        return configuration;
    }

    /**
     * Force to parse document and update configuration for JsonConfigurer.class
     *
     * @param fileName path to the config file.
     */
    public static ConfigJSON parseConfigurationFile(String fileName) {
        if (instance == null) {
            instance = new JsonConfigurer();
        }
        configuration = instance.parseConfiguration(fileName);
        return configuration;
    }

    private ConfigJSON parseConfiguration(String fileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = getFile(fileName);
        if(file == null){
            return null;
        }
        try {
            return objectMapper.readValue(file, ConfigJSON.class);
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
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String... args) {
        System.out.println(JsonConfigurer.getConfiguration());
        System.out.println(JsonConfigurer.parseConfigurationFile("conf.json"));
        System.out.println(JsonConfigurer.getConfiguration());
    }
}
