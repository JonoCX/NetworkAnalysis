package uk.ac.ncl.jcarlton.networkanalysis.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import uk.ac.ncl.jcarlton.networkanalysis.util.Utility;

/**
 * <h1>Twitter Setup</h1>
 * Sets up a Twitter4J instance to be used in the
 * analysis of the users Twitter profile.
 *
 * @author Jonathan Carlton
 */
public class TwitterSetup {

    private String consumerKey;
    private String secretKey;
    private String accessToken;
    private String accessTokenSecret;

    /**
     * Object constructor to use when the Twitter API keys
     * are being passed manually.
     *
     * @param ck  consumer key.
     * @param sk  secret key.
     * @param at  access token.
     * @param ats access token secret.
     */
    public TwitterSetup(String ck, String sk, String at, String ats) {
        this.consumerKey = ck;
        this.secretKey = sk;
        this.accessToken = at;
        this.accessTokenSecret = ats;
        //setup();
    }

    /**
     * Default constructor whereby the API keys are
     * read in from a local file.
     */
    TwitterSetup() {
        setup();
    }

    /**
     * Builds an instance of Twitter to used to make
     * API calls.
     *
     * @return an authenticated Twitter instance.
     */
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

    /**
     * Fetches the API keys in order to correctly
     * authenticate the Twitter instance.
     *
     * <b>Note:</b> If you were to use this class
     * you would need to change this method to correctly
     * point to the place in which the API keys are
     * stored.
     */
    private void setup() {
        Utility utility = new Utility();
        String[] arr = utility.getTokens("twitter", 4);
        consumerKey = arr[0];
        secretKey = arr[1];
        accessToken = arr[2];
        accessTokenSecret = arr[3];
    }
}
