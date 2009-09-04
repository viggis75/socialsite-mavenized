/*
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package com.sun.socialsite;

import com.sun.socialsite.pojos.Group;
import com.sun.socialsite.userapi.User;
import com.sun.socialsite.userapi.UserManagementException;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.business.startup.Startup;
import com.sun.socialsite.pojos.Profile;


/**
 * Utility class for unit test classes.
 */
public final class Utils {


    public static void setupSocialSite() throws SocialSiteException {
        if (!Factory.isBootstrapped()) {

            // do core services preparation
            Startup.prepare();

            // do application bootstrapping and init
            Factory.bootstrap();

            // always initialize the properties manager and flush
            Factory.getSocialSite().initialize();
            Factory.getSocialSite().flush();
        }
    }

    public static Profile setupPerson(
            String username, String first, String last, String email)
            throws SocialSiteException, UserManagementException {
        User user = setupUser(username);
        user.setFullName(first + " " + last);
        user.setEmailAddress(email);
        return setupProfile(user, first, last);
    }
    
    public static User setupUser(String userName) 
            throws UserManagementException {
        User user = new User();
        user.setUserId(userName);
        user.setUserName(userName);
        user.setPassword(userName);
        user.setFullName("Test User");
        user.setEmailAddress("TestUser@dev.null");
        user.setLocale("en_US");
        user.setTimeZone("America/Los_Angeles");
        user.setCreationDate(new java.util.Date());
        user.setUpdateDate(new java.util.Date());
        user.setUserId(userName);
        user.setEnabled(true);
        Factory.getSocialSite().getUserManager().addUser(user);
        return user;
    }


    public static Profile setupProfile(User user, String first, String last)
            throws SocialSiteException {
        Profile profile = new Profile();
        profile.setUserId(user.getUserName());
        profile.setFirstName(user.getUserName());
        profile.setLastName(user.getUserName());
        profile.setPrimaryEmail(user.getEmailAddress());
        Factory.getSocialSite().getProfileManager().saveProfile(profile);
        return profile;
    }

    public static Group setupGroup(String string) throws SocialSiteException {
        Group group = new Group();
        group.setName(string);
        group.setHandle(string);
        group.setDescription(string);
        Factory.getSocialSite().getGroupManager().saveGroup(group);
        return group;
    }

    public static void shutdownSocialSite() throws Exception {

        // trigger shutdown
        Factory.getSocialSite().shutdown();
    }


    /**
     * Convenience method that simulates the end of a typical session.
     *
     * Normally this would be triggered by the end of the response in the webapp
     * but for unit tests we need to do this explicitly.
     *
     * @param flush true if you want to flush changes to db before releasing
     */
    public static void endSession(boolean flush) throws Exception {
        if (flush) {
            Factory.getSocialSite().flush();
        }
        Factory.getSocialSite().release();
    }

    public static void teardownPerson(String userId) throws Exception {
        teardownProfile(userId);
        teardownUser(userId);        
    }

    public static void teardownProfile(String userId) throws Exception {
        Profile managedProfile = Factory.getSocialSite().getProfileManager().getProfileByUserId(userId);
        Factory.getSocialSite().getProfileManager().removeProfile(managedProfile);
        Factory.getSocialSite().flush();
    }

    public static void teardownUser(String userId) throws Exception {
        User managedUser = Factory.getSocialSite().getUserManager().getUserByUserId(userId);
        Factory.getSocialSite().getUserManager().removeUser(managedUser);
        Factory.getSocialSite().flush();
    }

    public static void teardownGroup(String handle) throws Exception {
        Group group = Factory.getSocialSite().getGroupManager().getGroupByHandle(handle);
        Factory.getSocialSite().getGroupManager().removeGroup(group);
        Factory.getSocialSite().flush();
    }
}
