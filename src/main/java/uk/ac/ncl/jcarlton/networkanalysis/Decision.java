package uk.ac.ncl.jcarlton.networkanalysis;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.ncl.jcarlton.networkanalysis.analysis.LinkAnalysisTwitter;
import uk.ac.ncl.jcarlton.networkanalysis.twitter.TwitterSetup;
import uk.ac.ncl.jcarlton.networkanalysis.util.MapSorter;

import java.io.IOException;
import java.util.*;

/**
 * Based on the Link Analysis, make a decision as to whether
 * or not the request user is able to authenticate with the
 * system. Returning a True (yes, they can authenticate) or
 * False (no, they cannot authenticate) result {@link #decide()}.
 * <p>
 * This class is intended as the main interface of the
 * package while other classes are mainly utility
 * class that are not designed to be used outside the
 * scope of this project. This class uses those utility
 * classes in order to make a decision.
 * <p>
 * Currently set-up for Twitter but could be changed
 * to accommodate both Twitter and Facebook.
 *
 * @author Jonathan Carlton on 24-Aug-16
 */
public class Decision {

    private List<Long> staticUsers;
    private long requestingUser;
    private Date lastChecked;

    private boolean decision;

    /**
     * Object constructor.
     * <p>
     * Need to provide a requesting user id and a list
     * of the static user id's that form the keys
     * of the network.
     *
     * @param requestingUser the user attempting to authenticate
     * @param staticUsers    a list of user ids
     */
    public Decision(long requestingUser, List<Long> staticUsers, Date lastChecked) {
        this.requestingUser = requestingUser;
        this.staticUsers = staticUsers;
        this.lastChecked = lastChecked;
    }

    /**
     * Reset option to re-try the authentication process.
     *
     * @param requestingUser the user attempting to authenticate
     * @param staticUsers    a list of user ids
     * @return a new Decision object
     */
    public Decision reset(long requestingUser, List<Long> staticUsers, Date lastChecked) {
        return new Decision(requestingUser, staticUsers, lastChecked);
    }

    /**
     * Key method which gives the result for the
     * authentication decision.
     *
     * @return the decision
     */
    public boolean decide() {
        // the process isn't going to work with null/empty/0'd variables
        if (staticUsers == null || staticUsers.isEmpty() || requestingUser == 0) {
            decision = false;
            return decision;
        }

        LinkAnalysisTwitter link = new LinkAnalysisTwitter(requestingUser, new TwitterSetup().getInstance(), lastChecked);

        boolean follow, friend, activity = false;

        // check for the following link
        Map<Long, Boolean> followMap = link.checkForLinksFollowing(staticUsers);

        follow = checkMap(followMap);


        // check for the friends link
        Map<Long, Boolean> friendMap = link.checkForLinksFriends(staticUsers);

        friend = checkMap(friendMap);

        // call check recent activity
        try {
            activity = checkRecentActivity(link.recentActivity(staticUsers));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // all true then set decision as true
        if (follow && friend && activity) decision = true;
            // else, if follow and activity is true then set decision as true
        else decision = follow && activity;

        return decision;
    }

    /**
     * Check that the recent activity of the requested
     * user is inline with the previously stored activity.
     * <p>
     * This will identify possible account breaches if
     * the activity isn't inline with previous attempts.
     *
     * @param recentActivity the stored json object of the users
     *                       recent activities
     * @return
     */
    private boolean checkRecentActivity(JSONObject recentActivity) {
        // unable to do anything with just one activity entry
        if (recentActivity.size() <= 1 || recentActivity.isEmpty())
            return false;

        Set<String> keySet = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        keySet.addAll(recentActivity.keySet());


        return topicsChecked(recentActivity, keySet);
    }

    /**
     * Check the contents of the map and return the
     * result.
     * <p>
     * Used to check the friend and follower maps.
     *
     * @param map to be checked
     * @return true/false
     */
    private boolean checkMap(Map<Long, Boolean> map) {
        if (map.isEmpty()) return false; // empty map, return false!
        else if (!map.containsValue(false)) return true; // contains no false values
        else {
            int falseCount = 0;
            int trueCount = 0;
            for (Map.Entry<Long, Boolean> m : map.entrySet()) {
                if (m.getValue()) trueCount++; // if value = true then increment
                else falseCount++; // else must be false
            }

            // contains more false values than true
            if (falseCount > trueCount) return false;
            else return falseCount < trueCount;
        }
    }

    /**
     * Check the topics that are stored in the users recent
     * activity and compare them with the previous.
     *
     * @param recentActivity
     * @param keySet
     * @return
     */
    private boolean topicsChecked(JSONObject recentActivity, Set<String> keySet) {

        Map<String, Integer> previousTopics = new HashMap<>();
        List<Boolean> topTopicCheckList = new ArrayList<>();

        for (String key : keySet) {
            // compare the topics
            JSONObject obj = (JSONObject) recentActivity.get(key);
            JSONArray topics = (JSONArray) obj.get("topics_posted");
            if (topics.isEmpty()) return false;

            if (previousTopics.isEmpty()) {
                for (Object t : topics) {
                    JSONObject innerTopicObject = (JSONObject) t;
                    previousTopics.put((String) innerTopicObject.get("topic"), Math.toIntExact((Long) innerTopicObject.get("frequency")));
                }
                previousTopics = MapSorter.valueDescending(previousTopics);
            } else {
                Map<String, Integer> currentTopics = new HashMap<>();
                for (Object t : topics) {
                    JSONObject innerTopicObject = (JSONObject) t;
                    currentTopics.put((String) innerTopicObject.get("topic"), Math.toIntExact((Long) innerTopicObject.get("frequency")));
                }
                currentTopics = MapSorter.valueDescending(currentTopics);

                // the maps are completely equal
                if (previousTopics.equals(currentTopics))
                    return true;

                int loopCounter = 0;

                for (Map.Entry<String, Integer> m : currentTopics.entrySet()) {
                    if (loopCounter != 2) {
                        topTopicCheckList.add(previousTopics.containsKey(m.getKey()));
                        loopCounter++;
                    } else {
                        break;
                    }
                }
                previousTopics = currentTopics;

            }

        }

        if (topTopicCheckList.isEmpty()) return false;
        else if (!topTopicCheckList.contains(false)) return true;
        else {
            int trueCount = 0;
            int falseCount = 0;
            for (Boolean b : topTopicCheckList) {
                if (b) trueCount++;
                else falseCount++;
            }

            if (falseCount > trueCount) return false;
            else return falseCount <= trueCount;
        }
    }


    /**
     * Getter methods for the object variables
     */

    public List<Long> getStaticUsers() {
        return staticUsers;
    }

    public long getRequestingUser() {
        return requestingUser;
    }

    public boolean isDecision() {
        return decision;
    }
}
