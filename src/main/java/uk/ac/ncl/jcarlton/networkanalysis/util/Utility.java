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
 * Utility class that provides handy methods that
 * can/could be used across the scope of the
 * project.
 *
 * @author Jonathan Carlton
 * @version 1.0
 */
public class Utility {

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

//        File file = new File(getClass().getResource("/access-codes/" + fileName).getFile());
//
//        int i = 0;
//        try (Scanner scanner = new Scanner(file)) {
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                result[i] = line;
//                i++;
//            }
//            scanner.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

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
     * @param fileName
     * @return
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

//        String resourcePath = getResourcePath();
//        if (resourcePath != null) {
//            File file = new File(resourcePath + "/json/" + fileName + ".json");
//            if (file.exists()) {
//                JSONObject result = null;
//                try {
//                    JSONParser parser = new JSONParser();
//                    result = (JSONObject) parser.parse(new FileReader(file));
//                } catch (IOException | ParseException e) {
//                    e.printStackTrace();
//                }
//                return result;
//            } else {
//                throw new IOException("File doesn't exist");
//            }
//        } else {
//            throw new IOException("Cannot read resource path");
//        }
    }

    /**
     * @param jsonObject
     * @param fileName
     */
    public void writeJSON(JSONObject jsonObject, String fileName) throws IOException {

        String date = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(Calendar.getInstance().getTime());
//            URL resourceURL = getClass().getResource("/json/" + fileName + ".json");
//            File file = new File(resourceURL.toURI());
        FileWriter writer;
        //PrintWriter writer = new PrintWriter(new File(this.getClass().getResource("/json/" + fileName + ".json").getPath()));
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


//        String resourcePath = getResourcePath();
//        if (resourcePath != null) {
//            System.out.println("RESOURCE PATH: " + resourcePath);
//            resourcePath = resourcePath.substring(0, 15);
//            System.out.println("RESOURCE PATH : " + resourcePath);
//            File file = new File(resourcePath + "/json/" + fileName + ".json");
//        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(getClass().getResourceAsStream("/json/" + fileName + ".json")));
//        File file = new File("/resources/json/" + fileName + ".json");
//        String date = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(Calendar.getInstance().getTime());
//        try {
//            FileWriter writer;
//            if (!file.createNewFile()) {
//                JSONObject previous = readInJSON(fileName);
//                previous.put("activity_" + date, jsonObject);
//                writer = new FileWriter(file);
//                writer.write(previous.toJSONString());
//            } else {
//                writer = new FileWriter(file);
//                JSONObject first = new JSONObject();
//                first.put("activity_" + date, jsonObject);
//                writer.write(first.toJSONString());
//            }
//            writer.flush();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        } else {
//            throw new IOException("Error in fetching resource path");
//        }
    }

    /**
     * @return
     */
    private String getResourcePath() {
        try {
            URI pathFile = System.class.getResource("/RESOURCE_PATH").toURI();
            //String resourcePath = Files.readAllLines(Paths.get(pathFile)).get(0);
            //String resourcePath = Files.readAllLines(Paths.get(pathFile)).get(0);
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
     * @param list
     * @param fromIndex
     * @param toIndex
     * @param <T>
     * @return
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
