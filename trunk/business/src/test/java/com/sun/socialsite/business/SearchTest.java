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

import com.sun.socialsite.TestUtils;
import com.sun.socialsite.pojos.App;
import com.sun.socialsite.pojos.Group;
import com.sun.socialsite.pojos.Profile;
import java.net.URL;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test Search-related business operations.
 */
public class SearchTest extends TestCase {

    public static Log log = LogFactory.getLog(SearchTest.class);


    public void setUp() throws Exception {
        TestUtils.setupSocialSite();
    }


    public static Test suite() {
        return new TestSuite(SearchTest.class);
    }


    public void tearDown() throws Exception {
    }


    public void testProfileSearch() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
            SearchManager searchManager = Factory.getSocialSite().getSearchManager();

            Profile profile1 = new Profile();
            profile1.setUserId("john");
            profile1.setFirstName("Jonathan");
            profile1.setMiddleName("Franklin");
            profile1.setLastName("Doe");
            profile1.setPrimaryEmail("john.f.doe@example.com");
            profileManager.saveProfile(profile1);
            TestUtils.endSession(true);
            assertNotNull(profileManager.getProfileByUserId("john"));

            Profile profile2 = new Profile();
            profile2.setUserId("jane");
            profile2.setFirstName("Jane");
            profile2.setMiddleName("Ellen");
            profile2.setLastName("Doe");
            profile2.setPrimaryEmail("jane.e.doe@example.com");
            profileManager.saveProfile(profile2);
            TestUtils.endSession(true);
            assertNotNull(profileManager.getProfileByUserId("jane"));

            int totalResults1 = searchManager.getTotalProfiles("john");
            List<Profile> results1 = searchManager.getProfiles(profileManager, 0, -1, "john");
            assertEquals(1, totalResults1);
            assertEquals(1, results1.size());
            assertTrue(results1.contains(profile1));
            assertFalse(results1.contains(profile2));

            int totalResults2 = searchManager.getTotalProfiles("Jane");
            List<Profile> results2 = searchManager.getProfiles(profileManager, 0, -1, "Jane");
            assertEquals(1, totalResults2);
            assertEquals(1, results2.size());
            assertFalse(results2.contains(profile1));
            assertTrue(results2.contains(profile2));

            int totalResults3 = searchManager.getTotalProfiles("Franklin");
            List<Profile> results3 = searchManager.getProfiles(profileManager, 0, -1, "Franklin");
            assertEquals(1, totalResults3);
            assertEquals(1, results3.size());
            assertTrue(results3.contains(profile1));
            assertFalse(results3.contains(profile2));

            int totalResults4 = searchManager.getTotalProfiles("Doe");
            List<Profile> results4 = searchManager.getProfiles(profileManager, 0, -1, "Doe");
            assertEquals(2, totalResults4);
            assertEquals(2, results4.size());
            assertTrue(results4.contains(profile1));
            assertTrue(results4.contains(profile2));

            int totalResults5 = searchManager.getTotalProfiles("Smith");
            List<Profile> results5 = searchManager.getProfiles(profileManager, 0, -1, "Smith");
            assertEquals(0, totalResults5);
            assertEquals(0, results5.size());
            assertFalse(results5.contains(profile1));
            assertFalse(results5.contains(profile2));

            profileManager.removeProfile((profileManager.getProfileByUserId("john")));
            TestUtils.endSession(true);

            profileManager.removeProfile((profileManager.getProfileByUserId("jane")));
            TestUtils.endSession(true);

            assertNull(profileManager.getProfileByUserId("john"));
            assertNull(profileManager.getProfileByUserId("jane"));

            // Now that profiles are removed, make sure search results are empty
            int totalResults6 = searchManager.getTotalProfiles("Doe");
            List<Profile> results6 = searchManager.getProfiles(profileManager, 0, -1, "Doe");
            assertEquals(0, totalResults6);
            assertEquals(0, results6.size());
            assertFalse(results6.contains(profile1));
            assertFalse(results6.contains(profile2));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        } finally {
            // cleanup here in case an assertion failed
            tearDownPersonIgnoreException("john");
            tearDownPersonIgnoreException("jane");
        }

        log.info("END");

    }


    public void testAppSearch() throws Exception {

        log.info("BEGIN");

        try {

            AppManager appManager = Factory.getSocialSite().getAppManager();
            SearchManager searchManager = Factory.getSocialSite().getSearchManager();

            // create and save app
            URL app1URL = new URL("http://example.com/xyzApp1.xml");
            App app1 = new App();
            app1.setURL(app1URL);
            app1.setTitle("XYZ App #123");
            app1.setDescription("XYZ App #123");
            appManager.saveApp(app1);
            TestUtils.endSession(true);
            assertNotNull(appManager.getAppByURL(app1URL));

            // create and save another app
            URL app2URL = new URL("http://example.com/abcApp1.xml");
            App app2 = new App();
            app2.setURL(app2URL);
            app2.setTitle("ABC App #123");
            app2.setDescription("ABC App #123");
            appManager.saveApp(app2);
            TestUtils.endSession(true);
            assertNotNull(appManager.getAppByURL(app2URL));

            int totalResults1 = searchManager.getTotalApps("XYZ");
            List<App> results1 = searchManager.getApps(appManager, 0, -1, "XYZ");
            assertEquals(1, totalResults1);
            assertEquals(1, results1.size());
            assertTrue(results1.contains(app1));
            assertFalse(results1.contains(app2));

            int totalResults2 = searchManager.getTotalApps("ABC");
            List<App> results2 = searchManager.getApps(appManager, 0, -1, "ABC");
            assertEquals(1, totalResults2);
            assertEquals(1, results2.size());
            assertFalse(results2.contains(app1));
            assertTrue(results2.contains(app2));

            appManager.removeApp((appManager.getAppByURL(app1URL)));
            TestUtils.endSession(true);

            appManager.removeApp((appManager.getAppByURL(app2URL)));
            TestUtils.endSession(true);

            assertNull(appManager.getAppByURL(app1URL));
            assertNull(appManager.getAppByURL(app2URL));

            // Now that apps are removed, make sure search results are empty
            int totalResults3 = searchManager.getTotalApps("123");
            List<App> results3 = searchManager.getApps(appManager, 0, -1, "123");
            assertEquals(0, totalResults3);
            assertEquals(0, results3.size());
            assertFalse(results3.contains(app1));
            assertFalse(results3.contains(app2));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }


    public void testGroupSearch() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
            GroupManager groupManager = Factory.getSocialSite().getGroupManager();
            SearchManager searchManager = Factory.getSocialSite().getSearchManager();

            Profile profile1 = new Profile();
            profile1.setUserId("john");
            profile1.setFirstName("Jonathan");
            profile1.setMiddleName("Franklin");
            profile1.setLastName("Doe");
            profile1.setPrimaryEmail("john.f.doe@example.com");
            profileManager.saveProfile(profile1);
            TestUtils.endSession(true);
            assertNotNull(profileManager.getProfileByUserId("john"));

            Group group1 = new Group();
            group1.setName("johnsFirstGroupName");
            group1.setHandle("johnsFirstGroupHandle");
            group1.setDescription("commonDescription");
            profile1 = profileManager.getProfile(profile1.getId());
            groupManager.createGroup(group1, profile1);
            TestUtils.endSession(true);
            assertNotNull(groupManager.getGroupByHandle("johnsFirstGroupHandle"));

            Group group2 = new Group();
            group2.setName("johnsSecondGroupName");
            group2.setHandle("johnsSecondGroupHandle");
            group2.setDescription("commonDescription");
            profile1 = profileManager.getProfile(profile1.getId());
            groupManager.createGroup(group2, profile1);
            TestUtils.endSession(true);
            assertNotNull(groupManager.getGroupByHandle("johnsSecondGroupHandle"));

            int totalResults1 = searchManager.getTotalGroups("johnsFirstGroupName");
            List<Group> results1 = searchManager.getGroups(groupManager, 0, -1, "johnsFirstGroupName");
            assertEquals(1, totalResults1);
            assertEquals(1, results1.size());
            assertTrue(results1.contains(group1));
            assertFalse(results1.contains(group2));

            int totalResults2 = searchManager.getTotalGroups("johnsSecondGroupHandle");
            List<Group> results2 = searchManager.getGroups(groupManager, 0, -1, "johnsSecondGroupHandle");
            assertEquals(1, totalResults2);
            assertEquals(1, results2.size());
            assertFalse(results2.contains(group1));
            assertTrue(results2.contains(group2));

            int totalResults3 = searchManager.getTotalGroups("commonDescription");
            List<Group> results3 = searchManager.getGroups(groupManager, 0, -1, "commonDescription");
            assertEquals(2, totalResults3);
            assertEquals(2, results3.size());
            assertTrue(results3.contains(group1));
            assertTrue(results3.contains(group2));

            int totalResults4 = searchManager.getTotalGroups("Smith");
            List<Group> results4 = searchManager.getGroups(groupManager, 0, -1, "Smith");
            assertEquals(0, totalResults4);
            assertEquals(0, results4.size());
            assertFalse(results4.contains(group1));
            assertFalse(results4.contains(group2));

            groupManager.removeGroup(groupManager.getGroupByHandle("johnsFirstGroupHandle"));
            TestUtils.endSession(true);

            groupManager.removeGroup(groupManager.getGroupByHandle("johnsSecondGroupHandle"));
            TestUtils.endSession(true);

            profileManager.removeProfile((profileManager.getProfileByUserId("john")));
            TestUtils.endSession(true);

            assertNull(profileManager.getProfileByUserId("john"));
            assertNull(groupManager.getGroupByHandle("johnsFirstGroupHandle"));
            assertNull(groupManager.getGroupByHandle("johnsSecondGroupHandle"));

            // Now that groups are removed, make sure search results are empty
            int totalResults5 = searchManager.getTotalGroups("commonDescription");
            List<Group> results5 = searchManager.getGroups(groupManager, 0, -1, "commonDescription");
            assertEquals(0, totalResults5);
            assertEquals(0, results5.size());
            assertFalse(results5.contains(group1));
            assertFalse(results5.contains(group2));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        } finally {
            // cleanup here in case an assertion failed
            tearDownPersonIgnoreException("john");
            tearDownGroupIgnoreException("johnsFirstGroupHandle");
            tearDownGroupIgnoreException("johnsSecondGroupHandle");
        }

        log.info("END");

    }

    // could move to tearDown() method except tests use different users
    private void tearDownPersonIgnoreException(String userId) {
        try {
            if (Factory.getSocialSite().getProfileManager().getProfileByUserId(
                userId) != null) {
                TestUtils.teardownPerson(userId);
            }
        } catch (Exception e) {
            log.debug("Exception ignored while cleaning up person: " +
                e.getMessage());
        }
    }

    // could move to tearDown() method except tests use different groups
    private void tearDownGroupIgnoreException(String handle) {
        try {
            if (Factory.getSocialSite().getGroupManager().getGroupByHandle(
                handle) != null) {
                TestUtils.teardownGroup(handle);
            }
        } catch (Exception e) {
            log.debug("Exception ignored while cleaning up group: " +
                e.getMessage());
        }
    }

}
