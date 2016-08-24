package uk.ac.ncl.jcarlton.networkanalysis.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
     * @return
     */
    public JSONObject readInJSON(String fileName) {
        File file = new File(getClass().getResource("/json/" + fileName).getFile());
        JSONObject result = null;
        try {
            JSONParser parser = new JSONParser();
            result = (JSONObject) parser.parse(new FileReader(file));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void writeJSON(JSONObject jsonObject) {

    }
}
