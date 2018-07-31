package net.anotheria.rproxy.utils;

import java.io.*;
import java.nio.file.Files;

/**
 * Class contains useful methods for interaction with files.
 */
public class FileUtils {

    /**
     * Serialize given object to the given folder.
     * @param o object to serialize
     * @param fileName name of file
     * @param fileDir directory to store file with serialized object
     * @return true if success
     */
    public static boolean serializeObjectIntoFileInDirectory(Object o, String fileName, String fileDir) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileDir + fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(o);
            out.close();
            fileOut.close();
            return true;
        } catch (IOException i) {
            i.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieve serialized object from file
     * @param fileName
     * @param fileDir
     * @return return object if success, otherwise null
     */
    public static Object deserializeObjectFromFileFromDirectory(String fileName, String fileDir) {
        Object o;
        try {
            FileInputStream fileIn = new FileInputStream(fileDir + fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            o = in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException i) {
            i.printStackTrace();
            return null;
        }

        return o;
    }

    /**
     * Remove file from directory
     * @param fileName
     * @param fileDir
     * @return true if success
     */
    public static boolean removeFileFromDirectory(String fileName, String fileDir){
        try {
            File file = new File(fileDir + fileName);
            Files.deleteIfExists(file.toPath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
