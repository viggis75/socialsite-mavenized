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

import com.sun.socialsite.userapi.User;
import com.sun.socialsite.Utils;
import com.sun.socialsite.pojos.SocialSiteActivity;
import com.sun.socialsite.pojos.Profile;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test Activity-related business operations.
 */
public class SocialSiteActivityTest extends TestCase {

    public static Log log = LogFactory.getLog(SocialSiteActivityTest.class);


    public void setUp() throws Exception {
        Utils.setupSocialSite();

    }


    public static Test suite() {
        return new TestSuite(SocialSiteActivityTest.class);
    }


    public void tearDown() throws Exception {
    }


    public void testSocialSiteActivityCRUD() throws Exception {

        log.info("BEGIN");

        try {

            SocialSiteActivityManager mgr = Factory.getSocialSite().getSocialSiteActivityManager();
            assertNotNull(mgr);

            User testUser = Utils.setupUser("activityTestUser");
            Utils.endSession(true);

            ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
            Profile profile = Utils.setupProfile(testUser, "Test", "User");
            pmgr.saveProfile(profile);
            Utils.endSession(true);

            SocialSiteActivity activity = new SocialSiteActivity();
            activity.setTitle("I'm testing right now");
            activity.setType(SocialSiteActivity.STATUS);
            activity.setProfile(profile);
            mgr.saveActivity(activity);
            Utils.endSession(true);

            assertNotNull(mgr.getActivity(activity.getId()));

            assertEquals(1, mgr.getUserActivities(profile, 0, 10).size());

            SocialSiteActivity fetched = mgr.getUserActivities(profile, 0, 10).get(0);
            assertNotNull(fetched.getProfile());

            mgr.removeActivity(mgr.getActivity(activity.getId()));
            Utils.endSession(true);

            pmgr.removeProfile(pmgr.getProfile(profile.getId()));
            Utils.endSession(true);

            Utils.teardownUser(testUser.getUserId());
            Utils.endSession(true);

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }

}
