
package com.samplesocialsiteapplication.web.ui.core.struts2;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * ProfileView action.
 */
public class ProfileView extends AbstractAction {

    private static Log log = LogFactory.getLog(ProfileView.class);

    private Boolean isFriend = null;

    /** userId of owner of profile being viewed */
    private String ownerId = null;

    /** status of owner of profile being viewed */
    private String status = null;

    public String execute() {
        // If owner is not specifed, then show the current user's profile
        if (ownerId == null) {
            ownerId = getViewerId();
        }
        return INPUT;
    }

    public void prepare() {
        setPageTitle("ProfileView.pageTitle");
    }


    // TODO: is there some way to get struts to automatically handle this for us?
    public void setEncodedOwnerId(String encodedOwnerId) {
        try {
            setOwnerId(URLDecoder.decode(encodedOwnerId, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            log.error("Failed to decode ownerId [" + encodedOwnerId + "]", ex);
        }
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isOwner() {
        return ownerId.equals(getViewerId());
    }

}
