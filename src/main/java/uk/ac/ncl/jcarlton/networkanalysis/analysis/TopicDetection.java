package uk.ac.ncl.jcarlton.networkanalysis.analysis;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.ncl.jcarlton.networkanalysis.util.Utility;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to perform topic detection based on a social
 * media feed.
 *
 * The topic detection is perform externally by the Monkey
 * Learn server.
 *
 * @author Jonathan Carlton
 * @version 1.0
 */
public class TopicDetection {

    private String apiKey;
    private List<String> feed;

    private static final String MONKEY_LEARN_BASE_URL = "https://api.monkeylearn.com/v2/classifiers/cl_5icAVzKR/classify/";
    private static final String URL_REGEX = "((www\\.[\\s]+)|(https?://[^\\s]+))";
    private static final String USERNAME_REGEX = "(@[A-Za-z0-9])\\w+";
    private static final String REPEATING_CHARS = "(.)\\1{3,}";


    private Utility utility;

    /**
     * Object constructor.
     *
     * @param feed  the list of strings to be
     *              used in the topic detection
     *              process.
     */
    public TopicDetection(List<String> feed) {
        setup();

        // pre-process the feed on object creation.
        this.feed = preprocessFeed(feed);
    }

    /**
     * Setup the elements of the object
     */
    private void setup() {
        utility = new Utility();

        // fetch the monkey learn api key
        String[] arr = utility.getTokens("monkeylearn", 1);
        apiKey = arr[0];
    }

    /**
     * Pre-process the feed that is being used to remove
     * common stop words; url's and username's.
     * <p>
     * Commonly, #'s are also removed from social media
     * posts, however the monkey learn api use's them
     * to help determine the topic of the string.
     * <p>
     * Remember to check if the string in the returned array
     * is empty as the pre-processing could remove the
     * entire contents of the string all together.
     * <pre>{@code string.isEmpty()}</pre>
     *
     * @param list   array of strings to be processed.
     * @return      a new array of processed strings.
     */
    private List<String> preprocessFeed(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String current : list) {
            current = current.replaceAll(URL_REGEX, "");

            current = current.replaceAll(USERNAME_REGEX, "");

            current = current.replaceAll(REPEATING_CHARS, "$1");

            if (utility.isWhitespace(current) || current.isEmpty()) continue;
            else result.add(current);
        }
        return result;
    }

    /**
     * From the feed detect the topics
     *
     * @return string -> [{label, probability}, {label, probability}]
     */
    public Map<String, JSONArray> detectTopicsAll() {
        return requestTopics("");
    }

    public JSONArray detectTopicSingular(String text) {
        Map<String, JSONArray> request = requestTopics(text);
        JSONArray result = new JSONArray();
        for (Map.Entry<String, JSONArray> m : request.entrySet())
            result = m.getValue();
        return result;
    }

    /**
     * Internal method to detect the topics through making a request
     * to the Monkey Learn servers
     * @return string -> [{label, probability}, {label, probability}]
     */
    private Map<String, JSONArray> requestTopics(String text) {
        StringBuilder builder = new StringBuilder();
        try {
            URL url = new URL(MONKEY_LEARN_BASE_URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // build the request header
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Token " + apiKey);
            connection.setRequestProperty("Content-type", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // send the request
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            if (text.isEmpty())
                jsonArray.addAll(feed);
            else
                jsonArray.add(text);
            jsonObject.put("text_list", jsonArray);
            writer.write(jsonObject.toJSONString());
            writer.flush();
            writer.close();

            // read input stream
            int code = connection.getResponseCode();
            BufferedReader input;

            // within common error codes
            if (code >= 400 && code <= 500)
                input = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            else
                input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            while ((inputLine = input.readLine()) != null)
                builder.append(inputLine);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return processResponse(builder.toString());
    }

    /**
     * Append the original string to its associated response json
     * array.
     * @param response  the response (json string) from the Monkey
     *                  Learn servers.
     * @return string -> associate response.
     */
    private Map<String, JSONArray> processResponse(String response) {
        Map<String, JSONArray> result = new HashMap<>();

        try {
            JSONParser parser = new JSONParser();
            JSONObject parsedObject = (JSONObject) parser.parse(response);

            // get the json array's of json arrays'
            JSONArray resultArr = (JSONArray) parsedObject.get("result");

            for (int i = 0; i < resultArr.size(); i++)
                result.put(feed.get(i), (JSONArray) resultArr.get(i));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<String> getFeed() {
        return feed;
    }
}
