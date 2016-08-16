package uk.ac.ncl.jcarlton.networkanalysis.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import uk.ac.ncl.jcarlton.networkanalysis.util.Utility;

/**
 * Created by jonathan on 16/08/2016.
 */
public class TwitterSetup {

    private String consumerKey;
    private String secretKey;
    private String accessToken;
    private String accessTokenSecret;

    public TwitterSetup() {
        setup();
    }

    public Twitter getInstance() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(secretKey)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);
        TwitterFactory factory = new TwitterFactory(builder.build());
        return factory.getInstance();
    }

    private void setup() {
        Utility utility = new Utility();
        String[] arr = utility.getTokens("twitter", 4);
        consumerKey = arr[0];
        secretKey = arr[1];
        accessToken = arr[2];
        accessTokenSecret = arr[3];
    }
}
