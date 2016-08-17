package uk.ac.ncl.jcarlton.networkanalysis.twitter;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonathan on 16/08/2016.
 */
public class TwitterUtil {

    private Twitter twitterInstance;
    private String userName;
    private long userId;

    /**
     *
     */
    public static class Builder {
        private Twitter twitterInstance;
        private String userName = null;
        private long userId = 0;

        public Builder(Twitter twitterInstance) {
            this.twitterInstance = twitterInstance;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder userId(long userId) {
            this.userId = userId;
            return this;
        }

        public TwitterUtil build() {
            return new TwitterUtil(this);
        }
    }

    private TwitterUtil(Builder builder) {
        this.twitterInstance = builder.twitterInstance;
        this.userName = builder.userName;
        this.userId = builder.userId;
    }

    /**
     * @param max
     * @return
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
