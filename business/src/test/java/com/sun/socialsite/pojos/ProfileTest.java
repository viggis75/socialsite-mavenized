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

package com.sun.socialsite.pojos;

import com.sun.socialsite.TestUtils;
import com.sun.socialsite.business.*;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Profile POJO implementation
 */
public class ProfileTest extends TestCase {

    private static Log log = LogFactory.getLog(ProfileTest.class);

    @Before
    public void setUp() throws Exception {
        TestUtils.setupSocialSite();
    }


    /**
     * Test updating profile via JSON. We're already testing simple Profile and
     * ProfileProperty CRUD in the ProfileManager test, so here we focus on
     * updating via JSON.
     */
    @Test
    public void testUpdate() {

        log.info("BEGIN");

        try {

            ProfileManager mgr = Factory.getSocialSite().getProfileManager();

            // create a profile
            Profile profile1 = new Profile();
            profile1.setUserId("steves");
            profile1.setFirstName("Steve");
            profile1.setMiddleName("Bertram");
            profile1.setLastName("Smith");
            profile1.setPrimaryEmail("steves@example.com");
            mgr.saveProfile(profile1);
            TestUtils.endSession(true);
            assertNotNull(mgr.getProfileByUserId("steves"));

            // test setting/getting a defined PROPERTY, should work ;-)
            {
                Profile profile = mgr.getProfileByUserId("steves");
                JSONObject updates = new JSONObject();
                updates.put("identification_nickname", "steves");
                profile.update(Profile.Format.FLAT, updates);
                mgr.saveProfile(profile);
                Factory.getSocialSite().flush();
                TestUtils.endSession(true);

                profile = mgr.getProfileByUserId("steves");
                assertEquals("steves",
                        profile.getProperty("identification_nickname").getValue());
                TestUtils.endSession(true);
            }

            // test setting/getting an UNDEFINED PROPERTY, should fail
            {
                Profile profile = mgr.getProfileByUserId("steves");
                JSONObject updates = new JSONObject();
                updates.put("monstertruckname", "gravedigger");
                profile.update(Profile.Format.FLAT, updates);
                mgr.saveProfile(profile);
                Factory.getSocialSite().flush();
                TestUtils.endSession(true);

                profile = mgr.getProfileByUserId("steves");
                assertNull(profile.getProperty("monstertruckname"));
                TestUtils.endSession(true);
            }

            // test setting/getting PROPERTY OBJECT properties
            {
                Profile profile = mgr.getProfileByUserId("steves");
                JSONObject updates = new JSONObject();
                updates.put("personal_profileSong_address",
                        "http://bodiddly.com/songs/roadrunner.mp3");
                profile.update(Profile.Format.FLAT, updates);
                mgr.saveProfile(profile);
                Factory.getSocialSite().flush();
                TestUtils.endSession(true);

                profile = mgr.getProfileByUserId("steves");
                assertEquals("http://bodiddly.com/songs/roadrunner.mp3",
                        profile.getProperty("personal_profileSong_address").getValue());
                TestUtils.endSession(true);
            }

            // test creating a PROPERTY OBJECT COLLECTION
            {
                Profile profile = mgr.getProfileByUserId("steves");
                JSONObject updates = new JSONObject();

                updates.put("experience_jobs_1_name", "ACME Industries, Inc.");
                updates.put("experience_jobs_1_webpage_address", "http://example-acme.com");

                updates.put("experience_jobs_2_name", "Virtual Corporation");
                updates.put("experience_jobs_2_webpage_address", "http://example-virtual.com");

                updates.put("experience_jobs_3_name", "Moon Microsystems, Inc.");
                updates.put("experience_jobs_3_webpage_address", "http://example-moon.com");

                updates.put("experience_jobs_4_name", "Whatsit, Inc.");
                updates.put("experience_jobs_4_webpage_address", "http://example-whatsit.com");

                profile.update(Profile.Format.FLAT, updates);
                mgr.saveProfile(profile);
                Factory.getSocialSite().flush();
                TestUtils.endSession(true);

                profile = mgr.getProfileByUserId("steves");
                assertEquals("Moon Microsystems, Inc.", profile.getProperty("experience_jobs_3_name").getValue());
                TestUtils.endSession(true);
            }

            // test deleting from middle of a PROPERTY OBJECT COLLECTION
            {
                // delete the 2nd object
                Profile profile = mgr.getProfileByUserId("steves");
                JSONObject updates = new JSONObject();

                // to delete an object, just set any one of its properties to zzz_DELETE_zzz
                updates.put("experience_jobs_2_name", "zzz_DELETE_zzz");
                profile.update(Profile.Format.FLAT, updates);
                mgr.saveProfile(profile);
                Factory.getSocialSite().flush();
                TestUtils.endSession(true);

                // and the 3rd object becomes the 2nd object
                profile = mgr.getProfileByUserId("steves");
                assertEquals("Moon Microsystems, Inc.", profile.getProperty("experience_jobs_2_name").getValue());
                assertEquals("http://example-moon.com", profile.getProperty("experience_jobs_2_webpage_address").getValue());
                TestUtils.endSession(true);
            }

            // test deleting from END of a PROPERTY OBJECT COLLECTION
            {
                // delete the 2nd object
                Profile profile = mgr.getProfileByUserId("steves");
                JSONObject updates = new JSONObject();

                // to delete an object, just set any one of its properties to zzz_DELETE_zzz
                updates.put("experience_jobs_4_name", "zzz_DELETE_zzz");
                profile.update(Profile.Format.FLAT, updates);
                mgr.saveProfile(profile);
                Factory.getSocialSite().flush();
                TestUtils.endSession(true);

                // and the 4th object is gone, including nested props
                profile = mgr.getProfileByUserId("steves");
                assertNull(profile.getProperty("experience_jobs_4_name"));
                assertNull(profile.getProperty("experience_jobs_4_webpage_address"));
            }

            // remove the profile
            mgr.removeProfile(mgr.getProfileByUserId("steves"));
            TestUtils.endSession(true);

        } catch (Exception e) {
            log.error("ERROR", e);
        }
        log.info("END");

    }


    /**
     * Test saving profile as OpenSocial Person model JSON format.
     */
    @Test
    public void testJSONHierarchy() {

        log.info("BEGIN");

        try {

            ProfileManager mgr = Factory.getSocialSite().getProfileManager();

            // create a profile
            Profile profile1 = new Profile();
            profile1.setUserId("steves2");
            profile1.setFirstName("Steve");
            profile1.setMiddleName("Bertram");
            profile1.setLastName("Smith");
            profile1.setPrimaryEmail("steves@example.com");
            mgr.saveProfile(profile1);
            TestUtils.endSession(true);
            assertNotNull(mgr.getProfileByUserId("steves2"));

            // add some properties
            {
                Profile profile = mgr.getProfileByUserId("steves2");
                JSONObject updates = new JSONObject();

                updates.put("personal_profileSong_address",
                    "http://bodiddly.com/songs/roadrunner.mp3");

                updates.put("experience_jobs_1_name", "ACME Industries, Inc.");
                updates.put("experience_jobs_1_webpage_address", "http://example-acme.com");

                updates.put("experience_jobs_2_name", "Virtual Corporation");
                updates.put("experience_jobs_2_webpage_address", "http://example-virtual.com");

                updates.put("experience_jobs_3_name", "Moon Microsystems, Inc.");
                updates.put("experience_jobs_3_webpage_address", "http://example-moon.com");

                updates.put("experience_jobs_4_name", "Whatsit, Inc.");
                updates.put("experience_jobs_4_webpage_address", "http://example-whatsit.com");

                profile.update(Profile.Format.FLAT, updates);
                mgr.saveProfile(profile);
                Factory.getSocialSite().flush();
                TestUtils.endSession(true);
            }

            // convert to JSON
            {
                Profile profile = mgr.getProfileByUserId("steves2");
                String json = profile.toJSON(Profile.Format.OPENSOCIAL).toString(4);
                log.info(json);
            }

            // remove the profile
            mgr.removeProfile(mgr.getProfileByUserId("steves2"));
            TestUtils.endSession(true);

        } catch (Exception e) {
            log.error("ERROR", e);
        }

        log.info("END");

    }

}
