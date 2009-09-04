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
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.pojos.ProfileProperty;
import java.util.Date;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test Profile-related business operations.
 */
public class ProfileManagerTest extends TestCase {

    public static Log log = LogFactory.getLog(ProfileManagerTest.class);


    public void setUp() throws Exception {
        Utils.setupSocialSite();
    }


    public static Test suite() {
        return new TestSuite(ProfileManagerTest.class);
    }


    public void tearDown() throws Exception {
    }


    public void testProfileCRUD() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager mgr = Factory.getSocialSite().getProfileManager();

            Profile profile1 = new Profile();
            profile1.setUserId("snoopdave1");
            profile1.setFirstName("David");
            profile1.setMiddleName("Mason");
            profile1.setLastName("Johnson");
            profile1.setPrimaryEmail("davidm.johnson@sun.com");
            mgr.saveProfile(profile1);
            Utils.endSession(true);
            assertNotNull(mgr.getProfileByUserId("snoopdave"));

            // Now make an update to snoopdave's profile
            Profile profile1a = mgr.getProfileByUserId("snoopdave1");
            assertNotNull(profile1a);
            Date d1 = profile1a.getUpdated();
            profile1a.setSurtitle("Jr");
            mgr.saveProfile(profile1a);
            Utils.endSession(true);

            // Then read back the result, and make sure the "updated" timestamp has progressed
            Profile profile1b = mgr.getProfileByUserId("snoopdave1");
            assertNotNull(profile1b);
            Date d2 = profile1b.getUpdated();
            assertTrue("d2 should be greater than d1", (d2.getTime() > d1.getTime()));
            Utils.endSession(true);

            Profile profile2 = new Profile();
            profile2.setUserId("otherguy");
            profile2.setFirstName("Other");
            profile2.setLastName("Guy");
            profile2.setPrimaryEmail("other.guy@someplace.com");
            mgr.saveProfile(profile2);
            Utils.endSession(true);
            assertNotNull(mgr.getProfileByUserId("otherguy"));

            List<Profile> results1 = mgr.getOldestProfiles(0, -1);
            assertEquals(2, results1.size());
            assertEquals(profile1, results1.get(0));
            assertEquals(profile2, results1.get(1));

            List<Profile> results2 = mgr.getMostRecentlyUpdatedProfiles(0, -1);
            assertEquals(2, results2.size());
            assertEquals(profile2, results2.get(0));
            assertEquals(profile1, results2.get(1));

            mgr.removeProfile(mgr.getProfileByUserId("snoopdave1"));
            Utils.endSession(true);

            mgr.removeProfile(mgr.getProfileByUserId("otherguy"));
            Utils.endSession(true);

            assertNull(mgr.getProfileByUserId("snoopdave1"));
            assertNull(mgr.getProfileByUserId("otherguy"));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }


    public void testProfilePropsCRUD() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager mgr = Factory.getSocialSite().getProfileManager();

            // create profile and save it
            Profile profile1 = new Profile();
            profile1.setUserId("snoopdave");
            profile1.setFirstName("David");
            profile1.setMiddleName("Mason");
            profile1.setLastName("Johnson");
            profile1.setPrimaryEmail("davidm.johnson@sun.com");
            mgr.saveProfile(profile1);
            Utils.endSession(true);

            // assert that profile now exists
            assertNotNull(mgr.getProfileByUserId("snoopdave"));

            // add a property to profile
            Profile profile2 = mgr.getProfileByUserId("snoopdave");
            ProfileProperty prop1 = new ProfileProperty();
            prop1.setName("setting1");
            prop1.setNameKey("setting1.key");
            prop1.setValue("value1");
            prop1.setVisibility(Profile.VisibilityType.PRIVATE);
            profile2.addProfileProp(prop1);
            Utils.endSession(true);

            // assert that property now exists
            Profile profile3 = mgr.getProfileByUserId("snoopdave");
            assertNotNull(profile3.getProperty("setting1"));
            assertEquals("value1", profile3.getProperty("setting1").getValue());

            // remove property
            ProfileProperty prop2 = profile3.getProperty("setting1");
            profile3.removeProperty(prop2);
            mgr.removeProfileProperty(prop2);
            Utils.endSession(true);

            // assert that property is gone
            Profile profile4 = mgr.getProfileByUserId("snoopdave");
            assertNull(profile4.getProperty("setting1"));

            // remove profile
            mgr.removeProfile(mgr.getProfileByUserId("snoopdave"));
            Utils.endSession(true);

            // assert that profile is gone
            assertNull(mgr.getProfileByUserId("snoopdave"));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }

}
