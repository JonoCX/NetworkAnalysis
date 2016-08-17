package uk.ac.ncl.jcarlton.networkanalysis.analysis;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Perform link analysis on a Twitter based data
 *  set.
 *
 *  Both the users that the given user is following {@link #checkForLinksFollowing(List)}
 *  and the users that are following (friends) the given user {@link #checkForLinksFriends(List)}
 *  are processed for consumption.
 *
 *  @author Jonathan Carlton
 *  @version 1.0
 */
public class LinkAnalysisTwitter implements LinkAnalysis {

    private long userId;
    private String username;
    private Twitter twitterInstance;

    /**
     * Create an object using a user id and an pre-authenticated
     * instance of the Twitter (Twitter4j) API.
     *
     * @param userId            the id of a user from Twitter.
     * @param twitterInstance   pre-authenticated instance of the Twitter4j
     *                          Twitter API.
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
     * @param username          the username (screen name) of a user from Twitter.
     * @param twitterInstance   pre-authenticated instance of the Twitter4j
     *                          Twitter API.
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
     * @param users             list of user ids (longs)
     * @return                  id mapped too true if there is a link, false if not
     * @throws TwitterException passed from the {@link #getFollowers()} method
     */
    @Override
    public Map<Long, Boolean> checkForLinksFollowing(List<Long> users) throws TwitterException {
        Map<Long, Boolean> result = new HashMap<>();

        for (long l : users)
            result.put(l, false);

        IDs ids = getFollowers();
        for (long anId : ids.getIDs())
            if (users.contains(anId))
                result.put(anId, true);

        return result;
    }

    /**
     * Fetch the followers of the given user (passed when the object
     * was created).
     *
     * @return                  the IDs of the followers.
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
     *
     * A friend, in terms of Twitter, is a user who has a following
     * relationship with another user.
     * user1 follows user2 (user1 is a friend of user2)
     *
     * @param users             list of user ids (long)
     * @return                  id mapped too true if there is a link, false if not
     * @throws TwitterException passed from the {@link #getFriends()} method
     */
    @Override
    public Map<Long, Boolean> checkForLinksFriends(List<Long> users) throws TwitterException {
        Map<Long, Boolean> result = new HashMap<>();
        for (long l : users)
            result.put(l, false);

        IDs ids = getFriends();
        for (long anId : ids.getIDs())
            if (users.contains(anId))
                result.put(anId, true);

        return result;
    }

    /**
     * Fetch the friends of a given user (passed when the
     * object was created).
     *
     * @return                      ids of the friends.
     * @throws TwitterException     passed from the {@link #twitterInstance} API.
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
     */


    @Override
    public void recentActivity(List<Long> users, Date since) {

    }
}
