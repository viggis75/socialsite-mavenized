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

package com.sun.socialsite.userapi;

import com.sun.socialsite.TestUtils;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.config.Config;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test User related business operations.
 */
public class UserTest extends TestCase {

    public static Log log = LogFactory.getLog(UserTest.class);


    public UserTest(String name) {
        super(name);
    }


    public static Test suite() {
        return new TestSuite(UserTest.class);
    }


    public void setUp() throws Exception {
        TestUtils.setupSocialSite();
    }

    public void tearDown() throws Exception {
    }


    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testUserCRUD() throws Exception {

        UserManager mgr = Factory.getSocialSite().getUserManager();
        User user = null;

        User testUser = new User();
        testUser.setUserId("testUser");
        testUser.setPassword("password");
        testUser.setUserName("Test User Screen Name");
        testUser.setFullName("Test User");
        testUser.setEmailAddress("TestUser@dev.null");
        testUser.setLocale("en_US");
        testUser.setTimeZone("America/Los_Angeles");
        testUser.setCreationDate(new java.util.Date());
        testUser.setEnabled(Boolean.TRUE);

        // make sure test user does not exist
        user = mgr.getUserByUserId(testUser.getUserId());
        assertNull(user);

        // add test user
        mgr.addUser(testUser);
        String userId = testUser.getUserId();
        TestUtils.endSession(true);

        // make sure test user exists
        user = null;
        user = mgr.getUserByUserId(userId);
        assertNotNull(user);
        assertEquals(testUser, user);

        // modify user and save
        user.setUserName("testtesttest");
        user.setFullName("testtesttest");
        mgr.saveUser(user);
        TestUtils.endSession(true);

        // make sure changes were saved
        user = null;
        user = mgr.getUserByUserId(userId);
        assertNotNull(user);
        assertEquals("testtesttest", user.getUserName());
        assertEquals("testtesttest", user.getFullName());

        // remove test user
        mgr.removeUser(user);
        TestUtils.endSession(true);

        // make sure user no longer exists
        user = null;
        user = mgr.getUserByUserId(userId);
        assertNull(user);
    }


    /**
     * Test lookup mechanisms.
     */
    public void testUserLookups() throws Exception {

        UserManager mgr = Factory.getSocialSite().getUserManager();
        User user = null;

        // add test user
        User testUser = TestUtils.setupUser("userTestUser");
        TestUtils.endSession(true);

        // lookup by id
        String userId = testUser.getUserId();
        user = null;
        user = mgr.getUserByUserId(userId);
        assertNotNull(user);
        assertEquals(testUser.getUserId(), user.getUserId());

        // lookup by UserName (part)
        user = null;
        List users1 = mgr.getUsersStartingWith(userId.substring(0, 3), Boolean.TRUE, 0, 1);
        assertEquals(1, users1.size());
        user = (User) users1.get(0);
        assertNotNull(user);
        assertEquals(testUser.getUserId(), user.getUserId());

        // lookup by Email (part)
        user = null;
        List users2 = mgr.getUsersStartingWith(testUser.getEmailAddress().substring(0, 3), Boolean.TRUE, 0, 1);
        assertEquals(1, users2.size());
        user = (User) users2.get(0);
        assertNotNull(user);
        assertEquals(testUser.getUserId(), user.getUserId());

        // make sure disable users are not returned
        user.setEnabled(Boolean.FALSE);
        mgr.saveUser(user);
        TestUtils.endSession(true);
        user = null;
        user = mgr.getUserByUserId(testUser.getUserId(), Boolean.TRUE);
        assertNull(user);

        // remove test user
        TestUtils.teardownUser(testUser.getUserId());
        TestUtils.endSession(true);
    }


    /**
     * Test basic user role persistence ... Add, Remove
     */
    public void testRoleCRUD() throws Exception {

        UserManager mgr = Factory.getSocialSite().getUserManager();
        User user = null;

        // add test user
        User testUser = TestUtils.setupUser("roleTestUser");
        TestUtils.endSession(true);

        user = mgr.getUserByUserId(testUser.getUserId());
        assertNotNull(user);
        mgr.grantRole("editor", user);
        TestUtils.endSession(true);

        user = mgr.getUserByUserId(testUser.getUserId());
        if (Config.getBooleanProperty("users.firstUserAdmin")) {
            assertEquals(2, mgr.getRoles(user.getUserId()).size());
            assertTrue(mgr.hasRole("editor", user.getUserId()));
            assertTrue(mgr.hasRole("admin", user.getUserId()));
        } else {
            assertEquals(1, mgr.getRoles(user.getUserId()).size());
            assertTrue(mgr.hasRole("editor", user.getUserId()));
            assertFalse(mgr.hasRole("admin", user.getUserId()));
        }

        // remove role
        mgr.revokeRole("editor",user);
        mgr.saveUser(user);
        TestUtils.endSession(true);

        // check that role was removed
        user = null;
        user = mgr.getUserByUserId(testUser.getUserId());
        assertNotNull(user);
        if (Config.getBooleanProperty("users.firstUserAdmin")) {
            assertEquals(1, mgr.getRoles(user.getUserId()).size());
            assertFalse(mgr.hasRole("editor", user.getUserId()));
            assertTrue(mgr.hasRole("admin", user.getUserId()));
        } else {
            assertEquals(0, mgr.getRoles(user.getUserId()).size());
            assertFalse(mgr.hasRole("editor", user.getUserId()));
            assertFalse(mgr.hasRole("admin", user.getUserId()));
        }
        // add role
        mgr.grantRole("editor", user);
        mgr.saveUser(user);
        TestUtils.endSession(true);

        // check that role was added
        user = null;
        user = mgr.getUserByUserId(testUser.getUserId());
        assertNotNull(user);
        if (Config.getBooleanProperty("users.firstUserAdmin")) {
            assertEquals(2, mgr.getRoles(user.getUserId()).size());
            assertTrue(mgr.hasRole("editor", user.getUserId()));
            assertTrue(mgr.hasRole("admin", user.getUserId()));
        } else {
            assertEquals(1, mgr.getRoles(user.getUserId()).size());
            assertTrue(mgr.hasRole("editor", user.getUserId()));
            assertFalse(mgr.hasRole("admin", user.getUserId()));
        }
        // remove test user
        TestUtils.teardownUser(testUser.getUserId());
        TestUtils.endSession(true);
    }


    /**
     * Test ability to remove a user with a full set of data.
     */
    public void testRemoveLoadedUser() throws Exception {
        // TODO: implement testRemoveLoadedUser
    }

}
