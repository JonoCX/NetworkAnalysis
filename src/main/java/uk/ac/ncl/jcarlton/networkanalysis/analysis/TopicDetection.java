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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jonathan on 16/08/2016.
 */
public class TopicDetection {

    private static final String MONKEY_LEARN_BASE_URL = "https://api.monkeylearn.com/v2/classifiers/cl_5icAVzKR/classify/";
    private String apiKey;
    private String[] feed;

    public TopicDetection(String[] feed) {
        this.feed = feed;
        setup();
    }

    private void setup() {
        Utility utility = new Utility();
        String[] arr = utility.getTokens("monkeylearn", 1);
        apiKey = arr[0];
    }

    public Map<String, JSONArray> detectTopics() {
        return requestTopics();
    }

    private Map<String, JSONArray> requestTopics() {
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
            connection.setDoOutput(false);

            // send the request
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(Arrays.asList(feed));
            jsonObject.put("text_list", jsonArray);
            writer.write(jsonObject.toJSONString());
            writer.flush();
            writer.close();

            // read input stream
            int code = connection.getResponseCode();
            BufferedReader input;
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

    private Map<String, JSONArray> processResponse(String response) {
        Map<String, JSONArray> result = new HashMap<>();

        try {
            JSONParser parser = new JSONParser();
            JSONObject parsedObject = (JSONObject) parser.parse(response);

            // get the json array's of json arrays'
            JSONArray resultArr = (JSONArray) parsedObject.get("result");

            for (int i = 0; i < resultArr.size(); i++)
                result.put(feed[i], (JSONArray) resultArr.get(i));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

}
