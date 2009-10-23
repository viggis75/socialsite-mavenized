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

package com.sun.socialsite.web.rest.opensocial;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.model.FilterOperation;
import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.service.FakeSocialSiteGadgetToken;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.SocialSpiException;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.socialsite.Utils;
import com.sun.socialsite.business.AppManager;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.business.ProfileManager;
import com.sun.socialsite.business.RelationshipManager;
import com.sun.socialsite.business.SocialSiteActivityManager;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.pojos.RelationshipRequest;
import com.sun.socialsite.pojos.SocialSiteActivity;
import com.sun.socialsite.userapi.User;
import com.sun.socialsite.userapi.UserManager;
import com.sun.socialsite.web.rest.config.SocialSiteGuiceModule;

/**
 * Test the JSONOpensocialService
 */
public class OpenSocialServicesTest extends TestCase {

	protected OpenSocialServices db;
	protected SecurityToken token = null;

	// Assuming friendship level is 2
	public static int FRIENDSHIP_LEVEL = 2;

	  private static final UserId CANON_USER = new UserId(UserId.Type.userId, "canonical");	
	  private static final UserId JOHN_DOE = new UserId(UserId.Type.userId, "john.doe");
	
	  private static final GroupId SELF_GROUP = new GroupId(GroupId.Type.self, null);	  
	protected static String APP_ID = null; // SOCIALSITE_MOD
	protected static String ACTIVITY_ID = null; // SOCIALSITE_MOD
	  private static final String  CANONICAL_USER_ID = "canonical";
	  
	protected void setUp() throws Exception {
		Injector injector = Guice.createInjector(new SocialSiteGuiceModule());
		db = injector.getInstance(OpenSocialServices.class);

		Utils.setupSocialSite();

		ProfileManager profileManager = Factory.getSocialSite()
				.getProfileManager();
		UserManager userManager = Factory.getSocialSite().getUserManager();
		RelationshipManager friendManager = Factory.getSocialSite()
				.getRelationshipManager();
		AppManager appManager = Factory.getSocialSite().getAppManager();
		SocialSiteActivityManager socialSiteActivityManager = Factory
				.getSocialSite().getSocialSiteActivityManager();

		token = new FakeSocialSiteGadgetToken();

		// import sample data into SocialSite database
		JSONObject sample = new JSONObject(IOUtils.toString(ResourceLoader
				.openResource("canonicaldb.json"), "UTF-8"));

		// process people

		JSONArray people = sample.getJSONArray("people");
		for (int i = 0; i < people.length(); i++) {
			JSONObject person = people.getJSONObject(i);

			User user = new User();
			Date now = new Date();
			user.setUserId(person.getString("id"));
			user.setUserName(person.getString("id"));
			user.resetPassword(person.getString("id"), "SHA");
			user.setFullName(person.getJSONObject("name")
					.getString("givenName")
					+ " "
					+ person.getJSONObject("name").getString("familyName"));
			if (person.has("emails")
					&& person.getJSONArray("emails").length() > 0) {
				user.setEmailAddress(person.getJSONArray("emails")
						.getJSONObject(0).getString("value"));
			} else {
				user.setEmailAddress("error@example.com");
			}
			user.setCreationDate(now);
			user.setUpdateDate(now);
			user.setAccessDate(now);
			user.setEnabled(true);
			userManager.saveUser(user);			
//			userManager.grantRole("user", user);
//			userManager.saveUser(user);
			Factory.getSocialSite().flush();

			Profile profile = new Profile();
			profile.setUserId(user.getUserId());
			profile.setFirstName(person.getJSONObject("name").getString(
					"givenName"));
			profile.setLastName(person.getJSONObject("name").getString(
					"familyName"));
			profile.setPrimaryEmail(user.getEmailAddress());
			profileManager.saveProfile(profile);
			Factory.getSocialSite().flush();

			profile = profileManager.getProfile(profile.getId());
			profile.update(Profile.Format.OPENSOCIAL, person);
			profileManager.saveProfile(profile);
			Factory.getSocialSite().flush();
		}

		// process relationships

		JSONObject friendLinks = sample.getJSONObject("friendLinks");
		for (int i = 0; i < JSONObject.getNames(friendLinks).length; i++) {
			String userId = JSONObject.getNames(friendLinks)[i];
			JSONArray links = friendLinks.getJSONArray(userId);

			for (int j = 0; j < links.length(); j++) {
				String friendId = links.getString(j);
				Profile p1 = profileManager.getProfileByUserId(userId);
				Profile p2 = profileManager.getProfileByUserId(friendId);

				if (friendManager.getRelationship(p1, p2) == null) {

					friendManager.requestRelationship(p1, p2, FRIENDSHIP_LEVEL,
							"met at work");
					Factory.getSocialSite().flush();

					List<RelationshipRequest> freqs = friendManager
							.getRelationshipRequestsByFromProfile(p1, 0, -1);
					friendManager.acceptRelationshipRequest(freqs.get(0), 1);
					Factory.getSocialSite().flush();
				}
			}
			Factory.getSocialSite().flush();
		}

		// process activities

		// But first clear out existing activities, they will confuse the tests
		for (int i = 0; i < people.length(); i++) {
			JSONObject person = people.getJSONObject(i);
			String uid = person.getString("id");
			List<SocialSiteActivity> activities = socialSiteActivityManager
					.getUserActivities(profileManager.getProfileByUserId(uid),
							0, -1);
			for (Iterator<SocialSiteActivity> ait = activities.iterator(); ait
					.hasNext();) {
				SocialSiteActivity socialSiteActivity = ait.next();
				socialSiteActivityManager.removeActivity(socialSiteActivity);
			}
			Factory.getSocialSite().flush();
		}

		JSONObject activities = sample.getJSONObject("activities");
		for (int i = 0; i < JSONObject.getNames(activities).length; i++) {
			String userId = JSONObject.getNames(activities)[i];
			Profile profile = profileManager.getProfileByUserId(userId);
			JSONArray activitiesArray = activities.getJSONArray(userId);
			for (int j = 0; j < activitiesArray.length(); j++) {
				JSONObject activity = activitiesArray.getJSONObject(j);
				SocialSiteActivity acontent = new SocialSiteActivity();
				acontent.setProfile(profile);
				acontent.setTitle(activity.getString("title"));
				acontent.setBody(activity.getString("body"));
				acontent.setType(SocialSiteActivity.APP_MESSAGE);
				socialSiteActivityManager.saveActivity(acontent);

				// esure ACTIVITY_ID is assigned to a valid ACTIVITY_ID which
				// is owned by user canonical because that's what the test want.
				if ("canonical".equals(userId))
					ACTIVITY_ID = acontent.getId();
			}
			Factory.getSocialSite().flush();
		}

		// process app data

		// first, get a valid APP_ID
		/*
		 * Profile profile = profileManager.getProfileByUserId("canonical");
		 * List<App> apps = appppManager.getApps(0, 1); App app =
		 * apps.iterator().next(); APP_ID = app.getId();
		 * 
		 * JSONObject appdata = sample.getJSONObject("userApplications"); for
		 * (int i=0; i<JSONObject.getNames(appdata).length; i++) { String userId
		 * = JSONObject.getNames(friendLinks)[i]; JSONArray data =
		 * appdata.getJSONArray(userId);
		 * 
		 * for (int j=0; j<data.length(); j++) { String dataItem =
		 * data.getString(j); Profile p1 =
		 * profileManager.getProfileByUserId(userId); Profile p2 =
		 * profileManager.getProfileByUserId(friendId);
		 * 
		 * if (friendManager.getFriendRelationship(p1, p2) == null) {
		 * 
		 * friendManager.requestRelationship(p1, p2);
		 * Factory.getSocialSite().flush();
		 * 
		 * List<FriendRequest> freqs =
		 * friendManager.getFriendRequestsByFromProfile(p1, 0, -1);
		 * friendManager.acceptRelationship(freqs.get(0));
		 * Factory.getSocialSite().flush(); } } Factory.getSocialSite().flush();
		 * }
		 */
	}

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

	  /*
	  public void testGetExpectedAppData() throws Exception {
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
	        new FakeSocialSiteGadgetToken()).get();
	    assertFalse(responseItem.getEntry().isEmpty());
	    assertFalse(responseItem.getEntry().get(CANONICAL_USER_ID).isEmpty());

	    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).size() == 1);
	    assertFalse(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("count"));
	    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("size"));
	  }

	  public void testUpdateExpectedAppData() throws Exception {
		  FakeSocialSiteGadgetToken fakeToken = new FakeSocialSiteGadgetToken();
	    fakeToken.setViewerId(CANONICAL_USER_ID);

	    // Delete the data
	    db.updatePersonData(CANON_USER, SELF_GROUP, APP_ID,
	        null, Maps.immutableMap("count", "10", "newvalue", "20"), fakeToken);

	    // Fetch the remaining and test
	    DataCollection responseItem = db.getPersonData(
	        Sets.newHashSet(CANON_USER), SELF_GROUP, APP_ID, Collections.<String>emptySet(),
	        new FakeSocialSiteGadgetToken()).get();

	    assertFalse(responseItem.getEntry().isEmpty());
	    assertFalse(responseItem.getEntry().get(CANONICAL_USER_ID).isEmpty());

	    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).size() == 3);
	    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("count"));
	    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).get("count").equals("10"));
	    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).containsKey("newvalue"));
	    assertTrue(responseItem.getEntry().get(CANONICAL_USER_ID).get("newvalue").equals("20"));
	  }
  */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		ProfileManager profileManager = Factory.getSocialSite()
				.getProfileManager();
		UserManager userManager = Factory.getSocialSite().getUserManager();

		// tear down those same users
		JSONObject sample = new JSONObject(IOUtils.toString(ResourceLoader
				.openResource("canonicaldb.json"), "UTF-8"));

		JSONArray people = sample.getJSONArray("people");
		for (int i = 0; i < people.length(); i++) {
			JSONObject person = people.getJSONObject(i);

			profileManager.removeProfile(profileManager
					.getProfileByUserId(person.getString("id")));
			Factory.getSocialSite().flush();

			userManager.removeUser(userManager.getUserByUserId(person
					.getString("id")));
			Factory.getSocialSite().flush();
		}
	}

}
