package uk.ac.ncl.jcarlton.networkanalysis.analysis;

import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author Jonathan Carlton
 *         17/08/2016.
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
     * @param users
     * @return
     */
    JSONObject recentActivity(List<Long> users);

}
