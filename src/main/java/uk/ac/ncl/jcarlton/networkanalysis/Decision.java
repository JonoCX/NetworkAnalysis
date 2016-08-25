package uk.ac.ncl.jcarlton.networkanalysis;

import org.json.simple.JSONObject;
import uk.ac.ncl.jcarlton.networkanalysis.analysis.LinkAnalysisTwitter;
import uk.ac.ncl.jcarlton.networkanalysis.twitter.TwitterSetup;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
            return false;
        }

        LinkAnalysisTwitter link = new LinkAnalysisTwitter(requestingUser, new TwitterSetup().getInstance());

        // check for the following link
        Map<Long, Boolean> followMap = link.checkForLinksFollowing(staticUsers);

        // check for the friends link
        Map<Long, Boolean> friendMap = link.checkForLinksFriends(staticUsers);

        // call check recent activity
        boolean activity = checkRecentActivity(link.recentActivity(staticUsers, lastChecked));


        return false;
    }

    /**
     * Check that the recent activity of the requested
     * user is inline with the previously stored activity.
     * <p>
     * This will identify possible account breaches if
     * the activity isn't inline with previous attempts.
     *
     * @return
     */
    private boolean checkRecentActivity(JSONObject recentActivity) {
        return true;
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
