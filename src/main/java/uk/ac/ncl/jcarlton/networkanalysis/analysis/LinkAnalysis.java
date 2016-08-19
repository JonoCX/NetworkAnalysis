package uk.ac.ncl.jcarlton.networkanalysis.analysis;

import org.json.simple.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Jonathan Carlton
 *         17/08/2016.
 */
public interface LinkAnalysis {

    /**
     * @param users
     * @return
     */
    Map<Long, Boolean> checkForLinksFollowing(List<Long> users);

    /**
     *
     * @param users
     * @return
     */
    Map<Long, Boolean> checkForLinksFriends(List<Long> users);

    // unsure on the type to return.
    JSONObject recentActivity(List<Long> users, Date since);

}
