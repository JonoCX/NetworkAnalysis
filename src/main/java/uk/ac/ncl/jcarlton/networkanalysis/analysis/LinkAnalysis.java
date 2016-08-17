package uk.ac.ncl.jcarlton.networkanalysis.analysis;

import twitter4j.TwitterException;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Jonathan Carlton
 *         17/08/2016.
 */
public interface LinkAnalysis {

    Map<Long, Boolean> checkForLinksFollowing(List<Long> users) throws TwitterException;

    Map<Long, Boolean> checkForLinksFriends(List<Long> users) throws TwitterException;

    // unsure on the type to return.
    void recentActivity(List<Long> users, Date since);

}
