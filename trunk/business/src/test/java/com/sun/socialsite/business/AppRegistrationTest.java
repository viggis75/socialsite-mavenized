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

import com.sun.socialsite.Utils;
import com.sun.socialsite.pojos.AppRegistration;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.userapi.User;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.social.opensocial.oauth.OAuthEntry;


/**
 * Test App-related business operations.
 */
public class AppRegistrationTest extends TestCase {
    public static Log log = LogFactory.getLog(AppRegistrationTest.class);

    public void setUp() throws Exception {
        Utils.setupSocialSite();
    }

    public static Test suite() {
        return new TestSuite(AppRegistrationTest.class);
    }

    public void tearDown() throws Exception {
    }

    /*
     * Test application registration process, end-to-end
     */
    public void testAppRegistration() throws Exception {
        log.info("BEGIN");

        // first, we need a developer
        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        assertNotNull(pmgr);
        User devDude = Utils.setupUser("devdude");
        Profile hisProfile = Utils.setupProfile(devDude, "Dev", "Dude");
        pmgr.saveProfile(hisProfile);
        Utils.endSession(true);

        // and we need an app
        String appurl = "file:./web/local_gadgets/face.xml";

        try {
            // we'll be testing the app manager today
            AppManager amgr = Factory.getSocialSite().getAppManager();

            // assert that no registrations exist
            List<AppRegistration> pending = amgr.getAppRegistrations(null, "PENDING");
            assertEquals(0, pending.size());

            List<AppRegistration> all = amgr.getAppRegistrations(null, null);
            assertEquals(0, all.size());

            List<AppRegistration> approved = amgr.getAppRegistrations(null, "APPROVED");
            assertEquals(0, approved.size());

            // developer registers app
            amgr.registerApp(hisProfile.getId(), appurl, null);
            Utils.endSession(true);

            // admin gets list of pending apps
            pending = amgr.getAppRegistrations(hisProfile.getId(), "PENDING");
            assertEquals(1, pending.size());

            all = amgr.getAppRegistrations(hisProfile.getId(), null);
            assertEquals(1, all.size());

            approved = amgr.getAppRegistrations(hisProfile.getId(), "APPROVED");
            assertEquals(0, approved.size());

            // admin approves app
            AppRegistration reg = pending.get(0);
            amgr.approveAppRegistration(reg.getId(), "approved, yay!");
            Utils.endSession(true);

            // assert that app is approved
            pending = amgr.getAppRegistrations(hisProfile.getId(), "PENDING");
            assertEquals(0, pending.size());

            all = amgr.getAppRegistrations(hisProfile.getId(), null);
            assertEquals(1, all.size());

            approved = amgr.getAppRegistrations(hisProfile.getId(), "APPROVED");
            assertEquals(1, approved.size());

            // remove app and asset that app is gone daddy gone
            amgr.rejectAppRegistration(reg.getId(), "rejected, sorry!");
            Utils.endSession(true);

            pending = amgr.getAppRegistrations(hisProfile.getId(), "PENDING");
            assertEquals(0, pending.size());

            all = amgr.getAppRegistrations(hisProfile.getId(), "APPROVED");
            assertEquals(0, all.size());

            approved = amgr.getAppRegistrations(hisProfile.getId(), "APPROVED");
            assertEquals(0, approved.size());

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
            
        } finally {
            Utils.teardownUser(devDude.getUserId());
            Utils.endSession(true);
        }

        log.info("END");

    }

    public void testOAuthEntryCRUD() throws Exception {

        try {

            // Create
            OAuthEntry entry = new OAuthEntry();
            entry.token = "token1";
            entry.appId = "br549";

            AppManager amgr = Factory.getSocialSite().getAppManager();
            amgr.saveOAuthEntry(entry);
            Utils.endSession(true);

            // Retrieve
            OAuthEntry fetched = amgr.getOAuthEntry(entry.token);
            assertEquals(entry.appId, fetched.appId);

            // Updated
            fetched.appId = "8675309";
            amgr.saveOAuthEntry(fetched);
            Utils.endSession(true);

            OAuthEntry refetched = amgr.getOAuthEntry(entry.token);
            assertEquals(fetched.appId, refetched.appId);

            // Delete
            amgr.removeOAuthEntry(entry);
            Utils.endSession(true);

            assertNull(amgr.getOAuthEntry(entry.token));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;

        } finally {
            Utils.endSession(false);
        }

        log.info("END");
    }
}
