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
import com.sun.socialsite.TestUtils;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.business.RelationshipManager;
import com.sun.socialsite.business.ProfileManager;
import com.sun.socialsite.pojos.App;
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
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.easymock.EasyMock;
import org.json.JSONObject;

/**
 * Extend Shindig test to test our extensions to the Shindig handler.
 */
public class SocialSitePersonHandlerTest extends TestCase {

    public static Log log = LogFactory.getLog(SocialSitePersonHandlerTest.class);

    protected FakeSocialSiteGadgetToken token;

    protected PersonService personService;

    protected PersonHandlerImpl handler;

    private BeanJsonConverter converter;

    protected HandlerRegistry registry;


    @Override
    protected void setUp() throws Exception {
        TestUtils.setupSocialSite();

        Injector injector = Guice.createInjector(new SocialSiteGuiceModule());
        converter = injector.getInstance(BeanJsonConverter.class);

        TestUtils.setupSocialSite();
        List<App> apps = Factory.getSocialSite().getAppManager().getApps(0, 1);
        token = new FakeSocialSiteGadgetToken();
        token.setAppId(apps.get(0).getId());

        personService = EasyMock.createMock(PersonService.class);
        handler = new PersonHandlerImpl(personService, null, converter);
        registry = new DefaultHandlerRegistry(null, Sets.<Object>newHashSet(handler), converter,
            new HandlerExecutionListener.NoOpHandlerExecutionListener());
        
    }


    public void testFriendshipRequestAdd() throws Exception {

        log.info("BEGIN");

        Profile jack = TestUtils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = TestUtils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        TestUtils.endSession(true);

        // Jack requests friendship with Sue by posting Sue to his friends list
        token.setViewerId(jack.getUserId());

        String path = "/people/" + jack.getUserId() + "/@friends";
        Map<String, String[]> params = Maps.newHashMap();
        params.put("level", new String[] {"2"});
        params.put("knowknow", new String[] {"who knows"});
        RestHandler operation = registry.getRestHandler(path, "POST");
        Future<?> future = operation.execute(params, 
            new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

        JSONObject response = (JSONObject)(future.get());

        // Verify proper return code
        // TODO: fix this when shindig fixes HTTP response code handling
        //assertEquals(202, response.getInt("code"));

        // Verify that friend request now exists
        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        RelationshipManager fmgr = Factory.getSocialSite().getRelationshipManager();
        jack = pmgr.getProfile(jack.getId());
        sue = pmgr.getProfile(sue.getId());
        assertNotNull(fmgr.getRelationshipRequest(jack, sue));

        fmgr.removeRelationshipRequest(fmgr.getRelationshipRequest(jack, sue));
        TestUtils.endSession(true);

        TestUtils.teardownPerson(jack.getUserId());
        TestUtils.teardownPerson(sue.getUserId());

        log.info("END");

    }


    public void testFriendshipRequestGet() throws Exception {

        log.info("BEGIN");

        Profile jack = TestUtils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = TestUtils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        TestUtils.endSession(true);

        // Jack requests friendship with Sue by posting Sue to his friends list
        token.setViewerId(jack.getUserId());

        String path = "/people/" + jack.getUserId() + "/@friends";
        Map<String, String[]> params = Maps.newHashMap();
        params.put("level", new String[] {"2"});
        params.put("knowknow", new String[] {"who knows"});
        RestHandler operation = registry.getRestHandler(path, "POST");
        Future<?> future = operation.execute(params,
            new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

        // Verify that Jack's friend request shows up in Sue's @requests list
        token.setViewerId(sue.getUserId());

        path = "/people/" + sue.getUserId() + "/@requests";
        params = Maps.newHashMap();
        operation = registry.getRestHandler(path, "GET");
        future = operation.execute(params, new StringReader(""), token, converter);

        RestfulCollection<?> collection = (RestfulCollection<?>)(future.get());
        assertNotNull(collection);
        boolean requestPresent = false;
        for (Object entry : collection.getEntry()) {
            boolean isAnExpectedType = Person.class.isAssignableFrom(entry.getClass());
            assertTrue(String.format("Unexpected entry type: %s", entry.getClass()), isAnExpectedType);
            Person person = (Person)entry;
            if (person.getId().equals(jack.getUserId())) {
                requestPresent = true;
            }
        }
        assertTrue("Did not find Jack in Sue's @requests list", requestPresent);

        // Remove friend request
        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        RelationshipManager fmgr = Factory.getSocialSite().getRelationshipManager();
        jack = pmgr.getProfile(jack.getId());
        sue = pmgr.getProfile(sue.getId());
        fmgr.removeRelationshipRequest(fmgr.getRelationshipRequest(jack, sue));
        TestUtils.endSession(true);

        TestUtils.teardownPerson(jack.getUserId());
        TestUtils.teardownPerson(sue.getUserId());

        log.info("END");

    }


    public void testFriendshipAccept() throws Exception {

        log.info("BEGIN");

        Profile jack = TestUtils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = TestUtils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        TestUtils.endSession(true);

        // Jack requests friendship with Sue by posting Sue to his friends list

        String path = "/people/" + jack.getUserId() + "/@friends";
        Map<String, String[]> params = Maps.newHashMap();
        params.put("level", new String[] {"2"});
        params.put("knowknow", new String[] {"who knows"});
        RestHandler operation = registry.getRestHandler(path, "POST");
        Future<?> future = operation.execute(params,
            new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

        // Sue accepts friendship with Jack by posting Jack to her friends list

        path = "/people/" + sue.getUserId() + "/@friends";
        params = Maps.newHashMap();
        operation = registry.getRestHandler(path, "POST");
        future = operation.execute(params,
            new StringReader(jack.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

        JSONObject response = (JSONObject)(future.get());

        // Verify proper return code
        // TODO: fix this when shindig fixes HTTP response code handling
        //assertEquals(200, response.getInt("code"));

        // Verify that friend relationship now exists
        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        RelationshipManager fmgr = Factory.getSocialSite().getRelationshipManager();
        jack = pmgr.getProfile(jack.getId());
        sue = pmgr.getProfile(sue.getId());
        assertNotNull(fmgr.getRelationship(jack, sue));

        TestUtils.teardownPerson(jack.getUserId());
        TestUtils.teardownPerson(sue.getUserId());

        log.info("END");

    }


    public void testFriendshipIgore() throws Exception {

        log.info("BEGIN");

        Profile jack = TestUtils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = TestUtils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        TestUtils.endSession(true);

        // Jack requests friendship with Sue by posting Sue to his friends list
        token.setViewerId(jack.getUserId());
        String path = "/people/" + jack.getUserId() + "/@friends";
        Map<String, String[]> params = Maps.newHashMap();
        params.put("level", new String[] {"2"});
        params.put("knowknow", new String[] {"who knows"});
        RestHandler operation = registry.getRestHandler(path, "POST");
        Future<?> future = operation.execute(params,
            new StringReader(sue.toJSON(Profile.Format.OPENSOCIAL).toString()), token, converter);

        // Sue gets Jack's request in her request list
        token.setViewerId(sue.getUserId());

        path = "/people/" + sue.getUserId() + "/@requests";
        params = Maps.newHashMap();
        operation = registry.getRestHandler(path, "GET");
        future = operation.execute(params, new StringReader(""), token, converter);

        RestfulCollection<?> collection = (RestfulCollection<?>)(future.get());
        assertNotNull(collection);
        Person notfriend = null;
        for (Object entry : collection.getEntry()) {
            boolean isAnExpectedType = Person.class.isAssignableFrom(entry.getClass());
            assertTrue(String.format("Unexpected entry type: %s", entry.getClass()), isAnExpectedType);
            Person person = (Person)entry;
            if (person.getId().equals(jack.getUserId())) {
                notfriend = person;
            }
        }
        assertNotNull(notfriend);

        // Sue ignores friendship by deleting the friend request
        token.setViewerId(sue.getUserId());

        path = "/people/" + sue.getUserId() + "/@requests/" + notfriend.getId();
        params = Maps.newHashMap();
        operation = registry.getRestHandler(path, "DELETE");
        future = operation.execute(params, new StringReader(""), token, converter);

        JSONObject response = (JSONObject)(future.get());

        // Verify proper return code
        // TODO: fix this when shindig fixes HTTP response code handling
        //assertEquals(200, response.getInt("code"));

        // Verify that friend request from Jack to Sue no longer exists
        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        RelationshipManager fmgr = Factory.getSocialSite().getRelationshipManager();
        jack = pmgr.getProfile(jack.getId());
        sue = pmgr.getProfile(sue.getId());
        assertNull(fmgr.getRelationshipRequest(jack, sue));

        TestUtils.teardownPerson(jack.getUserId());
        TestUtils.teardownPerson(sue.getUserId());

        log.info("END");

    }


    public void testFriendshipRemove() throws Exception {

        log.info("BEGIN");

        Profile jack = TestUtils.setupPerson("jack.flappy", "Jack", "Flappy", "jack.flappy@example.com");
        Profile sue = TestUtils.setupPerson("sue.who", "Sue", "Who", "sue.who@example.com");
        TestUtils.endSession(true);

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        RelationshipManager fmgr = Factory.getSocialSite().getRelationshipManager();
        fmgr.createMutualRelationship(jack, 1, sue, 1, "met at work");
        TestUtils.endSession(true);

        // Verify that friend relationship exists
        jack = pmgr.getProfile(jack.getId());
        sue = pmgr.getProfile(sue.getId());
        assertNotNull(fmgr.getRelationship(jack, sue));
        assertNotNull(fmgr.getRelationship(sue, jack));

        // Sue removes friendship relationship
        token.setViewerId(sue.getUserId());

        String path = "/people/" + sue.getUserId() + "/@friends/" + jack.getUserId();
        Map<String, String[]> params = Maps.newHashMap();
        RestHandler operation = registry.getRestHandler(path, "DELETE");
        Future<?> future = operation.execute(params, new StringReader(""), token, converter);
        JSONObject response = (JSONObject)(future.get());

        // Verify proper return code
        // TODO: fix this when shindig fixes HTTP response code handling
        //assertEquals(200, response.getInt("code"));

        // Verify that sue to jack relationship no longer exists
        jack = pmgr.getProfile(jack.getId());
        sue = pmgr.getProfile(sue.getId());
        assertNull(fmgr.getRelationship(sue, jack));

        TestUtils.teardownPerson(jack.getUserId());
        TestUtils.teardownPerson(sue.getUserId());

        log.info("END");

    }

}
