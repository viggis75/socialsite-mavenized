/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.social.opensocial.service;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.SocialSpiException;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.google.common.collect.Sets;

import com.sun.socialsite.web.rest.opensocial.OpenSocialServices;
import junit.framework.TestCase;

import java.util.Collections;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.model.FilterOperation;
import org.apache.shindig.protocol.model.SortOrder;

import javax.servlet.http.HttpServletResponse;


/**
 * Copy of Shindig class hacked up to test SocialSite.
 * TODO: keep this in-sync with the one in Shindig.
 */
public class JsonDbOpensocialServiceTest extends TestCase {
  protected OpenSocialServices db;

  private static final UserId CANON_USER = new UserId(UserId.Type.userId, "canonical");
  private static final UserId JOHN_DOE = new UserId(UserId.Type.userId, "john.doe");
  private static final UserId JANE_DOE = new UserId(UserId.Type.userId, "jane.doe");

  private static final GroupId SELF_GROUP = new GroupId(GroupId.Type.self, null);
  protected static String      APP_ID = null;      // SOCIALSITE_MOD
  protected static String      ACTIVITY_ID = null; // SOCIALSITE_MOD
  private static final String  CANONICAL_USER_ID = "canonical";

  protected SecurityToken token = null;

  @Override
  protected void setUp() throws Exception {
    //Injector injector = Guice.createInjector(new SocialApiTestsGuiceModule());
    //db = injector.getInstance(JsonDbOpensocialService.class);
  }


  // NO FURTHER CHANGES BELOW THIS POINT...


  public void testGetPersonDefaultFields() throws Exception {
    Person person = db
        .getPerson(CANON_USER, Person.Field.DEFAULT_FIELDS, token).get();

    assertNotNull("Canonical user not found", person);
    assertNotNull("Canonical user has no id", person.getId());
    assertNotNull("Canonical user has no name", person.getName());
    assertNotNull("Canonical user has no thumbnail",
        person.getThumbnailUrl());
  }

  public void testGetPersonAllFields() throws Exception {
    Person person = db.getPerson(CANON_USER, Person.Field.ALL_FIELDS, token).get();
    assertNotNull("Canonical user not found", person);
  }

public void testGetExpectedFriends() throws Exception {
    CollectionOptions options = new CollectionOptions();
    options.setSortBy(PersonService.TOP_FRIENDS_SORT);
    options.setSortOrder(SortOrder.ascending);
    options.setFilter(null);
    options.setFilterOperation(FilterOperation.contains);
    options.setFilterValue("");
    options.setFirst(0);
    options.setMax(20);

    RestfulCollection responseItem = db.getPeople(
        Sets.newHashSet(CANON_USER), new GroupId(GroupId.Type.friends, null),
        options, Collections.<String>emptySet(), token).get();
    assertNotNull(responseItem);
    assertEquals(responseItem.getTotalResults(), 4);
    // Test a couple of users
    assertEquals(((Person)responseItem.getEntry().get(0)).getId(), "george.doe");
    assertEquals(((Person)responseItem.getEntry().get(1)).getId(), "jane.doe");
  }

/* TODO: this test will not work until we implement one way relationships
  public void testGetExpectedUsersForPlural() throws Exception {
    CollectionOptions options = new CollectionOptions();
    options.setSortBy(PersonService.TOP_FRIENDS_SORT);
    options.setSortOrder(PersonService.SortOrder.ascending);
    options.setFilter(null);
    options.setFilterOperation(PersonService.FilterOperation.contains);
    options.setFilterValue("");
    options.setFirst(0);
    options.setMax(20);

    RestfulCollection<Person> responseItem = db.getPeople(
        Sets.newLinkedHashSet(JOHN_DOE, JANE_DOE), new GroupId(GroupId.Type.friends, null),
       options, Collections.<String>emptySet(), token).get();
    assertNotNull(responseItem);
    assertEquals(responseItem.getTotalResults(), 4);
    // Test a couple of users
    assertEquals(responseItem.getEntry().get(0).getId(), "john.doe");
    assertEquals(responseItem.getEntry().get(1).getId(), "jane.doe");
  }*/

  public void testGetExpectedActivities() throws Exception {
    CollectionOptions opts = new CollectionOptions();
    opts.setFirst(0);
    opts.setMax(-1);
    RestfulCollection<Activity> responseItem = db.getActivities(
        Sets.newHashSet(CANON_USER), SELF_GROUP, ACTIVITY_ID, 
        Collections.<String>emptySet(), opts, new FakeSocialSiteGadgetToken()).get();
    assertEquals(2, responseItem.getTotalResults());
  }

  public void testGetExpectedActivitiesForPlural() throws Exception {
    CollectionOptions opts = new CollectionOptions();
    opts.setFirst(0);
    opts.setMax(-1);
    RestfulCollection<Activity> responseItem = db.getActivities(
        Sets.newHashSet(CANON_USER, JOHN_DOE), SELF_GROUP, ACTIVITY_ID,
        Collections.<String>emptySet(), opts, new FakeSocialSiteGadgetToken()).get();
    assertEquals(3, responseItem.getTotalResults());
  }

  public void testGetExpectedActivity() throws Exception {
    FakeSocialSiteGadgetToken fakeToken = new FakeSocialSiteGadgetToken();
    fakeToken.setViewerId(CANONICAL_USER_ID);
    Activity activity = db.getActivity(CANON_USER, SELF_GROUP, APP_ID, 
        Sets.newHashSet("appId", "body", "mediaItems"), ACTIVITY_ID, fakeToken).get(); 
    assertNotNull(activity);
    // Check that some fields are fetched and others are not
    assertNotNull(activity.getBody());
    assertNull(activity.getBodyId());
  }

  public void testDeleteExpectedActivity() throws Exception {
    FakeSocialSiteGadgetToken fakeToken = new FakeSocialSiteGadgetToken();
    fakeToken.setViewerId(CANONICAL_USER_ID);
    db.deleteActivities(CANON_USER, SELF_GROUP, APP_ID, 
            Sets.newHashSet(ACTIVITY_ID), fakeToken);

    // Try to fetch the activity
    try {
      db.getActivity(CANON_USER, SELF_GROUP, APP_ID,
          Sets.newHashSet("appId", "body", "mediaItems"), ACTIVITY_ID, fakeToken).get(); 
      fail();
    } catch (SocialSpiException sse) {
      // assertEquals(HttpServletResponse.SC_BAD_REQUEST, sse.);
    }
  }

  /*public void testGetExpectedAppData() throws Exception {
    FakeSocialSiteGadgetToken fakeToken = new FakeSocialSiteGadgetToken();
    fakeToken.setViewerId(CANONICAL_USER_ID);
    DataCollection responseItem = db.getPersonData(
        Sets.newHashSet(CANON_USER), SELF_GROUP, APP_ID, Collections.<String>emptySet(),
        fakeToken).get();
    assertFalse(responseItem.getEntry().isEmpty());
    assertFalse(responseItem.getEntry().get(CANONICAL_USER_ID).isEmpty());

    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).size() == 2);
    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("count"));
    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("size"));
  }

  public void testGetExpectedAppDataForPlural() throws Exception {
    FakeSocialSiteGadgetToken fakeToken = new FakeSocialSiteGadgetToken();
    fakeToken.setViewerId(CANONICAL_USER_ID);

    DataCollection responseItem = db.getPersonData(
        Sets.newHashSet(CANON_USER, JOHN_DOE), SELF_GROUP, APP_ID, Collections.<String>emptySet(),
        fakeToken).get();
    assertFalse(responseItem.getEntry().isEmpty());
    assertFalse(responseItem.getEntry().get(CANONICAL_USER_ID).isEmpty());

    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).size() == 2);
    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("count"));
    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("size"));

    assertFalse(responseItem.getEntry().get(JOHN_DOE.getUserId()).isEmpty());
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).size() == 1);
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("count"));
  }

  public void testDeleteExpectedAppData() throws Exception {
    FakeSocialSiteGadgetToken fakeToken = new FakeSocialSiteGadgetToken();
    fakeToken.setViewerId(CANONICAL_USER_ID);

    // Delete the data
    db.deletePersonData(CANON_USER, SELF_GROUP, APP_ID,
        Sets.newHashSet("count"), fakeToken);

    // Fetch the remaining and test
    DataCollection responseItem = db.getPersonData(
        Sets.newHashSet(CANON_USER), SELF_GROUP, APP_ID, Collections.<String>emptySet(),
        new FakeGadgetToken()).get();
    assertFalse(responseItem.getEntry().isEmpty());
    assertFalse(responseItem.getEntry().get(CANONICAL_USER_ID).isEmpty());

    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).size() == 1);
    assertFalse(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("count"));
    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("size"));
  }

  public void testUpdateExpectedAppData() throws Exception {
    FakeGadgetToken fakeToken = new FakeGadgetToken();
    fakeToken.setViewerId(CANONICAL_USER_ID);

    // Delete the data
    db.updatePersonData(CANON_USER, SELF_GROUP, APP_ID,
        null, Maps.immutableMap("count", "10", "newvalue", "20"), fakeToken);

    // Fetch the remaining and test
    DataCollection responseItem = db.getPersonData(
        Sets.newHashSet(CANON_USER), SELF_GROUP, APP_ID, Collections.<String>emptySet(),
        new FakeGadgetToken()).get();

    assertFalse(responseItem.getEntry().isEmpty());
    assertFalse(responseItem.getEntry().get(CANONICAL_USER_ID).isEmpty());

    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).size() == 3);
    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("count"));
    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).get("count").equals("10"));
    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("newvalue"));
    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).get("newvalue").equals("20"));
  }*/
}