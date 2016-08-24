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
    // these need to be moved into another class. probably the accessing class once the project is packaged
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
     *
     * @param users
     * @param since
     * @return
     */
    @Override
    public JSONObject recentActivity(List<Long> users, Date since) {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
        String lastChecked = "";

        // if the date is null, then it hasn't been checked before so set it to the current date
        if (since == null)
            lastChecked = currentDate;
        else
            lastChecked = new SimpleDateFormat("dd/MM/yyyy").format(since);

        Map<String, JSONArray> favourites = processFavouritesInteractions(users, since);
        JSONArray tweetsLiked = favourites.get("tweets_liked");
        JSONArray staticUsers = favourites.get("static_users_interacted_with");
        List<Status> timelineSince = getTweets(userId, since);

        // get the text from the timeline of status updates
        List<String> textFeed = new ArrayList<>();
        for (Status s : timelineSince)
            textFeed.add(s.getText());

        JSONArray topicsPosted = topicsPosted(textFeed);


        // package the json object
        JSONObject inner = new JSONObject();
        inner.put("user_id", userId);
        inner.put("current_date", currentDate);
        inner.put("last_checked", lastChecked);
        inner.put("tweets_liked", tweetsLiked);
        inner.put("timeline_since_last_checked", timelineSince);
        inner.put("topics_posted", topicsPosted);
        inner.put("static_users_interacted_with", staticUsers);

        JSONObject outer = new JSONObject();
        outer.put("activity_" + currentDate, inner);

        JSONObject result = new JSONObject();
        result.put("user_id", userId);

        return result;
    }

    /**
     * Process the topics that the user has posted about
     * and return them in a JSON array ready to be added
     * to the recent activity json file.
     *
     * @param feed
     * @return
     */
    private JSONArray topicsPosted(List<String> feed) {
        TopicDetection detection = new TopicDetection(feed);
        Map<String, JSONArray> response = detection.detectTopicsAll();


        Map<String, Integer> countMap = new HashMap<>();
        for (Map.Entry<String, JSONArray> m : response.entrySet()) {
            JSONArray current = m.getValue();
            double probability = 0.000;
            String label = "";
            for (int i = 0; i < current.size(); i++) {
                JSONObject inner = (JSONObject) current.get(i);
                double currentProbability = (double) inner.get("probability");

                // pick the label that has the highest probability
                if (currentProbability > probability) {
                    probability = currentProbability;
                    label = (String) inner.get("label");
                }
            }

            // if the label is an empty string then it could not be determined
            if (label.isEmpty())
                System.out.println("Topic could not be determined for the string : " + m.getKey());
            else {
                if (countMap.containsKey(label))
                    countMap.put(label, countMap.get(label) + 1);
                else
                    countMap.put(label, 1);
            }
        }


        Map<String, Integer> sortedMap;
        if (countMap.size() <= 1)
            sortedMap = countMap;
        else
            sortedMap = MapSorter.valueDescending(countMap);

        // convert to JSON Array
        JSONArray result = new JSONArray();
        for (Map.Entry<String, Integer> m : sortedMap.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("topic", m.getKey());
            jsonObject.put("frequency", m.getValue());
            result.add(jsonObject);
        }

        return result;
    }



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

                // if the user has liked a tweet by one of the static users
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
                if ((s.getCreatedAt()).after(since))
                    result.add(s);

            }

            temp.clear();
            paging.setPage(paging.getPage() + 1);
        } while (temp.size() > 0);

        return result;
    }


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
