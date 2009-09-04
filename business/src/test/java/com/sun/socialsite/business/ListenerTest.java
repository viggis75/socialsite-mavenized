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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test Listener-related business operations.
 */
public class ListenerTest extends TestCase {

    public static Log log = LogFactory.getLog(ListenerTest.class);


    public void setUp() throws Exception {
        Utils.setupSocialSite();

    }


    public static Test suite() {
        return new TestSuite(ListenerTest.class);
    }


    public void tearDown() throws Exception {
    }


    public void testProfileCRUD() throws Exception {

        log.info("BEGIN");

        try {

            ListenerManager listenerManager = Factory.getSocialSite().getListenerManager();
            List<ProfileEvent> profileEvents = Collections.synchronizedList(new ArrayList<ProfileEvent>());
            listenerManager.addListener(Profile.class, new ProfileListener(profileEvents));

            ProfileManager profileManager = Factory.getSocialSite().getProfileManager();

            // Step 1: Create a profile for "john"
            Profile profile1 = new Profile();
            profile1.setUserId("john");
            profile1.setFirstName("Jonathan");
            profile1.setMiddleName("Franklin");
            profile1.setLastName("Doe");
            profile1.setPrimaryEmail("john.f.doe@example.com");
            profileManager.saveProfile(profile1);
            Utils.endSession(true);
            assertNotNull(profileManager.getProfileByUserId("john"));

            // Verify that we received expected events for Step 1
            assertEquals(PrePersist.class, profileEvents.get(0).annotationClass);
            assertEquals(profile1, profileEvents.get(0).profile);
            assertEquals(PostPersist.class, profileEvents.get(1).annotationClass);
            assertEquals(profile1, profileEvents.get(1).profile);

            // Step 2: Make an update to john's profile
            Profile profile1a = profileManager.getProfileByUserId("john");
            assertNotNull(profile1a);
            Date d1 = profile1a.getUpdated();
            profile1a.setSurtitle("Jr");
            profileManager.saveProfile(profile1a);
            Utils.endSession(true);

            // Verify that we received expected events for Step 2
            assertEquals(PostLoad.class, profileEvents.get(4).annotationClass);
            assertEquals(profile1a, profileEvents.get(4).profile);
            assertEquals(PreUpdate.class, profileEvents.get(5).annotationClass);
            assertEquals(profile1a, profileEvents.get(5).profile);
            assertEquals(PostUpdate.class, profileEvents.get(6).annotationClass);
            assertEquals(profile1a, profileEvents.get(6).profile);

            // Step 3: Read back the result, and make sure the "updated" timestamp has progressed
            Profile profile1b = profileManager.getProfileByUserId("john");
            assertNotNull(profile1b);
            Date d2 = profile1b.getUpdated();
            assertTrue("d2 should be greater than d1", (d2.getTime() > d1.getTime()));
            Utils.endSession(true);

            // Verify that we received expected events for Step 3
            assertEquals(PostLoad.class, profileEvents.get(7).annotationClass);
            assertEquals(profile1b, profileEvents.get(7).profile);

            // Step 4: Delete john's profile
            Profile profile1c = profileManager.getProfileByUserId("john");
            profileManager.removeProfile(profile1c);
            Utils.endSession(true);

            // Verify that we received expected events for Step 4
            assertEquals(PostLoad.class, profileEvents.get(8).annotationClass);
            assertEquals(profile1c, profileEvents.get(8).profile);
            assertEquals(PreRemove.class, profileEvents.get(9).annotationClass);
            assertEquals(profile1c, profileEvents.get(9).profile);
            assertEquals(PostRemove.class, profileEvents.get(10).annotationClass);
            assertEquals(profile1c, profileEvents.get(10).profile);

            assertNull(profileManager.getProfileByUserId("john"));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }

}


class ProfileListener {

    List<? super ProfileEvent> profileEvents;

    public ProfileListener(List<? super ProfileEvent> profileEvents) {
        this.profileEvents = profileEvents;
    }

    @PostLoad
    public void postLoad(Profile profile) {
        profileEvents.add(new ProfileEvent(PostLoad.class, profile));
    }

    @PrePersist
    public void prePersist(Profile profile) {
        profileEvents.add(new ProfileEvent(PrePersist.class, profile));
    }

    @PostPersist
    public void postPersist(Profile profile) {
        profileEvents.add(new ProfileEvent(PostPersist.class, profile));
    }

    @PreRemove
    public void preRemove(Profile profile) {
        profileEvents.add(new ProfileEvent(PreRemove.class, profile));
    }

    @PostRemove
    public void postRemove(Profile profile) {
        profileEvents.add(new ProfileEvent(PostRemove.class, profile));
    }

    @PreUpdate
    public void preUpdate(Profile profile) {
        profileEvents.add(new ProfileEvent(PreUpdate.class, profile));
    }

    @PostUpdate
    public void postUpdate(Profile profile) {
        profileEvents.add(new ProfileEvent(PostUpdate.class, profile));
    }

}


class ProfileEvent {

    public final Class annotationClass;

    public final Profile profile;

    public ProfileEvent(Class annotationClass, Profile profile) {
        this.annotationClass = annotationClass;
        this.profile = profile;
    }

}
