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
import com.sun.socialsite.userapi.User;
import com.sun.socialsite.Utils;
import com.sun.socialsite.business.impl.JPARelationshipManagerImpl;
import com.sun.socialsite.config.Config;
import com.sun.socialsite.pojos.RelationshipRequest;
import com.sun.socialsite.pojos.Profile;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test Friend-related business operations when  two-way friends required.
 */
public class FriendsTwoWayOnlyTest extends TestCase {

    protected boolean twoWayOnly = true;
    
    public static Log log = LogFactory.getLog(FriendsTwoWayOnlyTest.class);

    public static int FRIENDSHIP_LEVEL = Config.getIntProperty(
    "socialsite.relationship.friendshiplevel");;

    // Assuming 

    public void setUp() throws Exception {
        Utils.setupSocialSite();
    }


    public static Test suite() {
        return new TestSuite(FriendsTwoWayOnlyTest.class);
    }


    public void tearDown() throws Exception {
    }


    public void testFriendRequestCRUD() throws Exception {

        log.info("BEGIN");

        try {

            RelationshipManager mgr = Factory.getSocialSite().getRelationshipManager();
            ProfileManager pmgr = Factory.getSocialSite().getProfileManager();

            // create user 1
            User testUser1 = Utils.setupUser("testUser1");
            Profile dave = Utils.setupProfile(testUser1, "Test", "User1");
            pmgr.saveProfile(dave);

            // create user 2
            User testUser2 = Utils.setupUser("testUser2");
            Profile friend = Utils.setupProfile(testUser2, "Test", "User2");
            pmgr.saveProfile(friend);
            Utils.endSession(true);

            // request relationship from 1 to 2
            mgr.requestRelationship(dave, friend, FRIENDSHIP_LEVEL, "met at work");
            Utils.endSession(true);

            // assert requests exists
            dave = pmgr.getProfile(dave.getId());
            List<RelationshipRequest> freqs = mgr.getRelationshipRequestsByFromProfile(dave,0,-1);
            assertEquals(1, freqs.size());

            // remove request
            mgr.removeRelationshipRequest(freqs.get(0));
            Utils.endSession(true);

            // assert request is gone
            dave = pmgr.getProfile(dave.getId());
            List<RelationshipRequest> freqs2 = mgr.getRelationshipRequestsByFromProfile(dave,0,-1);
            assertEquals(0, freqs2.size());

            // clean up
            dave = pmgr.getProfile(dave.getId());
            friend = pmgr.getProfile(friend.getId());
            pmgr.removeProfile(dave);
            pmgr.removeProfile(friend);
            Utils.teardownUser(testUser1.getUserName());
            Utils.teardownUser(testUser2.getUserName());
            Utils.endSession(true);

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }


    public void testRelationshipAccept() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager pmgr = Factory.getSocialSite().getProfileManager();

            // create two people snoopdave and fred
            Profile profile1 = new Profile();
            profile1.setUserId("snoopdave");
            profile1.setFirstName("David");
            profile1.setMiddleName("Mason");
            profile1.setLastName("Johnson");
            profile1.setPrimaryEmail("davidm.johnson@sun.com");
            pmgr.saveProfile(profile1);
            Utils.endSession(true);

            Profile profile2 = new Profile();
            profile2.setUserId("fred");
            profile2.setFirstName("Fred");
            profile2.setMiddleName("Richard");
            profile2.setLastName("Mortimer");
            profile2.setPrimaryEmail("fred@example.com");
            pmgr.saveProfile(profile2);
            Utils.endSession(true);

            RelationshipManager mgr = Factory.getSocialSite().getRelationshipManager();
            assertNotNull(mgr);

            // create a friend requst from snoopdave to fred
            Profile snoopdave = pmgr.getProfile(profile1.getId());
            Profile fred = pmgr.getProfile(profile2.getId());
            mgr.requestRelationship(snoopdave, fred, FRIENDSHIP_LEVEL, "met at work");
            Utils.endSession(true);

            // verify that request exists
            snoopdave = pmgr.getProfile(snoopdave.getId());
            List<RelationshipRequest> freqs = mgr.getRelationshipRequestsByFromProfile(snoopdave, 0, -1);
            assertEquals(1, freqs.size());

            if (!twoWayOnly) {
                // if one way friendships are allowed then
                // verify that snoopdave now friends with fred
                assertEquals(1, mgr.getRelationships(snoopdave, 0, -1).size());
            } else {
                assertEquals(0, mgr.getRelationships(snoopdave, 0, -1).size());
            }

            // verify that fred still not friends with snoopdave
            fred = pmgr.getProfile(fred.getId());
            assertEquals(0, mgr.getRelationships(fred, 0, -1).size());

            // fred accepts the friend request
            mgr.acceptRelationshipRequest(freqs.get(0), 1);
            Utils.endSession(true);

            // verify that fred now friends with snoopdave
            fred = pmgr.getProfile(fred.getId());
            snoopdave = pmgr.getProfile(snoopdave.getId());
            assertEquals(1, mgr.getRelationships(snoopdave, 0, -1).size());

            // remove the two relationships between fred and snoopdave
            mgr.removeRelationships(snoopdave, fred);
            Utils.endSession(true);

            // verify that relationships are gone
            snoopdave = pmgr.getProfile(snoopdave.getId());
            assertEquals(0, mgr.getRelationships(snoopdave, 0, -1).size());
            fred = pmgr.getProfile(fred.getId());
            assertEquals(0, mgr.getRelationships(fred, 0, -1).size());

            // remove the snoopdave and dave profiles
            fred = pmgr.getProfile(fred.getId());
            snoopdave = pmgr.getProfile(snoopdave.getId());
            pmgr.removeProfile(snoopdave);
            pmgr.removeProfile(fred);
            Utils.endSession(true);

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }


    public void testRelationshipClarify() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager pmgr = Factory.getSocialSite().getProfileManager();

            // create two people snoopdave and fred
            Profile profile1 = new Profile();
            profile1.setUserId("snoopdave2");
            profile1.setFirstName("David");
            profile1.setMiddleName("Mason");
            profile1.setLastName("Johnson");
            profile1.setPrimaryEmail("davidm.johnson@sun.com");
            pmgr.saveProfile(profile1);
            Utils.endSession(true);

            Profile profile2 = new Profile();
            profile2.setUserId("fred");
            profile2.setFirstName("Fred");
            profile2.setMiddleName("Richard");
            profile2.setLastName("Mortimer");
            profile2.setPrimaryEmail("fred@example.com");
            pmgr.saveProfile(profile2);
            Utils.endSession(true);

            RelationshipManager mgr = Factory.getSocialSite().getRelationshipManager();
            assertNotNull(mgr);


            // create a friend requst from snoopdave to fred

            Profile snoopdave = pmgr.getProfile(profile1.getId());
            Profile fred = pmgr.getProfile(profile2.getId());
            mgr.requestRelationship(snoopdave, fred, FRIENDSHIP_LEVEL, "met at work");
            Utils.endSession(true);

            // verify that request exists
            // on snoopdave's end
            snoopdave = pmgr.getProfile(snoopdave.getId());
            List<RelationshipRequest> fromDaveReqs =
                    mgr.getRelationshipRequestsByFromProfile(snoopdave, 0, -1);
            assertEquals(1, fromDaveReqs.size());
            // and on fred's end
            fred = pmgr.getProfile(fred.getId());
            List<RelationshipRequest> toFredReqs = mgr.getRelationshipRequestsByToProfile(fred, 0, -1);
            assertEquals(1, toFredReqs.size());

            if (!twoWayOnly) {
                // if one way friendships are allowed then
                // verify that snoopdave now friends with fred
                assertEquals(1, mgr.getRelationships(snoopdave, 0, -1).size());
            } else {
                assertEquals(0, mgr.getRelationships(snoopdave, 0, -1).size());
            }

            // verify that fred still not friends with snoopdave
            fred = pmgr.getProfile(fred.getId());
            assertEquals(0, mgr.getRelationships(fred, 0, -1).size());


            // fred clarifies the friend request

            mgr.clarifyRelationshipRequest(toFredReqs.get(0), 1, "met at ACME Corporation");
            Utils.endSession(true);

            // verify that fred still not friends with snoopdave
            fred = pmgr.getProfile(fred.getId());
            assertEquals(0, mgr.getRelationships(fred, 0, -1).size());

            // verify that snoopdave now has the request again
            // on snoopdave's end
            snoopdave = pmgr.getProfile(snoopdave.getId());
            List<RelationshipRequest> toDaveReqs =
                    mgr.getRelationshipRequestsByToProfile(snoopdave, 0, -1);
            assertEquals(1, toDaveReqs.size());
            // and on fred's end
            fred = pmgr.getProfile(fred.getId());
            List<RelationshipRequest> fromFredReqs = 
                    mgr.getRelationshipRequestsByFromProfile(fred, 0, -1);
            assertEquals(1, fromFredReqs.size());


            // snoopdave accepts the clarified request
            mgr.acceptRelationshipRequest(toDaveReqs.get(0), 1);
            Utils.endSession(true);


            // verify that fred now friends with snoopdave with same how know message
            fred = pmgr.getProfile(fred.getId());
            snoopdave = pmgr.getProfile(snoopdave.getId());
            assertEquals(1, mgr.getRelationships(fred, 0, -1).size());
            assertEquals(1, mgr.getRelationships(snoopdave, 0, -1).size());
            assertEquals(
                mgr.getRelationships(fred, 0, -1).get(0).getHowknow(),
                mgr.getRelationships(snoopdave, 0, -1).get(0).getHowknow());
            

            // remove the two relationships between fred and snoopdave
            mgr.removeRelationships(snoopdave, fred);
            Utils.endSession(true);

            // verify that relationships are gone
            snoopdave = pmgr.getProfile(snoopdave.getId());
            assertEquals(0, mgr.getRelationships(snoopdave, 0, -1).size());
            fred = pmgr.getProfile(fred.getId());
            assertEquals(0, mgr.getRelationships(fred, 0, -1).size());

            // remove the snoopdave and dave profiles
            fred = pmgr.getProfile(fred.getId());
            snoopdave = pmgr.getProfile(snoopdave.getId());
            pmgr.removeProfile(snoopdave);
            pmgr.removeProfile(fred);
            Utils.endSession(true);

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }


    /*
     * The requestRelationship() method should return true
     * if it is not a duplicate request.
     */
    public void testDuplicateFriendRequest() throws Exception {

        log.info("BEGIN");

        try {

            RelationshipManager mgr = Factory.getSocialSite().getRelationshipManager();
            assertNotNull(mgr);

            ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
            assertNotNull(pmgr);

            User aliceUser = Utils.setupUser("alice");
            Profile alice = Utils.setupProfile(aliceUser, "Alice", "Flappenpopper");
            pmgr.saveProfile(alice);
            Utils.endSession(true);

            User bobUser = Utils.setupUser("bob");
            Profile bob = Utils.setupProfile(bobUser, "Bob", "Smokeshifter");
            pmgr.saveProfile(bob);
            Utils.endSession(true);

            alice = pmgr.getProfile(alice.getId());
            bob = pmgr.getProfile(bob.getId());
            mgr.requestRelationship(alice, bob, FRIENDSHIP_LEVEL, "met at work");
            Utils.endSession(true);

            // request relationship once
            alice = pmgr.getProfile(alice.getId());
            bob = pmgr.getProfile(bob.getId());
            List<RelationshipRequest> freqs = mgr.getRelationshipRequestsByFromProfile(alice,0,-1);
            assertEquals(1, freqs.size());

            boolean dupError = false;
            try {
                // request relatonship again
                mgr.requestRelationship(alice, bob, FRIENDSHIP_LEVEL, "met at work");
                Utils.endSession(true);
            } catch (SocialSiteException sse) {
                dupError = true;
            }
            // should have gotten exception due to duplicate request
            assertTrue(dupError);

            // number of requests should be only 1
            alice = pmgr.getProfile(alice.getId());
            freqs = mgr.getRelationshipRequestsByFromProfile(alice, 0, -1);
            assertEquals(1, freqs.size());

            mgr.removeRelationshipRequest(freqs.get(0));
            Utils.endSession(true);

            // remove the alice and bob profiles
            alice = pmgr.getProfile(alice.getId());
            bob= pmgr.getProfile(bob.getId());
            pmgr.removeProfile(alice);
            pmgr.removeProfile(bob);
            Utils.endSession(true);

            // remove alice and bob users
            Utils.teardownUser(aliceUser.getUserId());
            Utils.teardownUser(bobUser.getUserId());
            Utils.endSession(true);

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }

}
