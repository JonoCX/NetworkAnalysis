package uk.ac.ncl.jcarlton.networkanalysis.analysis;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <h1>Link Analysis Interface</h1>
 * The interface defines the methods that a class, which
 * implements it, should also define. The methods are the
 * building blocks on which to perform Link Analysis
 * (specifically related to social media).
 *
 * @author Jonathan Carlton
 */
public interface LinkAnalysis {

    /**
     * Given a list of user ids, check to see if there are links
     * between the user passed in the creation of the object and the
     * ids in the list.
     *
     * @param users list of user ids (longs)
     * @return id mapped too true if there is a link, false if not
     */
    Map<Long, Boolean> checkForLinksFollowing(List<Long> users);

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
    Map<Long, Boolean> checkForLinksFriends(List<Long> users);

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
     * @throws IOException  thrown from the the fileIO methods that
     *                      are used in the method.
     */
    JSONObject recentActivity(List<Long> users) throws IOException;

}
