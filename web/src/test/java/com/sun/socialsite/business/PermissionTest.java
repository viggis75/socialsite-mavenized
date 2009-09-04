/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://socialsite.dev.java.net/legal/CDDL+GPL.html
 * or legal/LICENSE.txt.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at legal/LICENSE.txt.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided by Sun
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.socialsite.business;

import com.sun.socialsite.SocialSiteException;
import com.sun.socialsite.Utils;
import com.sun.socialsite.config.Config;
import com.sun.socialsite.pojos.App;
import com.sun.socialsite.pojos.PermissionGrant;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.security.FeaturePermission;
import com.sun.socialsite.util.JSONWrapper;
import com.sun.socialsite.web.rest.opensocial.ConsumerContext;
import com.sun.socialsite.web.rest.opensocial.SocialSiteTokenBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.Permission;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.auth.SecurityToken;
import org.json.JSONObject;


/**
 * Test Permission-related business operations.
 */
public class PermissionTest extends TestCase {

    public static Log log = LogFactory.getLog(PermissionTest.class);


    public void setUp() throws Exception {
        Utils.setupSocialSite();

    }


    public static Test suite() {
        return new TestSuite(PermissionTest.class);
    }


    public void tearDown() throws Exception {
    }


    public void testAppPermissions() throws Exception {

        log.info("BEGIN");

        ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
        assertNotNull(profileManager);

        PermissionManager permissionManager = Factory.getSocialSite().getPermissionManager();
        assertNotNull(permissionManager);

        SocialSiteTokenBuilder tokenBuilder = new SocialSiteTokenBuilder();
        assertNotNull(tokenBuilder);

        // Create a Profile (for later use)
        Profile tom = new Profile();
        tom.setUserId("tom");
        tom.setFirstName("Thomas");
        tom.setMiddleName("D.");
        tom.setLastName("Cat");
        tom.setPrimaryEmail("thomas.d.cat@cartoons.com");
        profileManager.saveProfile(tom);
        Utils.endSession(true);

        // Create another Profile (for later use)
        Profile jerry = new Profile();
        jerry.setUserId("jerry");
        jerry.setFirstName("Jerry");
        jerry.setMiddleName("D.");
        jerry.setLastName("Mouse");
        jerry.setPrimaryEmail("jerry.d.mouse@cartoons.com");
        profileManager.saveProfile(jerry);
        Utils.endSession(true);

        // Create a ConsumerContext with these profiles
        JSONObject json = new JSONObject();
        JSONWrapper wrapper = new JSONWrapper(json);
        wrapper.put("assertions.owner.id", tom.getUserId());
        wrapper.put("assertions.viewer.id", jerry.getUserId());
        ConsumerContext consumerContext = new ConsumerContext(null, json);

        // Find one trusted and one untrusted App (for later use)
        App trustedApp = getApp(true);
        App untrustedApp = getApp(false);

        assertNotNull(trustedApp);
        assertNotNull(untrustedApp);

        List<PermissionGrant> grantsForTrustedApp = permissionManager.getPermissionGrants(trustedApp, 0, -1);
        assertTrue("grantsForTrustedApp.size() should be greater than 0", (grantsForTrustedApp.size() > 0));

        List<PermissionGrant> grantsForUntrustedApp = permissionManager.getPermissionGrants(untrustedApp, 0, -1);
        assertEquals(0, grantsForUntrustedApp.size());

        Permission requiredPermission = new FeaturePermission("socialsite-0.1");

        SecurityToken trustedToken = tokenBuilder.buildAppToken(consumerContext, trustedApp.getId(), -1L);
        SecurityException unexpectedException = checkPerm(permissionManager, requiredPermission, trustedToken);
        assertNull("checkPermission should succeed for trusted token", unexpectedException);

        SecurityToken untrustedToken = tokenBuilder.buildAppToken(consumerContext, untrustedApp.getId(), -1L);
        SecurityException expectedException = checkPerm(permissionManager, requiredPermission, untrustedToken);
        assertNotNull("checkPermission should fail for untrusted token", expectedException);

        // remove profiles
        profileManager.removeProfile(profileManager.getProfileByUserId("tom"));
        profileManager.removeProfile(profileManager.getProfileByUserId("jerry"));
        Utils.endSession(true);

        // assert that removals worked
        assertNull(profileManager.getProfileByUserId("tom"));
        assertNull(profileManager.getProfileByUserId("jerry"));

        log.info("END");

    }


    /**
     * Gets an App.  If <code>trusted</code> is true, the app will have whatever
     * permissions are granted to SocialSite's "core" gadgets.  If it is false,
     * the app will have no permissions.
     */
    private App getApp(boolean trusted) throws Exception {
        AppManager appManager = Factory.getSocialSite().getAppManager();
        App result = null;
        String preLoadDirectoryPath = Config.getProperty("socialsite.gadgets.preload.path");
        String baseUrl = Config.getProperty("socialsite.gadgets.preload.base.url");
        if (preLoadDirectoryPath != null) {
            File preLoadDirectory = new File(preLoadDirectoryPath);
            File[] files = preLoadDirectory.listFiles();
            for (int i = 0; i < files.length; i++) {
                if ((!files[i].isDirectory()) && (files[i].toString().endsWith(".xml"))) {
                    if (trusted) {
                        URL url = new URL(baseUrl + "/" + files[i].getName());
                        result = appManager.getAppByURL(url);
                        break;
                    } else {
                        URL url = new URL("http://untrusted.fakeserver.com/" + files[i].getName());
                        result = App.readFromStream(new FileInputStream(files[i]), url);
                        appManager.saveApp(result);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Convenience method to determine whether <code>PermissionManager.checkPermission</code> throws
     * an exception (without requiring your own try/catch block).
     */
    private SecurityException checkPerm(PermissionManager pm, Permission p, SecurityToken t) throws SocialSiteException {
        try {
            pm.checkPermission(p, t);
            return null;
        } catch (SecurityException e) {
            return e;
        }
    }

}
