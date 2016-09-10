package uk.ac.ncl.jcarlton.networkanalysis.analysis;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import twitter4j.*;
import uk.ac.ncl.jcarlton.networkanalysis.util.MapSorter;
import uk.ac.ncl.jcarlton.networkanalysis.util.Utility;

import java.io.IOException;
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

    private long userId;
    private String username;
    private Twitter twitterInstance;
    private Date since;
    private List<String> feed;

    /**
     * Create an object using a user id and an pre-authenticated
     * instance of the Twitter (Twitter4j) API.
     *
     * @param userId          the id of a user from Twitter.
     * @param twitterInstance pre-authenticated instance of the Twitter4j
     *                        Twitter API.
     */
    public LinkAnalysisTwitter(long userId, Twitter twitterInstance, Date since) {
        this.userId = userId;
        this.username = null;
        this.twitterInstance = twitterInstance;
        this.since = since;
        setupFeed();
    }

    /**
     * Create an object using a username (screen name) and a pre-authenticated
     * instance of the Twitter (Twitter4j) API.
     *
     * @param username        the username (screen name) of a user from Twitter.
     * @param twitterInstance pre-authenticated instance of the Twitter4j
     *                        Twitter API.
     */
    public LinkAnalysisTwitter(String username, Twitter twitterInstance, Date since) {
        this.username = username;
        this.userId = 0;
        this.twitterInstance = twitterInstance;
        this.since = since;
        setupFeed();
    }

    private void setupFeed() {
        List<Status> rawFeed;
        feed = new ArrayList<>();
        if (userId != 0) {
            try {
                username = twitterInstance.getScreenName();
                rawFeed = getTweets(userId);
                for (Status s : rawFeed) {
                    feed.add(s.getText());
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            try {
                userId = twitterInstance.getId();
                //System.out.println(twitterInstance.getId());
                rawFeed = getTweets(userId);
                for (Status s : rawFeed)
                    feed.add(s.getText());
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
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
            for (long u : users) {
                for (long anId : ids.getIDs()) {
                    if (u == anId) {
                        result.put(anId, true);
                        break;
                    }
                }
            }

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
            System.out.println("IDS (Followers): " + ids);
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

            for (long u : users) {
                for (long anId : ids.getIDs()) {
                    if (u == anId) {
                        result.put(anId, true);
                        break;
                    }
                }
            }
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
            System.out.println("IDS (Friends): " + ids);
        } while ((cursor = ids.getNextCursor()) != 0);

        return ids;
    }

    /**
     * Package the recent activity by the user in question, ready to
     * be processed and stored.
     *
     * This 'recent activity' includes all social media activity
     * such as; the tweets they've liked, an up-to-date snapshot
     * of their timeline, the topics that they've posted about
     * since being last checked and the static users that they've
     * interacted with in the meantime.
     *
     * @param users     the static users
     * @return JSONObject of all the most recent social
     *                  media activity from the user in question.
     */
    @Override
    public JSONObject recentActivity(List<Long> users) throws IOException {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(Calendar.getInstance().getTime());
        String lastChecked = "";

        // if the date is null, then it hasn't been checked before so set it to the current date
        if (since == null)
            lastChecked = currentDate;
        else
            lastChecked = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(since);


        JSONArray topicsPosted = topicsPosted(feed);


        // package the json object
        JSONObject inner = new JSONObject();
        inner.put("user_id", userId);
        inner.put("current_date", currentDate);
        inner.put("last_checked", lastChecked);
        //inner.put("tweets_liked", tweetsLiked);
        inner.put("timeline_since_last_checked", feed);
        inner.put("topics_posted", topicsPosted);
        //inner.put("static_users_interacted_with", staticUsers);

        //JSONObject result = new JSONObject();
        //result.put("activity_" + currentDate, inner);

        //JSONObject result = new JSOnNObject();
        //result.put(userId, outer);

        Utility utility = new Utility();
        try {
            utility.writeJSON(inner, Long.toString(userId));
        } catch (IOException e) {
            e.printStackTrace();
        }


        return utility.readInJSON(Long.toString(userId));

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


    private Map<String, JSONArray> processFavouritesInteractions(List<Long> users) {
        //TopicDetection detection = new TopicDetection(feed);
        Map<String, JSONArray> result = new HashMap<>();
        JSONArray tweetsLiked = new JSONArray();
        JSONArray interactions = new JSONArray();
        try {
            List<Status> favourites = getFavourites();

            // need to send the monkey learn requests in batches.
            List<String> textBatch = new ArrayList<>();
            for (Status s : favourites)
                textBatch.add(s.getText());
            TopicDetection detection = new TopicDetection(textBatch);
            Map<String, JSONArray> detectionResult = detection.detectTopicsAll();


            JSONObject usersInteracted = new JSONObject();
            for (Status status : favourites) {
                if (users.contains(status.getUser().getId())) {
                    usersInteracted.put("user_id", status.getUser().getId());
                    usersInteracted.put("method", "favourite");
                    usersInteracted.put("tweet_created", status.getCreatedAt());
                    interactions.add(usersInteracted);
                    usersInteracted = new JSONObject();
                }
            }
            result.put("static_users_interacted_with", interactions);

            JSONObject tweetsLikedObj = new JSONObject();
//            for (Status status : favourites) {
//                for (Map.Entry<String, JSONArray> m: detectionResult.entrySet()) {
//                    if (status.getText().contains(m.getKey())) {
//                        double probability = 0.0;
//                        String label = "";
//                        for (Object t : m.getValue()) {
//                            JSONObject jsonObject = (JSONObject) t;
//                            if (probability < (double) jsonObject.get("probability")) {
//                                probability = (double) jsonObject.get("probability");
//                                label = (String) jsonObject.get("label");
//                        }
//
//
//                    }
//                }
//            }

            //System.out.println("FAVOURITES: " + favourites);

            int i = 0;
            for (Status s : favourites) {
                //System.out.println("VALUE OF i: " + i);
                // process topics
                JSONArray topic = detection.detectTopicSingular(s.getText());
                //System.out.println("TOPIC AFTER DETECTION, SINGULAR: " + topic);
                double probability = 0.0;
                String label = "";
                for (Object t : topic) {
                    JSONArray arrObj = (JSONArray) t;
                    for (Object obj : arrObj) {
                        JSONObject jsonObj = (JSONObject) obj;
                        if (probability < (double) jsonObj.get("probability")) {
                            probability = (double) jsonObj.get("probability");
                            label = (String) jsonObj.get("label");
                        }
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
//                if (users.contains(s.getUser().getId())) {
//                    usersInteracted.put("user_id", s.getUser().getId());
//                    usersInteracted.put("method", "favourite");
//                    usersInteracted.put("tweet_created", s.getCreatedAt());
//                    interactions.add(usersInteracted);
//                    usersInteracted = new JSONObject();
//                }
                i++;
            }
            result.put("tweets_liked", tweetsLiked);


        } catch (TwitterException e) {
            e.printStackTrace();
        }
        //System.out.println("RESULT: " + result);
        return result;
    }


    private List<Status> getFavourites() throws TwitterException {
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
                else if (s.getCreatedAt().before(since))
                    break outerloop;
            }

            temp.clear();
            paging.setPage(paging.getPage() + 1);
        } while (temp.size() > 0);

        return result;
    }


    private List<Status> getTweets(long userId) throws IndexOutOfBoundsException, TwitterException {
        List<Status> list = new ArrayList<>();

        Paging paging = new Paging(1);
        List<Status> temp;
        outerLoop:
        do {
            temp = twitterInstance.getUserTimeline(userId, paging);

            //System.out.println("TEMP SIZE: " + temp.size());
            Status lastStatus = temp.get(temp.size() - 1);

            for (Status s : temp) {
                if (s.getCreatedAt().after(since)) {
                    list.add(s);
                } else if (s.getCreatedAt().before(since)) {
                    break outerLoop;
                }
            }


            temp.clear();
            paging.setPage(paging.getPage() + 1);
        } while (list.size() > 0);
        //System.out.println("LIST: " + list);
        return list;
    }

    private JSONObject packageJSON() {
        return new JSONObject();
    }
}
