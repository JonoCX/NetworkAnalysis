package uk.ac.ncl.jcarlton.networkanalysis.twitter;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Twitter Utility</h1>
 * Builder class to be an instance of Twitter.
 *
 * @author Jonathan Carlton
 */
public class TwitterUtil {

    private Twitter twitterInstance;
    private String userName;
    private long userId;

    /**
     * Inner builder class.
     */
    public static class Builder {
        private Twitter twitterInstance;
        private String userName = null;
        private long userId = 0;

        /**
         * The main builder constructor, the twitter instance
         * parameter is required in order to build the object.
         *
         * @param twitterInstance non-optional parameter, an instance
         *                        of the Twitter4J Twitter object.
         */
        public Builder(Twitter twitterInstance) {
            this.twitterInstance = twitterInstance;
        }

        /**
         * When using the builder, you're able to pass an optional
         * user name to the object as part of the build process
         *
         * @param userName the user name of the Twitter user.
         * @return the constructed builder object.
         */
        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        /**
         * When using the builder, you're able to pass an optional
         * user id to the object as part of the build process.
         *
         * @param userId the user id of the Twitter user.
         * @return the constructed builder object.
         */
        public Builder userId(long userId) {
            this.userId = userId;
            return this;
        }

        /**
         * This method is called last, once the three methods are used/
         * not used. It builds the builder object to return an instance
         * of the TwitterUtil class.
         *
         * @return a TwitterUtil built object.
         */
        public TwitterUtil build() {
            return new TwitterUtil(this);
        }
    }

    /**
     * Private constructor that takes a Builder object to
     * construct the TwitterUtil object.
     *
     * @param builder the builder object with the variables
     *                already initialised.
     */
    private TwitterUtil(Builder builder) {
        this.twitterInstance = builder.twitterInstance;
        this.userName = builder.userName;
        this.userId = builder.userId;
    }

    /**
     * Fetch the tweets of a user.
     * <p>
     * <b>Note:</b> Doesnt include all the meta-data for the
     * tweets, just the actual text of the tweet.
     *
     * @param max max number of tweets to fetch.
     * @return the list of the tweets (text).
     */
    public List<String> getTweets(int max) {
        List<String> list = new ArrayList<>();
        Paging paging = new Paging(1, max);
        try {
            List<Status> statuses;
            if (userName == null)
                statuses = twitterInstance.getUserTimeline(userId, paging);
            else
                statuses = twitterInstance.getUserTimeline(userName, paging);

            for (Status s : statuses)
                list.add(s.getText());
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return list;
    }


}
