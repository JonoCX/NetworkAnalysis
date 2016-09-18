package uk.ac.ncl.jcarlton.networkanalysis.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * <h1>Utility</h1>
 * Utility class that provides handy methods that
 * can/could be used across the scope of the
 * project.
 *
 * @author Jonathan Carlton
 */
public class Utility {

    /**
     * Default object constructor
     */
    public Utility() {
    }

    /**
     * Fetch the access codes for the various api's that are
     * being used throughout the project.
     *
     * @param fileName name of the API
     * @param arrSize  number of expected tokens
     * @return String array consisting of the
     * request API tokens
     */
    public String[] getTokens(String fileName, int arrSize) {
        String[] result = new String[arrSize];


        BufferedReader reader = null;
        try {
            String currentLine;
            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/access-codes/" + fileName)));

            int i = 0;
            while ((currentLine = reader.readLine()) != null) {
                result[i] = currentLine;
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Check to see if a String is just made up of
     * whitespace.
     *
     * <b>Note:</b> An empty String, tested with .isEmpty()
     * will return true if the String representation is "". However,
     * it will return false if the String is made up of whitespace:
     * "  ".
     *
     * @param str       the String to be checked.
     *
     * @return true if the String is made up purely
     *                  of whitespace and false if it is not.
     */
    public boolean isWhitespace(String str) {
        if (str == null) return false;

        for (int i = 0; i < str.length(); i++) {
            if ((!Character.isWhitespace(str.charAt(i))))
                return false;
        }
        return true;
    }

    /**
     * Read in the stored JSON from the internal memory, given
     * a particular file name.
     *
     * @param fileName          the name of the file, for the stored
     *                          json it will be the requesting users
     *                          id.
     *
     * @return the stored information as a JSON object,
     *                          all information stored.
     *
     * @throws IOException      thrown when the file can't be found
     *                          or read just to corruption, etc.
     */
    public JSONObject readInJSON(String fileName) throws IOException {
        BufferedReader reader;
        JSONObject result = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/json/" + fileName + ".json")));
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(reader);
            result = (JSONObject) obj;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Write a JSON object to the internal memory of the device
     * that the application is running on.
     *
     * @param jsonObject        the object to be written
     *
     * @param fileName          the name of the file - usually the
     *                          user's id
     *
     * @throws IOException      thrown if the file cannot be written
     *                          to.
     */
    public void writeJSON(JSONObject jsonObject, String fileName) throws IOException {

        String date = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(Calendar.getInstance().getTime());
        FileWriter writer;
        File file = new File(fileName + ".json");
        if (!file.createNewFile()) {
            JSONObject previous = readInJSON(fileName);
            previous.put("activity_" + date, jsonObject);
            writer = new FileWriter(file);
            writer.write(previous.toJSONString());
        } else {
            writer = new FileWriter(file);
            JSONObject first = new JSONObject();
            first.put("activity_" + date, jsonObject);
            writer.write(first.toJSONString());
        }
        writer.flush();
        writer.close();

    }

    /**
     * Fetch the Maven resource path for reading/writing files to.
     * @return the resource path.
     */
    private String getResourcePath() {
        try {
            URI pathFile = System.class.getResource("/RESOURCE_PATH").toURI();
            File file = new File(pathFile);
            String resourcePath = file.getAbsolutePath();
            URI rootURI = new File("").toURI();
            URI resourceURI = new File(resourcePath).toURI();
            URI relativeResourceURI = rootURI.relativize(resourceURI);
            return relativeResourceURI.getPath();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generic method for creating a sub list safely.
     *
     * @param list          the list to be reduced in size.
     *
     * @param fromIndex     the starting index.
     *
     * @param toIndex       the finishing point.
     *
     * @param <T>           generic type of the list.
     *
     * @return the reduced list from the fromIndex
     *                      to the toIndex.
     */
    public static <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
        int size = list.size();
        if (fromIndex >= size || toIndex <= 0 || fromIndex >= toIndex) {
            return Collections.emptyList();
        }

        fromIndex = Math.max(0, fromIndex);
        toIndex = Math.min(size, toIndex);

        return list.subList(fromIndex, toIndex);
    }
}
