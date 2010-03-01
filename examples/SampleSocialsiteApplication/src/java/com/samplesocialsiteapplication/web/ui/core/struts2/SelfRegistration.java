
package com.samplesocialsiteapplication.web.ui.core.struts2;


import com.sun.socialsite.SocialSiteException;
import com.sun.socialsite.userapi.UserManager;
import com.sun.socialsite.userapi.UserManagementException;
import com.samplesocialsiteapplication.util.UserManagerProvider;
import com.sun.socialsite.userapi.User;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class SelfRegistration extends AbstractAction {

    private static Log log = LogFactory.getLog(SelfRegistration.class);

    private User newUser = new User();

    public SelfRegistration() {
    }

    @Override
    public String execute() {
        return INPUT;
    }

    @Override
    public void prepare() {
        setPageTitle("SelfRegistration.pageTitle");
    }

    public String save() {
        try {
            createUser(newUser);
        } catch (Exception ex) {
            this.setError("SelfRegistration.error", ex.getLocalizedMessage());
            log.error("Unexpected Exception", ex);
            return INPUT;
        }
        addMessage("SelfRegistration.saved");
        return SUCCESS;
    }

    public User getNewUser() {
        return newUser;
    }

    private void createUser(User newUser) throws SocialSiteException, UserManagementException {

        newUser.setUserName(newUser.getUserId());
        newUser.resetPassword(newUser.getPassword(), "SHA");
        Date currDate = new Date();
        newUser.setCreationDate(currDate);
        newUser.setAccessDate(currDate);
        newUser.setUpdateDate(currDate);
        newUser.setEnabled(true);

        UserManagerProvider provider = new UserManagerProvider();
        UserManager uMgr = provider.getUserManager();
        uMgr.registerUser(newUser);
        uMgr.grantRole("user", newUser);
        uMgr.saveUser(newUser);
        provider.flush();
        provider.close();

    }

}
