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
import com.sun.socialsite.pojos.RelationshipRequest;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.pojos.SectionPrivacy;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;


/**
 * Test section privacy related business operations.
 */
public class SectionPrivacyTest extends TestCase {

    public static Log log = LogFactory.getLog(SectionPrivacyTest.class);


    public void setUp() throws Exception {
        TestUtils.setupSocialSite();
    }


    public static Test suite() {
        return new TestSuite(SectionPrivacyTest.class);
    }


    public void tearDown() throws Exception {
    }


    public void testFriendPrivacy() {

        log.info("BEGIN");

        try {

            ProfileManager profileManager = Factory.getSocialSite().getProfileManager();

            Profile bart = createProfile("bart", "Bart", "Simpson", "elbarto@gmail.com");
            Profile ralph = createProfile("ralph", "Ralph", "Wiggum", "ralph@police.springfield.gov");

            // add some properties to bart, properties are private by default
            bart = profileManager.getProfileByUserId("bart"); // get managed instance
            addSomePropertiesPersonal(bart);

            // add more
            bart = profileManager.getProfileByUserId("bart"); // get managed instance
            addSomePropertiesExperience(bart);

            // verify that ralph CANNOT see any of bart's properties
            bart = profileManager.getProfileByUserId("bart");
            ralph = profileManager.getProfileByUserId("ralph");
            assertEquals(0, bart.getPropertiesForViewer(ralph.getUserId()).size());

            // verify that bart can see all of his own properties
            // that's 9 profile properties + 7 section privacy properties
            assertEquals(16, bart.getPropertiesForViewer(bart.getUserId()).size());

            // bart and ralph become friends
            makeRelationship(bart, ralph, 1);

            // verify that ralph STILL cannot see any of bart's properties
            bart = profileManager.getProfileByUserId("bart");
            ralph = profileManager.getProfileByUserId("ralph");
            assertEquals(0, bart.getPropertiesForViewer(ralph.getUserId()).size());

            // bart makes his job experience visible to friends
            Map<String, SectionPrivacy> privs = profileManager.getSectionPrivacies(bart);
            SectionPrivacy priv = privs.get("experience");
            priv.setVisibility(Profile.VisibilityType.FRIENDS);
            priv.setRelationshipLevel(1);
            profileManager.updateSectionPrivacy(bart, priv);
            TestUtils.endSession(true);

            // verify that ralph can now see bart's job experience properties
            // that's 8 properties + 1 section privacy property
            bart = profileManager.getProfileByUserId("bart");
            ralph = profileManager.getProfileByUserId("ralph");
            assertEquals(9, bart.getPropertiesForViewer(ralph.getUserId()).size());

            // remove profiles
            profileManager.removeProfile(profileManager.getProfileByUserId("bart"));
            profileManager.removeProfile(profileManager.getProfileByUserId("ralph"));
            TestUtils.endSession(true);

        } catch (Exception e) {
            log.error("ERROR", e);
        }

        log.info("END");

    }


    public void testSomeGroupPrivacy() {
    }


    public void testAllGroupPrivacy() {

    }


    public void testPrivacySettingsCRUD() throws Exception {
    }


    Profile createProfile(String userId, String first, String last, String email) throws Exception {

        ProfileManager profileManager = Factory.getSocialSite().getProfileManager();

        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setFirstName(first);
        profile.setLastName(last);
        profile.setPrimaryEmail(email);
        profileManager.saveProfile(profile);
        TestUtils.endSession(true);

        return profile;

    }


    void makeRelationship(Profile p1, Profile p2, int level) throws Exception {

        ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
        RelationshipManager fmgr = Factory.getSocialSite().getRelationshipManager();

        fmgr.requestRelationship(p1, p2, 1, "met at work");
        TestUtils.endSession(true);

        p1 = profileManager.getProfile(p1.getId());
        List<RelationshipRequest> freqs = fmgr.getRelationshipRequestsByFromProfile(p1, 0, -1);
        fmgr.acceptRelationshipRequest(freqs.get(0), 1);
        TestUtils.endSession(true);

    }


    void addSomePropertiesPersonal(Profile p) throws Exception {

        ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
        JSONObject updates = new JSONObject();

        updates.put("personal_profileSong_address", "http://bodiddly.com/songs/roadrunner.mp3");

        p.update(Profile.Format.FLAT, updates);
        profileManager.saveProfile(p);
        Factory.getSocialSite().flush();
        TestUtils.endSession(true);

    }


    void addSomePropertiesExperience(Profile profile) throws Exception {

        ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
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
        profileManager.saveProfile(profile);
        Factory.getSocialSite().flush();
        TestUtils.endSession(true);

    }

}
