package com.samplesocialsiteapplication.util;

import com.sun.socialsite.SocialSiteException;
import com.sun.socialsite.business.EmfProvider;
import com.sun.socialsite.userapi.UserManager;
import com.sun.socialsite.userapi.UserManagerImpl;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author nic
 */
public class UserManagerProvider {
private EntityManager em;

    private UserManager userManager;

    /**
     * Constructs a new <code>UserManagerProvider</code>.
     */
    public UserManagerProvider() throws SocialSiteException {
        EntityManagerFactory emf = EmfProvider.getEmf();
         em= emf.createEntityManager();
        userManager = new UserManagerImpl(em);
    }

    /**
     * Gets the <code>UserManager</code> associated with this provider.
     */
    public UserManager getUserManager() {
        return userManager;
    }

    /**
     * Flush object states.
     */
    public void flush() {
        em.getTransaction().begin();
        em.getTransaction().commit();
    }

    /**
     * Closes any resources held by this object.
     */
    public void close() {
        em.close();
    }

}
