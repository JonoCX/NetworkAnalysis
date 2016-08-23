package uk.ac.ncl.jcarlton.networkanalysis.analysis;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import twitter4j.*;
import uk.ac.ncl.jcarlton.networkanalysis.util.MapSorter;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Perform link analysis on a Twitter based data
 * set.
 * <p>
 * Both the users that the given user is following {@link #checkForLinksFollowing(List)}
 * and the users that are following (friends) the given user {@link #checkForLinksFriends(List)}
 * are processed for consumption.
 *
 * @author Jonathan Carlton
 * @version 1.0
 */
public class LinkAnalysisTwitter implements LinkAnalysis {

    // static users that are keys in the network
    private static final long STATIC_USER_ONE = 76805343207060275L;
    private static final long STATIC_USER_TWO = 768054311054151680L;
    private static final long STATIC_USER_THREE = 768058443362107392L;

    private long userId;
    private String username;
    private Twitter twitterInstance;

    /**
     * Create an object using a user id and an pre-authenticated
     * instance of the Twitter (Twitter4j) API.
     *
     * @param userId          the id of a user from Twitter.
     * @param twitterInstance pre-authenticated instance of the Twitter4j
     *                        Twitter API.
     */
    public LinkAnalysisTwitter(long userId, Twitter twitterInstance) {
        this.userId = userId;
        this.username = null;
        this.twitterInstance = twitterInstance;
    }

    /**
     * Create an object using a username (screen name) and a pre-authenticated
     * instance of the Twitter (Twitter4j) API.
     *
     * @param username        the username (screen name) of a user from Twitter.
     * @param twitterInstance pre-authenticated instance of the Twitter4j
     *                        Twitter API.
     */
    public LinkAnalysisTwitter(String username, Twitter twitterInstance) {
        this.username = username;
        this.userId = 0;
        this.twitterInstance = twitterInstance;
    }

    /**
     * Given a list of user ids, check to see if there are links
     * between the user passed in the creation of the object and the
     * ids in the list.
     *
     * @param users list of user ids (longs)
     * @return id mapped too true if there is a link, false if not
     */
    @Override
    public Map<Long, Boolean> checkForLinksFollowing(List<Long> users) {
        Map<Long, Boolean> result = new HashMap<>();
        try {
            for (long l : users)
                result.put(l, false);

            IDs ids = getFollowers();
            for (long anId : ids.getIDs())
                if (users.contains(anId))
                    result.put(anId, true);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Fetch the followers of the given user (passed when the object
     * was created).
     *
     * @return the IDs of the followers.
     * @throws TwitterException passed from the {@link #twitterInstance} API.
     */
    private IDs getFollowers() throws TwitterException {
        IDs ids;
        long cursor = -1;

        do {
            if (userId == 0)
                ids = twitterInstance.getFollowersIDs(username, cursor);
            else
                ids = twitterInstance.getFollowersIDs(userId, cursor);
        } while ((cursor = ids.getNextCursor()) != 0);

        return ids;
    }

    /**
     * Given a list of users, check to see if there are any established
     * links between them and the user passed at the creation of the object.
     * <p>
     * A friend, in terms of Twitter, is a user who has a following
     * relationship with another user.
     * user1 follows user2 (user1 is a friend of user2)
     *
     * @param users list of user ids (long)
     * @return id mapped too true if there is a link, false if not
     */
    @Override
    public Map<Long, Boolean> checkForLinksFriends(List<Long> users) {
        Map<Long, Boolean> result = new HashMap<>();
        try {
            for (long l : users)
                result.put(l, false);

            IDs ids = getFriends();
            for (long anId : ids.getIDs())
                if (users.contains(anId))
                    result.put(anId, true);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * Fetch the friends of a given user (passed when the
     * object was created).
     *
     * @return ids of the friends.
     * @throws TwitterException passed from the {@link #twitterInstance} API.
     */
    private IDs getFriends() throws TwitterException {
        IDs ids;
        long cursor = -1;

        do {
            if (userId == 0)
                ids = twitterInstance.getFriendsIDs(username, cursor);
            else
                ids = twitterInstance.getFriendsIDs(userId, cursor);
        } while ((cursor = ids.getNextCursor()) != 0);

        return ids;
    }

    /**
     * Look at the favourites and re-tweets of the user. Check to see if they've
     * interacted with the static users - favourite a recent tweet or re-tweeted
     * a recent tweet.
     * <p>
     * Storing the recent activity is also a must, in order to compare with the
     * newer activity. This can be ordered by the date (?) since to the time
     * of request and then patterns could be look at the in the activity?
     * The topic of the favourite/re-tweet
     * etc...
     * <p>
     * Could also look at the recent location of the user to see if their location
     * determines anything.
     * <p>
     * date1.compareTo(date2) date1 is after date2
     * <p>
     * <p>
     * return a map, similiar to above to see if they've interacted
     * with any of the users since the date?
     * <p>
     * Structure of the recent activity?
     * JSONObject?
     * <p>
     * {
     *      "user_id":
     *       {
     *          "activity_date":
     *          {
     *              "user_id":
     *              "current_date":
     *              "last_checked":
     *              "tweets_liked": [ {"tweet_text": , "tweet_topic":, "id_of_creator" :, "tweet_id":} ]
     *              "timeline_since_last_checked": [ ]
     *              "topics_posted": [ {topic:, frequency:}*ordered by most frequent* ]
     *              "static_users_interacted_with": [ {"user_id":, "method": favourite/re-tweet, "when":} ]
     *          }
     *      }
     * }
     */


    @Override
    public JSONObject recentActivity(List<Long> users, Date since) {
        JSONObject result = new JSONObject();
        Map<Long, List<Status>> retweetsPerUser = new HashMap<>();


        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
        String lastChecked = new SimpleDateFormat("dd/MM/yyyy").format(since);

        Map<String, JSONArray> favourites = processFavouritesInteractions(users, since);
        JSONArray tweetsLiked = favourites.get("tweets_liked");
        JSONArray staticUsers = favourites.get("static_users_interacted_with");
        List<Status> timelineSince = getTweets(userId, since);


        return result;
    }

    public JSONArray topicsPosted(List<String> feed) {
        TopicDetection detection = new TopicDetection(feed);
        Map<String, JSONArray> response = detection.detectTopicsAll();

        JSONArray result = new JSONArray();

        Map<String, Integer> countMap = new HashMap<>();
        for (Map.Entry<String, JSONArray> m : response.entrySet()) {
            JSONArray current = m.getValue();
            for (int i = 0; i < current.size(); i++) {
                JSONObject inner = (JSONObject) current.get(i);


            }
        }

        Map<String, Integer> sortedMap = MapSorter.valueDescending(countMap);
        System.out.println(sortedMap);
        return result;
    }

    // "tweets_liked": [ {"tweet_text": , "tweet_topic":, "id_of_creator" :, "tweet_id":} ]
    // "static_users_interacted_with": [ {"user_id":, "method": favourite/re-tweet, "when":} ]

    /**
     * @param users
     * @param since
     * @return
     */
    private Map<String, JSONArray> processFavouritesInteractions(List<Long> users, Date since) {
        TopicDetection detection = new TopicDetection(new LinkedList<String>());
        Map<String, JSONArray> result = new HashMap<>();
        JSONArray tweetsLiked = new JSONArray();
        JSONArray interactions = new JSONArray();
        try {
            List<Status> favourites = getFavourites(since);
            JSONObject tweetsLikedObj = new JSONObject();
            JSONObject usersInteracted = new JSONObject();
            for (Status s : favourites) {

                // process topics
                JSONArray topic = detection.detectTopicSingular(s.getText());
                double probability = 0.0;
                String label = "";
                for (Object t : topic) {
                    JSONObject obj = (JSONObject) t;
                    if (probability < (double) obj.get("probability")) {
                        probability = (double) obj.get("probability");
                        label = (String) obj.get("label");
                    }
                }

                tweetsLikedObj.put("tweet_text", s.getText());
                tweetsLikedObj.put("tweet_topic_label", label);
                tweetsLikedObj.put("tweet_topic_probability", probability);
                tweetsLikedObj.put("creator_id", s.getUser().getId());
                tweetsLikedObj.put("tweet_id", s.getId());
                tweetsLiked.add(tweetsLikedObj);
                tweetsLikedObj = new JSONObject();

                if (users.contains(s.getUser().getId())) {
                    usersInteracted.put("user_id", s.getUser().getId());
                    usersInteracted.put("method", "favourite");
                    usersInteracted.put("tweet_created", s.getCreatedAt());
                    interactions.add(usersInteracted);
                    usersInteracted = new JSONObject();
                }
            }
            result.put("tweets_liked", tweetsLiked);
            result.put("static_users_interacted_with", interactions);

        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param since
     * @return
     * @throws TwitterException
     */
    private List<Status> getFavourites(Date since) throws TwitterException {
        List<Status> result = new ArrayList<>();
        Paging paging = new Paging(1);
        List<Status> temp;
        outerloop:
        do {
            if (userId == 0)
                temp = twitterInstance.getFavorites(username, paging);
            else
                temp = twitterInstance.getFavorites(userId, paging);

            for (Status s : temp) {
                if ((s.getCreatedAt()).after(since)) {
                    System.out.println(s.getUser().getScreenName() + " : " + s.getUser().getId());
                    result.add(s);
                }
            }

            temp.clear();
            paging.setPage(paging.getPage() + 1);
        } while (temp.size() > 0);

        return result;
    }

    /**
     * Think about whether this is actually needed, it would
     * be handy for the analysis but is problematic.
     * <p>
     * <p>
     * Only able to get the first 100 retweets of any tweet due
     * to Twitter API limitations.
     *
     * @param userId
     * @param since
     * @return
     * @throws TwitterException
     */
    public List<Status> getRetweets(long userId, Date since) throws TwitterException {
        List<Status> tweets = getTweets(userId, since);
        List<Status> retweets = null;
        if (tweets != null) {
            Paging paging = new Paging(1);
            for (Status s : tweets) {
                retweets = twitterInstance.getRetweets(s.getId());
                paging.setPage(paging.getPage() + 1);
            }
            return retweets;
        }
        return retweets;
    }

    /**
     * @param userId
     * @return
     * @throws IndexOutOfBoundsException
     */
    private List<Status> getTweets(long userId, Date since) throws IndexOutOfBoundsException {
        List<Status> list = null;
        int pagingMax = 200;
        try {
            Paging paging = new Paging(1, pagingMax);
            list = twitterInstance.getUserTimeline(userId, paging);

            // Only get the tweets that have been posted after the since date
            int lastStatus = list.size() - 1;
            if (since.before(list.get(lastStatus).getCreatedAt()))
                return list;

        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return list;
    }

    private JSONObject packageJSON() {
        return new JSONObject();
    }
}
