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
import com.sun.socialsite.pojos.Group;
import com.sun.socialsite.pojos.GroupRelationship;
import com.sun.socialsite.pojos.GroupRequest;
import com.sun.socialsite.pojos.Profile;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test Group-related business operations.
 */
public class GroupTest extends TestCase {

    public static Log log = LogFactory.getLog(GroupTest.class);


    public void setUp() throws Exception {
        Utils.setupSocialSite();

    }

    public static Test suite() {
        return new TestSuite(GroupTest.class);
    }


    public void tearDown() throws Exception {
    }

    public void testGroupCRUD() throws Exception {

        log.info("BEGIN");

        try {

            GroupManager mgr = Factory.getSocialSite().getGroupManager();
            assertNotNull(mgr);

            List<Group> all = mgr.getGroups(0, -1);
            for (Group e : all) {
                mgr.removeGroup(e);
            }
            Utils.endSession(true);

            Group group1 = new Group();
            group1.setName("group1");
            group1.setHandle("group1");
            mgr.saveGroup(group1);
            Utils.endSession(true);
            assertNotNull(mgr.getGroupByHandle("group1"));

            Group group2 = new Group();
            group2.setName("group2");
            group2.setHandle("group2");
            mgr.saveGroup(group2);
            Utils.endSession(true);
            assertNotNull(mgr.getGroupByHandle("group2"));

            List<Group> results1 = mgr.getOldestGroups(0, -1);
            assertEquals(2, results1.size());
            assertTrue(group1.equals(results1.get(0)));
            assertTrue(group2.equals(results1.get(1)));

            List<Group> results2 = mgr.getMostRecentlyUpdatedGroups(0, -1);
            assertEquals(2, results2.size());
            assertTrue(group2.equals(results2.get(0)));
            assertTrue(group1.equals(results2.get(1)));

            mgr.removeGroup(mgr.getGroupByHandle("group1"));
            Utils.endSession(true);

            mgr.removeGroup(mgr.getGroupByHandle("group2"));
            Utils.endSession(true);

            assertNull(mgr.getGroupByHandle("group1"));
            assertNull(mgr.getGroupByHandle("group2"));


        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }


    public void testGroupRequestCRUD() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager pmgr = Factory.getSocialSite().getProfileManager();

            // create persona dave
            Profile profile1 = new Profile();
            profile1.setUserId("snoopdave3");
            profile1.setFirstName("David");
            profile1.setMiddleName("Mason");
            profile1.setLastName("Johnson");
            profile1.setPrimaryEmail("davidm.johnson@sun.com");
            pmgr.saveProfile(profile1);
            Utils.endSession(true);

            GroupManager mgr = Factory.getSocialSite().getGroupManager();
            assertNotNull(mgr);

            // create group3
            Group group3 = new Group();
            group3.setHandle("group3");
            group3.setName("group3");
            mgr.saveGroup(group3);
            Utils.endSession(true);
            // verify that group exists
            group3 = mgr.getGroupByHandle("group3");
            assertNotNull(group3);

            // create request from snoopdave to join group3
            Profile snoopdave = pmgr.getProfile(profile1.getId());
            assertTrue("New membership request should return true",
                    mgr.requestMembership(group3, snoopdave));
            Utils.endSession(true);
            // duplicate request should be ignored
            snoopdave = pmgr.getProfile(profile1.getId());
            assertFalse("Duplicate membership request should return false",
                    mgr.requestMembership(group3, snoopdave));
            Utils.endSession(true);
            // verify that request exists
            group3 = mgr.getGroupByHandle("group3");
            List<GroupRequest> nreqs = mgr.getMembershipRequestsByGroup(group3, 0, -1);
            assertEquals(1, nreqs.size());

            // accept request from snoopdave to join group3
            GroupRequest nreq = nreqs.get(0);
            mgr.acceptMembership(nreq);
            Utils.endSession(true);

            // verify that snoopdave is member of group3
            snoopdave = pmgr.getProfile(profile1.getId());
            List<GroupRelationship> groups = mgr.getMembershipsByProfile(snoopdave, 0, -1);
            assertEquals(1, groups.size());

            // remove group relationship
            mgr.removeMembership(groups.get(0).getGroup(), snoopdave);
            Utils.endSession(true);

            // verify that relationship is gone
            snoopdave = pmgr.getProfile(profile1.getId());
            group3 = mgr.getGroupByHandle("group3");
            List<GroupRelationship> rels = mgr.getMembershipsByProfile(snoopdave, 0, -1);
            assertEquals(0, rels.size());

            // remove the snoopdave profile
            pmgr.removeProfile(snoopdave);
            Utils.endSession(true);

            // remove the group
            group3 = mgr.getGroupByHandle("group3");
            mgr.removeGroup(group3);
            Utils.endSession(true);

            // assert that profile and group are gone
            assertNull(pmgr.getProfileByUserId("snoopdave3"));
            assertNull(mgr.getGroupByHandle("group3"));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }


    public void testGroupTypeCRUD() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
            Profile profile1 = new Profile();
            profile1.setUserId("manveen");
            profile1.setFirstName("Manveen");
            profile1.setLastName("Kaur");
            profile1.setPrimaryEmail("manveen.kaur@sun.com");
            pmgr.saveProfile(profile1);
            Utils.endSession(true);

            GroupManager mgr = Factory.getSocialSite().getGroupManager();
            assertNotNull(mgr);

            Group group5 = new Group();
            group5.setName("group5");
            group5.setHandle("group5");
            group5.setPolicy(Group.Policy.allowAll);
            mgr.createGroup(group5, profile1);
            Utils.endSession(true);
            assertNotNull(mgr.getGroupByHandle("group5"));

            // verify that manveen is the founder of the group
            Profile manveen = pmgr.getProfile(profile1.getId());
            List<GroupRelationship> groups = mgr.getMembershipsByProfile(manveen, 0, -1);
            assertEquals(1, groups.size());

            // create a new profile to request membership in
            Profile profile2 = new Profile();
            profile2.setUserId("m2");
            profile2.setFirstName("M2");
            profile2.setLastName("Kaur");
            profile2.setPrimaryEmail("m2.kaur@sun.com");
            pmgr.saveProfile(profile2);
            Utils.endSession(true);

            Profile m2 = pmgr.getProfile(profile2.getId());
            // create request from manveen to join group5
            assertTrue("New membership request should return true",
                    mgr.requestMembership(group5, m2));
            Utils.endSession(true);

            assertFalse("Duplicate membership request should return false",
                    mgr.requestMembership(group5, m2));
            Utils.endSession(true);

            // verify that request exists
            group5 = mgr.getGroupByHandle("group5");
            List<GroupRequest> nreqs = mgr.getMembershipRequestsByGroup(group5, 0, -1);
            assertEquals(1, nreqs.size());

            // accept request from manveen to join group5
            GroupRequest nreq = nreqs.get(0);
            mgr.acceptMembership(nreq);
            Utils.endSession(true);

            // verify that manveen is member of group5
            groups = mgr.getMembershipsByProfile(m2, 0, -1);
            assertEquals(1, groups.size());

            // remove group relationship
            mgr.removeMembership(groups.get(0).getGroup(), m2);
            Utils.endSession(true);

            // verify that relationship is gone
            manveen = pmgr.getProfile(profile1.getId());
            m2 = pmgr.getProfile(profile2.getId());
            group5 = mgr.getGroupByHandle("group5");
            List<GroupRelationship> rels = mgr.getMembershipsByProfile(m2, 0, -1);
            assertEquals(0, rels.size());

            // remove the profiles
            pmgr.removeProfile(manveen);
            pmgr.removeProfile(m2);
            Utils.endSession(true);

            // remove the group
            group5 = mgr.getGroupByHandle("group5");
            mgr.removeGroup(group5);
            Utils.endSession(true);

            // assert that removals worked
            assertNull(pmgr.getProfileByUserId("manveen"));
            assertNull(pmgr.getProfileByUserId("m2"));
            assertNull(mgr.getGroupByHandle("group5"));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }


   public void testAdminOnlyGroup() throws Exception {

        log.info("BEGIN");

        try {

            ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
            Profile profile1 = new Profile();
            profile1.setUserId("admin");
            profile1.setFirstName("Admin");
            profile1.setLastName("Jee");
            profile1.setPrimaryEmail("admin@sun.com");
            pmgr.saveProfile(profile1);
            Utils.endSession(true);

            GroupManager mgr = Factory.getSocialSite().getGroupManager();
            assertNotNull(mgr);

            Group group5 = new Group();
            group5.setName("club");
            group5.setHandle("club");
            group5.setPolicy(Group.Policy.adminOnly);
            mgr.createGroup(group5, profile1);
            Utils.endSession(true);
            assertNotNull(mgr.getGroupByHandle("club"));

            // verify that manveen is the founder of the group
            Profile manveen = pmgr.getProfile(profile1.getId());
            List<GroupRelationship> groups = mgr.getMembershipsByProfile(manveen, 0, -1);
            assertEquals(1, groups.size());

            // create a new profile to request membership in
            Profile profile2 = new Profile();
            profile2.setUserId("m2");
            profile2.setFirstName("M2");
            profile2.setLastName("Kaur");
            profile2.setPrimaryEmail("m2.kaur@sun.com");
            pmgr.saveProfile(profile2);
            Utils.endSession(true);

            Profile m2 = pmgr.getProfile(profile2.getId());
            // create request from manveen to join group5
            assertTrue("New membership request should return true",
                    mgr.requestMembership(group5, m2));
            Utils.endSession(true);

            assertFalse("Duplicate membership request should return false",
                    mgr.requestMembership(group5, m2));
            Utils.endSession(true);

            // verify that request exists
            group5 = mgr.getGroupByHandle("club");
            List<GroupRequest> nreqs = mgr.getMembershipRequestsByGroup(group5, 0, -1);
            assertEquals(1, nreqs.size());

            // accept request from manveen to join the club
            GroupRequest nreq = nreqs.get(0);
            mgr.acceptAsGroupAdmin(nreq);
            Utils.endSession(true);

            // verify that manveen is member of group5
            groups = mgr.getMembershipsByProfile(m2, 0, -1);
            assertEquals(GroupRelationship.Relationship.ADMIN,groups.get(0).getRelcode());
            assertEquals(1, groups.size());

            // remove group relationship
            mgr.removeMembership(groups.get(0).getGroup(), m2);
            Utils.endSession(true);

            // verify that relationship is gone
            manveen = pmgr.getProfile(profile1.getId());
            m2 = pmgr.getProfile(profile2.getId());
            group5 = mgr.getGroupByHandle("club");
            List<GroupRelationship> rels = mgr.getMembershipsByProfile(m2, 0, -1);
            assertEquals(0, rels.size());
            // remove the profile
            pmgr.removeProfile(manveen);
            pmgr.removeProfile(m2);
            Utils.endSession(true);

            // remove the group
            group5 = mgr.getGroupByHandle("club");
            mgr.removeGroup(group5);
            Utils.endSession(true);
            assertNull(mgr.getGroupByHandle("club"));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");

    }

    public void testGrantAdminRights() throws Exception {
        log.info("BEGIN");
        try {
            GroupManager mgr = Factory.getSocialSite().getGroupManager();

            // create group founder
            ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
            Profile founder = new Profile();
            founder.setUserId("admin");
            founder.setFirstName("Beth");
            founder.setLastName("Stone");
            founder.setPrimaryEmail("admin@sun.com");
            pmgr.saveProfile(founder);
            Utils.endSession(true);

            // create some guy
            Profile guy = new Profile();
            guy.setUserId("guy");
            guy.setFirstName("Some");
            guy.setLastName("Guy");
            guy.setPrimaryEmail("someguy@someplace.net");
            pmgr.saveProfile(guy);
            Utils.endSession(true);

            // create group
            Group group = new Group();
            group.setName("Rock Club");
            group.setHandle("rockclub");
            group.setPolicy(Group.Policy.adminOnly);
            mgr.createGroup(group, founder);
            Utils.endSession(true);
            assertNotNull(mgr.getGroupByHandle("rockclub"));

            // grant founder and guy admin rights
            guy = pmgr.getProfile(guy.getId());
            group = mgr.getGroupByHandle("rockclub");
            mgr.grantAdminRights(group, founder);
            mgr.grantAdminRights(group, guy);
            Utils.endSession(true);
            
            // guy should be member and admin
            assertTrue(mgr.isMember(group, guy));
            assertTrue(mgr.isAdmin(group, guy));

            // founder should still be founder
            assertTrue(mgr.isFounder(group, founder));

            // remove the profiles
            guy = pmgr.getProfile(guy.getId());
            pmgr.removeProfile(guy);
            founder = pmgr.getProfile(founder.getId());
            pmgr.removeProfile(founder);
            Utils.endSession(true);

            // remove the group
            group = mgr.getGroupByHandle("rockclub");
            mgr.removeGroup(group);
            Utils.endSession(true);
            assertNull(mgr.getGroupByHandle("rockclub"));

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");
    }
}
