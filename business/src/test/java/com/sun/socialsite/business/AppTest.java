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
import com.sun.socialsite.config.Config;
import com.sun.socialsite.pojos.App;
import com.sun.socialsite.pojos.AppInstance;
import com.sun.socialsite.pojos.Profile;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.gadgets.spec.GadgetSpec;


/**
 * Test App-related business operations.
 */
public class AppTest extends TestCase {

    public static Log log = LogFactory.getLog(AppTest.class);


    public void setUp() throws Exception {
        TestUtils.setupSocialSite();

    }


    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void tearDown() throws Exception {
    }


    public void testAppCRUD() throws Exception {

        log.info("BEGIN");

        try {

            AppManager appManager = Factory.getSocialSite().getAppManager();
            assertNotNull(appManager);

            // Make sure that apps were preloaded as expected
            String preLoadDirectoryPath = Config.getProperty("socialsite.apps.preload.path");
            String baseUrl = Config.getProperty("socialsite.apps.preload.base.url");

            if (preLoadDirectoryPath != null) {

                File preLoadDirectory = new File(preLoadDirectoryPath);
                File[] files = preLoadDirectory.listFiles();

                for (int i = 0; i < files.length; i++) {

                    if ((!files[i].isDirectory()) && (files[i].toString().endsWith(".xml"))) {

                        URL url = new URL(baseUrl + "/" + files[i].getName());
                        App app = appManager.getAppByURL(url);
                        assertNotNull(String.format("getAppByURL(%s) should not be null", url), app);
                        assertEquals(app.getURL(), url);

                        // When we're running unit tests, there is likely to be no
                        // webserver listening at the URL specified above.  So we'll
                        // use a filesystem-based URL for GadgetSpec testing.
                        URL fileURL = files[i].toURI().toURL();
                        GadgetSpec spec = appManager.getGadgetSpecByURL(fileURL);
                        assertNotNull(spec);
                        assertEquals(spec.getUrl().toJavaUri().toURL(), fileURL);

                    }

                }

            }

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }


    public void testAppDataCRUD() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
            assertNotNull(profileManager);

            AppManager appManager = Factory.getSocialSite().getAppManager();
            assertNotNull(appManager);

            // Create a Profile (for later use)
            Profile profile = new Profile();
            profile.setUserId("john1");
            profile.setFirstName("Jonathan");
            profile.setMiddleName("Franklin");
            profile.setLastName("Doe");
            profile.setPrimaryEmail("john.f.doe@example.com");
            profileManager.saveProfile(profile);
            TestUtils.endSession(true);

            // create and save app
            URL appURL = new URL("http://example.com/app.xml");
            App app = new App();
            app.setURL(appURL);
            app.setTitle("Test App #1");
            appManager.saveApp(app);
            TestUtils.endSession(true);

            // verify it exists as created
            app = appManager.getAppByURL(appURL);
            assertNotNull(app);
            assertEquals(new URL("http://example.com/app.xml"), app.getURL());
            assertEquals("Test App #1", app.getTitle());

            appManager.setAppData(app, profile, "marsupial", "wombat");
            appManager.setAppData(app, profile, "smokestack", "lightning");
            TestUtils.endSession(true);

            // get app, verify properties
            app = appManager.getAppByURL(appURL);
            profile = profileManager.getProfileByUserId("john1");
            assertEquals(2, appManager.getAppData(app, profile).size());

            profileManager.removeProfile(profileManager.getProfileByUserId("john1"));
            TestUtils.endSession(true);

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }


    public void testAppInstanceCRUD() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
            assertNotNull(profileManager);

            AppManager appManager = Factory.getSocialSite().getAppManager();
            assertNotNull(appManager);

            // Create a Profile (for later use)
            Profile profile = new Profile();
            profile.setUserId("john2");
            profile.setFirstName("Jonathan");
            profile.setMiddleName("Franklin");
            profile.setLastName("Doe");
            profile.setPrimaryEmail("john.f.doe@example.com");
            profileManager.saveProfile(profile);
            TestUtils.endSession(true);

            // Find an existing App (for later use)
            List<App> apps = appManager.getApps(0, 1);
            assertEquals(1, apps.size());
            App app = apps.iterator().next();
            assertNotNull(app);

            // Add an AppInstance
            Profile storedProfile = profileManager.getProfileByUserId("john2");
            assertNotNull(storedProfile);
            assertEquals(0, storedProfile.getAppInstances().size());
            List<AppInstance> appInstances = new ArrayList<AppInstance>();
            AppInstance appInstance = new AppInstance();
            appInstance.setProfile(storedProfile);
            appInstance.setApp(app);
            appInstances.add(appInstance);
            storedProfile.setAppInstances(appInstances);
            profileManager.saveProfile(storedProfile);
            TestUtils.endSession(true);

            // And make sure it's stored
            Profile storedProfile2 = profileManager.getProfileByUserId("john2");
            assertNotNull(storedProfile2);
            assertEquals(1, storedProfile2.getAppInstances().size());
            AppInstance storedAppInstance = storedProfile2.getAppInstances().iterator().next();
            assertEquals(storedAppInstance, appInstance);
            TestUtils.endSession(true);

            // Remove apps
            Profile storedProfile3 = profileManager.getProfileByUserId("john2");
            assertNotNull(storedProfile3);
            storedProfile3.setAppInstances(new ArrayList<AppInstance>());
            TestUtils.endSession(true);

            // And make sure they're gone
            Profile storedProfile4 = profileManager.getProfileByUserId("john2");
            assertNotNull(storedProfile4);
            assertEquals(0, storedProfile4.getAppInstances().size());
            TestUtils.endSession(true);

            profileManager.removeProfile(profileManager.getProfileByUserId("john2"));
            TestUtils.endSession(true);

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }

}
