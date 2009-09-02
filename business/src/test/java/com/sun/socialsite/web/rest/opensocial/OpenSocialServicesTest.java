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

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.sun.socialsite.TestUtils;
import com.sun.socialsite.business.SocialSiteActivityManager;
import com.sun.socialsite.business.AppManager;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.business.RelationshipManager;
import com.sun.socialsite.business.ProfileManager;
import com.sun.socialsite.pojos.SocialSiteActivity;
import com.sun.socialsite.pojos.RelationshipRequest;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.userapi.User;
import com.sun.socialsite.userapi.UserManager;
import com.sun.socialsite.web.rest.config.SocialSiteGuiceModule;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.social.opensocial.service.FakeSocialSiteGadgetToken;
import org.apache.shindig.social.opensocial.service.JsonDbOpensocialServiceTest;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Test the JSONOpensocialService
 */
public class OpenSocialServicesTest extends JsonDbOpensocialServiceTest {

    // Assuming friendship level is 2
    public static int FRIENDSHIP_LEVEL = 2;

    @Override
    protected void setUp() throws Exception {
        Injector injector = Guice.createInjector(new SocialSiteGuiceModule());
        db = injector.getInstance(OpenSocialServices.class);

        TestUtils.setupSocialSite();

        ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
        UserManager    userManager = Factory.getSocialSite().getUserManager();
        RelationshipManager  friendManager = Factory.getSocialSite().getRelationshipManager();
        AppManager     appManager = Factory.getSocialSite().getAppManager();
        SocialSiteActivityManager socialSiteActivityManager = Factory.getSocialSite().getSocialSiteActivityManager();
                
        token = new FakeSocialSiteGadgetToken();

        // import sample data into SocialSite database
        JSONObject sample = new JSONObject(IOUtils.toString(
                ResourceLoader.openResource("canonicaldb.json"), "UTF-8"));
        
        // process people
        
        JSONArray people = sample.getJSONArray("people");
        for (int i=0; i<people.length(); i++) {
            JSONObject person = people.getJSONObject(i);
                    
            User user = new User();
            Date now = new Date();
            user.setUserId(person.getString("id"));
            user.setUserName(person.getString("id"));
            user.resetPassword(person.getString("id"), "SHA");
            user.setFullName(
                    person.getJSONObject("name").getString("givenName") + " " +
                    person.getJSONObject("name").getString("familyName"));
            if (person.has("emails") && person.getJSONArray("emails").length() > 0) {
                user.setEmailAddress(person.getJSONArray("emails").getJSONObject(0).getString("value"));
            } else {
                user.setEmailAddress("error@example.com");
            }
            user.setCreationDate(now);
            user.setUpdateDate(now);
            user.setAccessDate(now);
            user.setEnabled(true);
            userManager.saveUser(user);
            userManager.grantRole("user", user);
            userManager.saveUser(user);
            Factory.getSocialSite().flush();
            
            Profile profile = new Profile();
            profile.setUserId(user.getUserId());
            profile.setFirstName(person.getJSONObject("name").getString("givenName"));
            profile.setLastName(person.getJSONObject("name").getString("familyName"));
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
        for (int i=0; i<JSONObject.getNames(friendLinks).length; i++) {
            String userId = JSONObject.getNames(friendLinks)[i];
            JSONArray links = friendLinks.getJSONArray(userId);

            for (int j=0; j<links.length(); j++) {
                String friendId = links.getString(j);
                Profile p1 = profileManager.getProfileByUserId(userId);
                Profile p2 = profileManager.getProfileByUserId(friendId);

                if (friendManager.getRelationship(p1, p2) == null) {
                    
                    friendManager.requestRelationship(p1, p2, FRIENDSHIP_LEVEL, "met at work");
                    Factory.getSocialSite().flush();
                    
                    List<RelationshipRequest> freqs = friendManager.getRelationshipRequestsByFromProfile(p1, 0, -1);
                    friendManager.acceptRelationshipRequest(freqs.get(0), 1);
                    Factory.getSocialSite().flush();
                }
            }
            Factory.getSocialSite().flush();
        }
        
        // process activities
        
        // But first clear out existing activities, they will confuse the tests
        for (int i=0; i<people.length(); i++) {
            JSONObject person = people.getJSONObject(i);
            String uid = person.getString("id");
            List<SocialSiteActivity> activities = socialSiteActivityManager.getUserActivities(profileManager.getProfileByUserId(uid), 0, -1);
            for (Iterator<SocialSiteActivity> ait = activities.iterator(); ait.hasNext();) {
                SocialSiteActivity socialSiteActivity = ait.next();
                socialSiteActivityManager.removeActivity(socialSiteActivity);
            }
            Factory.getSocialSite().flush();
        }
        
        
        JSONObject activities = sample.getJSONObject("activities");
        for (int i=0; i<JSONObject.getNames(activities).length; i++) {
            String userId = JSONObject.getNames(activities)[i];
            Profile profile = profileManager.getProfileByUserId(userId);
            JSONArray activitiesArray = activities.getJSONArray(userId);
            for (int j=0; j<activitiesArray.length(); j++) {
                JSONObject activity = activitiesArray.getJSONObject(j);
                SocialSiteActivity acontent = new SocialSiteActivity();
                acontent.setProfile(profile);
                acontent.setTitle(activity.getString("title"));
                acontent.setBody(activity.getString("body"));
                acontent.setType(SocialSiteActivity.APP_MESSAGE);
                socialSiteActivityManager.saveActivity(acontent);
                
                // esure ACTIVITY_ID is assigned to a valid ACTIVITY_ID which 
                // is owned by user canonical because that's what the test want.
                if ("canonical".equals(userId)) ACTIVITY_ID = acontent.getId();
            }
            Factory.getSocialSite().flush();
        }
        
        // process app data
        
        // first, get a valid APP_ID
        /*Profile profile = profileManager.getProfileByUserId("canonical");
        List<App> apps = appppManager.getApps(0, 1);
        App app = apps.iterator().next();        
        APP_ID = app.getId();
        
        JSONObject appdata = sample.getJSONObject("userApplications");
        for (int i=0; i<JSONObject.getNames(appdata).length; i++) {
            String userId = JSONObject.getNames(friendLinks)[i];
            JSONArray data = appdata.getJSONArray(userId);

            for (int j=0; j<data.length(); j++) {
                String dataItem = data.getString(j);
                Profile p1 = profileManager.getProfileByUserId(userId);
                Profile p2 = profileManager.getProfileByUserId(friendId);

                if (friendManager.getFriendRelationship(p1, p2) == null) {
                    
                    friendManager.requestRelationship(p1, p2);
                    Factory.getSocialSite().flush();
                    
                    List<FriendRequest> freqs = friendManager.getFriendRequestsByFromProfile(p1, 0, -1);
                    friendManager.acceptRelationship(freqs.get(0));
                    Factory.getSocialSite().flush();
                }
            }
            Factory.getSocialSite().flush();
        }*/
    }
    

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
        UserManager userManager = Factory.getSocialSite().getUserManager();

        // tear down those same users
        JSONObject sample = new JSONObject(IOUtils.toString(
                ResourceLoader.openResource("canonicaldb.json"), "UTF-8"));
        
        JSONArray people = sample.getJSONArray("people");
        for (int i=0; i<people.length(); i++) {
            JSONObject person = people.getJSONObject(i);

            profileManager.removeProfile(profileManager.getProfileByUserId(person.getString("id")));           
            Factory.getSocialSite().flush();
            
            userManager.removeUser(userManager.getUserByUserId(person.getString("id")));           
            Factory.getSocialSite().flush();
        }
    }
    
    
}

