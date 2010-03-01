/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.samplesocialsiteapplication.web.ui.core.struts2;

/**
 *
 * @author nic
 */
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import com.sun.socialsite.config.Config;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Extends the Struts2 ActionSupport class to add in support for handling an
 * error and status success.  Other actions extending this one only need to
 * calle setError() and setSuccess() accordingly.
 *
 * NOTE: as a small convenience, all errors and messages are assumed to be keys
 * which point to a success in a resource bundle, so we automatically call
 * getText(key) on the param passed into setError() and setSuccess().
 */
public abstract class AbstractAction extends ActionSupport implements Preparable {

    private static Log log = LogFactory.getLog(AbstractAction.class);

    // status params
    private String error = null;
    private String warning = null;
    private String success = null;

    private String viewerId;

    // page title
    private String pageTitle = null;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = getText(error);
    }

    public void setError(String error, String param) {
        this.error = getText(error, error, param);
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = getText(warning);
    }

    public void setWarning(String warning, String param) {
        this.warning = getText(warning, warning, param);
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String message) {
        this.success = getText(message);
    }

    public void setSuccess(String message, String param) {
        this.success = getText(message, message, param);
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public void prepare() throws Exception {
    }

    public String getProp(String key) {
        String value = Config.getProperty(key);
        return (value == null) ? key : value;
    }

    public boolean getBooleanProp(String key) {
        String value = Config.getProperty(key);
        return (value == null) ? false : (new Boolean(value)).booleanValue();
    }

    public int getIntProp(String key) {
        String value = Config.getProperty(key);
        return (value == null) ? 0 : (new Integer(value)).intValue();
    }

    /**
     * Sets the current authenticated user.
     * @param viewerId the userId of the authenticated user.
     */
    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    /**
     * Gets the current authenticated user.
     * @return the userId of the current authenticated user.
     */
    public String getViewerId() {
        return viewerId;
    }

    /**
     * Returns true if the current viewer should be offered a chance to create
     * a profile for themself.  Currently, this means that the viewer must not
     * already have a profile and profile auto-creation must be disabled.
     * @return true if the current viewer should be offered a chance to create a
     *  profile for themself; otherwise false.
     */
    public boolean getShouldOfferProfileCreation() {
        boolean autoCreationEnabled = Config.getBooleanProperty("socialsite.profile.autocreation.enabled");
        return ((viewerId == null) && (autoCreationEnabled == false));
    }

    public void addError(String errorKey) {
        addActionError(getText(errorKey));
    }

    public void addError(String errorKey, String param) {
        addActionError(getText(errorKey, errorKey, param));
    }

    public void addError(String errorKey, List args) {
        addActionError(getText(errorKey, args));
    }

    public void addMessage(String msgKey) {
        addActionMessage(getText(msgKey));
    }

    public void addMessage(String msgKey, String param) {
        addActionMessage(getText(msgKey, msgKey, param));
    }

    public void addMessage(String msgKey, List args) {
        addActionMessage(getText(msgKey, args));
    }

}
