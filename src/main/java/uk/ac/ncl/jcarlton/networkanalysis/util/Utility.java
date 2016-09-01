package uk.ac.ncl.jcarlton.networkanalysis.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

/**
 * Utility class that provides handy methods that
 * can/could be used across the scope of the
 * project.
 *
 * @author Jonathan Carlton
 * @version 1.0
 */
public class Utility {

    public Utility(){}

    /**
     * Fetch the access codes for the various api's that are
     * being used throughout the project.
     *
     * @param fileName      name of the API
     * @param arrSize       number of expected tokens
     * @return String array consisting of the
     *                      request API tokens
     */
    public String[] getTokens(String fileName, int arrSize) {
        String[] result = new String[arrSize];

        File file = new File(getClass().getResource("/access-codes/" + fileName).getFile());

        int i = 0;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result[i] = line;
                i++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     *
     * @param str
     * @return
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
     *
     *
     * @param fileName
     * @return
     */
    public JSONObject readInJSON(String fileName) {
        String resourcePath = getResourcePath();
        if (resourcePath != null) {
            File file = new File(resourcePath + "/json/" + fileName + ".json");
            if (file.exists()) {
                JSONObject result = null;
                try {
                    JSONParser parser = new JSONParser();
                    //System.out.println(parser.parse(new FileReader(file)));
                    result = (JSONObject) parser.parse(new FileReader(file));
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     *
     *
     * @param jsonObject
     * @param fileName
     */
    public void writeJSON(JSONObject jsonObject, String fileName) throws IOException {
        String resourcePath = getResourcePath();
        if (resourcePath != null) {
            File file = new File(resourcePath + "/json/" + fileName + ".json");
            String date = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(Calendar.getInstance().getTime());
            try {
                FileWriter writer;
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IOException("Error in fetching resource path");
        }
    }

    private String getResourcePath() {
        try {
            URI pathFile = System.class.getResource("/RESOURCE_PATH").toURI();
            String resourcePath = Files.readAllLines(Paths.get(pathFile)).get(0);
            URI rootURI = new File("").toURI();
            URI resourceURI = new File(resourcePath).toURI();
            URI relativeResourceURI = rootURI.relativize(resourceURI);
            return relativeResourceURI.getPath();
        } catch (Exception e) {
            return null;
        }
    }
}
