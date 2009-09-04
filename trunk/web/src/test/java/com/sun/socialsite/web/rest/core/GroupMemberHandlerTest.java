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

package com.sun.socialsite.web.rest.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.socialsite.Utils;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.business.GroupManager;
import com.sun.socialsite.business.ProfileManager;
import com.sun.socialsite.pojos.App;
import com.sun.socialsite.pojos.Group;
import com.sun.socialsite.pojos.GroupRelationship;
import com.sun.socialsite.pojos.GroupRequest;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.web.rest.config.SocialSiteGuiceModule;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.protocol.DefaultHandlerRegistry;
import org.apache.shindig.protocol.HandlerExecutionListener;
import org.apache.shindig.protocol.HandlerRegistry;
import org.apache.shindig.protocol.RestHandler;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.service.FakeSocialSiteGadgetToken;
import org.json.JSONObject;


/**
 * Extend Shindig test to test our extensions to the Shindig handler.
 */
public class GroupMemberHandlerTest extends TestCase {

    public static Log log = LogFactory.getLog(GroupMemberHandlerTest.class);

    private BeanJsonConverter converter;
    protected GroupMemberHandler handler;
    protected FakeSocialSiteGadgetToken token;
    protected HandlerRegistry registry;

    @Override
    protected void setUp() throws Exception {
        Injector injector = Guice.createInjector(new SocialSiteGuiceModule());
        converter = injector.getInstance(BeanJsonConverter.class);

        Utils.setupSocialSite();
        List<App> apps = Factory.getSocialSite().getAppManager().getApps(0, 1);
        token = new FakeSocialSiteGadgetToken();
        token.setAppId(apps.get(0).getId());

        handler = new GroupMemberHandler(converter);
        registry = new DefaultHandlerRegistry(null, Sets.<Object>newHashSet(handler), converter,
            new HandlerExecutionListener.NoOpHandlerExecutionListener());
    }


    public void testGetGroupMembers() throws Exception {

        log.info("BEGIN");

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.ADMIN);
            gmgr.createMembership(group, sue, GroupRelationship.Relationship.MEMBER);
            Utils.endSession(true);

            token.setViewerId(jack.getUserId());
            
            String path = "/members/" + group.getHandle();
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = operation = registry.getRestHandler(path, "GET");
            Future<?> future = operation.execute(params, new StringReader(""), token, converter);

            RestfulCollection<?> collection = (RestfulCollection<?>)(future.get());
            assertNotNull(collection);
            assertEquals(2, collection.getEntry().size());
            for (Object entry : collection.getEntry()) {
                boolean isAnExpectedType = Person.class.isAssignableFrom(entry.getClass());
                assertTrue(String.format("Unexpected entry type: %s", entry.getClass()), isAnExpectedType);
                Person person = (Person)entry;
                boolean isAnExpectedEntry = (person.getId().equals(jack.getUserId()) || person.getId().equals(sue.getUserId()));
                assertTrue("collection should contain only jack and jill", isAnExpectedEntry);
            }

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }


    public void testGetGroupAdmins() throws Exception {

        log.info("BEGIN");

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            jack = pmgr.getProfile(jack.getId());
            sue = pmgr.getProfile(sue.getId());
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.ADMIN);
            gmgr.createMembership(group, sue, GroupRelationship.Relationship.MEMBER);
            Utils.endSession(true);

            token.setViewerId(jack.getUserId());

            String path = "/members/" + group.getHandle() + "/@admins";
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = operation = registry.getRestHandler(path, "GET");
            Future<?> future = operation.execute(params, new StringReader(""), token, converter);

            RestfulCollection<?> collection = (RestfulCollection<?>)(future.get());
            assertNotNull(collection);
            assertEquals(1, collection.getEntry().size());
            for (Object entry : collection.getEntry()) {
                boolean isAnExpectedType = Person.class.isAssignableFrom(entry.getClass());
                assertTrue(String.format("Unexpected entry type: %s", entry.getClass()), isAnExpectedType);
                Person person = (Person)entry;
                assertEquals(jack.getUserId(), person.getId());
            }

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }


    public void testRequestGroupMembership() throws Exception {

        log.info("BEGIN");

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            // Jack is group admin for 'testgroup'
            jack = pmgr.getProfile(jack.getId());
            group = gmgr.getGroupById(group.getId());
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.ADMIN);
            Utils.endSession(true);

            // Sue posts herself to 'testgroup' to request membership
            token.setViewerId(sue.getUserId());

            String path = "/members/" + group.getHandle();
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = operation = registry.getRestHandler(path, "POST");
            Future<?> future = operation.execute(params, 
                new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

            JSONObject response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(202, response.getInt("code"));

            // Make sure Sue's request is in the queue
            group = gmgr.getGroupById(group.getId());
            List<GroupRequest> reqs = gmgr.getMembershipRequestsByGroup(group,0,-1);
            assertEquals(1, reqs.size());

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }


    public void testGetGroupMembershipRequests() throws Exception {

        log.info("BEGIN");

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            // Jack is group admin for 'testgroup'
            jack = pmgr.getProfile(jack.getId());
            group = gmgr.getGroupById(group.getId());
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.ADMIN);
            Utils.endSession(true);

            // Sue posts herself to 'testgroup' to request membership
            token.setViewerId(sue.getUserId());

            String path = "/members/" + group.getHandle();
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = operation = registry.getRestHandler(path, "POST");
            Future<?> future = operation.execute(params,
                new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

            JSONObject response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(202, response.getInt("code"));

            // Verify that new request is in Jack's list
            token.setViewerId(jack.getUserId());

            path = "/members/" + group.getHandle() + "/@requests";
            params = Maps.newHashMap();
            operation = operation = registry.getRestHandler(path, "GET");
            future = operation.execute(params, new StringReader(""), token, converter);

            RestfulCollection<?> collection = (RestfulCollection<?>)(future.get());
            assertNotNull(collection);
            assertEquals(1, collection.getEntry().size());
            for (Object entry : collection.getEntry()) {
                boolean isAnExpectedType = Person.class.isAssignableFrom(entry.getClass());
                assertTrue(String.format("Unexpected entry type: %s", entry.getClass()), isAnExpectedType);
                Person person = (Person)entry;
                assertEquals(sue.getUserId(), person.getId());
            }

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }


    public void testAcceptGroupMembershipRequest() throws Exception {

        log.info("BEGIN");

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            // Jack is group admin for 'testgroup'
            jack = pmgr.getProfile(jack.getId());
            group = gmgr.getGroupById(group.getId());
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.ADMIN);
            Utils.endSession(true);

            // Sue posts herself to 'testgroup' to request membership
            token.setViewerId(sue.getUserId());

            String path = "/members/" + group.getHandle();
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = operation = registry.getRestHandler(path, "POST");
            Future<?> future = operation.execute(params,
                new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

            JSONObject response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(202, response.getInt("code"));

            // Jack can now approve Sue's membership by POSTing Sue to the group
            token.setViewerId(jack.getUserId());

            path = "/members/" + group.getHandle();
            params = Maps.newHashMap();
            operation = operation = registry.getRestHandler(path, "POST");
            future = operation.execute(params,
                new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

            response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(200, response.getInt("code"));

            // Verify that Sue is now in the group
            group = gmgr.getGroupById(group.getId());
            assertEquals(2, gmgr.getMembershipsByGroup(group, 0, -1).size());

            // Verify that Sue's request is gone
            sue = pmgr.getProfile(sue.getId());
            group = gmgr.getGroupById(group.getId());
            sue = pmgr.getProfile(sue.getId());
            assertNull(gmgr.getMembershipRequest(group, sue));

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }


    public void testRejectGroupMembershipRequest() throws Exception {

        log.info("BEGIN");

        //setPathOperationAndParams("/members/@requests/{requestId}","DELETE", null, converter);
        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            // Jack is group admin for 'testgroup'
            jack = pmgr.getProfile(jack.getId());
            group = gmgr.getGroupById(group.getId());
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.ADMIN);
            Utils.endSession(true);

            // Sue posts herself to 'testgroup' to request membership
            token.setViewerId(sue.getUserId());

            String path = "/members/" + group.getHandle();
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = registry.getRestHandler(path, "POST");
            Future<?> future = operation.execute(params,
                new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

            JSONObject response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(202, response.getInt("code"));

            // Jack can now reject Sue's membership by DELETEing her request
            token.setViewerId(jack.getUserId());

            path = "/members/" + group.getHandle() + "/@requests/" + sue.getUserId();
            params = Maps.newHashMap();
            operation = registry.getRestHandler(path, "DELETE");
            future = operation.execute(params,
                new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

            response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(200, response.getInt("code"));

            // Verify that Sue's request is gone
            sue = pmgr.getProfile(sue.getId());
            group = gmgr.getGroupById(group.getId());
            sue = pmgr.getProfile(sue.getId());
            assertNull(gmgr.getMembershipRequest(group, sue));

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }


    public void testCreateGroupAdmin() throws Exception {

        log.info("BEGIN");

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            // Jack is group admin
            jack = pmgr.getProfile(jack.getId());
            sue = pmgr.getProfile(sue.getId());
            group = gmgr.getGroupById(group.getId());
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.ADMIN);
            gmgr.createMembership(group, sue, GroupRelationship.Relationship.MEMBER);
            Utils.endSession(true);

            // Jack posts Sue to group's admin collection to make here an admin
            token.setViewerId(jack.getUserId());

            String path = "/members/" + group.getHandle() + "/@admins";
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = registry.getRestHandler(path, "POST");
            Future<?> future = operation.execute(params,
                new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

            JSONObject response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(200, response.getInt("code"));

            // Verify that Sue is now an ADMIN
            sue = pmgr.getProfile(sue.getId());
            group = gmgr.getGroupById(group.getId());
            assertEquals(GroupRelationship.Relationship.ADMIN,
                    gmgr.getMembership(group, sue).getRelcode());

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }


    public void testCannotPromoteSelfToGroupAdmin() throws Exception {

        log.info("BEGIN");

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            // Jack is group admin
            jack = pmgr.getProfile(jack.getId());
            sue = pmgr.getProfile(sue.getId());
            group = gmgr.getGroupById(group.getId());
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.MEMBER);
            gmgr.createMembership(group, sue, GroupRelationship.Relationship.MEMBER);
            Utils.endSession(true);

            // Sue tries to promote herself to admin
            token.setViewerId(sue.getUserId());

            String path = "/members/" + group.getHandle() + "/@admins";
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = registry.getRestHandler(path, "POST");
            Future<?> future = operation.execute(params,
                new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

            JSONObject response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(400, response.getInt("code"));

            // Verify that Sue is not an ADMIN
            sue = pmgr.getProfile(sue.getId());
            group = gmgr.getGroupById(group.getId());
            assertEquals(GroupRelationship.Relationship.MEMBER,
                    gmgr.getMembership(group, sue).getRelcode());

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }


    public void testLeaveGroup() throws Exception {

        log.info("BEGIN");

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.ADMIN);
            gmgr.createMembership(group, sue, GroupRelationship.Relationship.MEMBER);
            Utils.endSession(true);

            // Sue leaves group
            token.setViewerId(sue.getUserId());

            String path = "/members/" + group.getHandle() + "/" + sue.getUserId();
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = registry.getRestHandler(path, "DELETE");
            Future<?> future = operation.execute(params,
                new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

            JSONObject response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(200, response.getInt("code"));

            // Verify that Sue is gone from group
            jack = pmgr.getProfile(jack.getId());
            sue = pmgr.getProfile(sue.getId());
            group = gmgr.getGroupById(group.getId());
            assertNotNull(gmgr.getMembership(group, jack));
            assertNull(gmgr.getMembership(group, sue));

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }


    public void testAdminCanRemoveGroupMember() throws Exception {

        log.info("BEGIN");

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.ADMIN);
            gmgr.createMembership(group, sue, GroupRelationship.Relationship.MEMBER);
            Utils.endSession(true);

            // Sue leaves group
            token.setViewerId(jack.getUserId());

            String path = "/members/" + group.getHandle() + "/" + sue.getUserId();
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = registry.getRestHandler(path, "DELETE");
            Future<?> future = operation.execute(params, new StringReader(""), token, converter);

            JSONObject response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(200, response.getInt("code"));

            // Verify that Sue is gone from group
            jack = pmgr.getProfile(jack.getId());
            sue = pmgr.getProfile(sue.getId());
            group = gmgr.getGroupById(group.getId());
            assertNotNull(gmgr.getMembership(group, jack));
            assertNull(gmgr.getMembership(group, sue));

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }


    public void testMemberCannotRemoveOtherGroupMember() throws Exception {

        log.info("BEGIN");

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        Profile jack = Utils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = Utils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        Group group = Utils.setupGroup("testgroup");
        Utils.endSession(true);

        try {
            // Jack and Sue are just plain old members
            gmgr.createMembership(group, jack, GroupRelationship.Relationship.MEMBER);
            gmgr.createMembership(group, sue, GroupRelationship.Relationship.MEMBER);
            Utils.endSession(true);

            // Jack tries to remove Sue from group
            token.setViewerId(jack.getUserId());

            String path = "/members/" + group.getHandle() + "/" + sue.getUserId();
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = registry.getRestHandler(path, "DELETE");
            Future<?> future = operation.execute(params, new StringReader(""), token, converter);
            JSONObject response = (JSONObject)(future.get());

            // Verify proper return code
            assertEquals(400, response.getInt("code"));

            // Verify that Sue is still in group
            jack = pmgr.getProfile(jack.getId());
            sue = pmgr.getProfile(sue.getId());
            group = gmgr.getGroupById(group.getId());
            assertNotNull(gmgr.getMembership(group, jack));
            assertNotNull(gmgr.getMembership(group, sue));

        } finally {
            Utils.teardownGroup(group.getHandle());
            Utils.teardownPerson(jack.getUserId());
            Utils.teardownPerson(sue.getUserId());
        }

        log.info("END");

    }
}
