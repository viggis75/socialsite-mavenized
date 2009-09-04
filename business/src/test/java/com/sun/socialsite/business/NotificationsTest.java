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
import com.sun.socialsite.pojos.GroupRequest;
import com.sun.socialsite.pojos.MessageContent;
import com.sun.socialsite.pojos.Profile;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test Notifications-related business operations.
 */
public class NotificationsTest extends TestCase {

    public static Log log = LogFactory.getLog(NotificationsTest.class);

    // using email-like terms as they differ method to methog in manager
    private static final String SUBJECT = "some subject";
    private static final String BODY = "some body";
    
    private Profile aa = null;
    private Profile bb = null;

    @Override
    public void setUp() throws Exception {
        Utils.setupSocialSite();
        aa = Utils.setupPerson("aa", "user", "a", "a@sun.com");
        bb = Utils.setupPerson("bb", "user", "b", "b@sun.com");
        Utils.setupPerson("cc", "user", "c", "c@sun.com");
        Utils.setupPerson("dd", "user", "d", "d@sun.com");
        Utils.endSession(true);
    }

    public static Test suite() {
        return new TestSuite(NotificationsTest.class);
    }


    @Override
    public void tearDown() throws Exception {
        Utils.teardownPerson("aa");
        Utils.teardownPerson("bb");
        Utils.teardownPerson("cc");
        Utils.teardownPerson("dd");
        Utils.endSession(true);
    }

    /*
     * Not currently testing message contents since there is some
     * confusion about summary, title, and contents.
     */
    public void testNotificationCRUD() throws Exception {
        log.info("BEGIN");
        try {
            NotificationManager mgr =
                Factory.getSocialSite().getNotificationManager();
            assertNotNull(mgr);

            clearAaAndBb(mgr);

            // Send simple notification. Type must be set as NOTIFICATION,
            // which doesn't seem necessary given the method name. Issue 281 filed.
            Group nullGroup = null; // null group means not sent to one person
            mgr.recordNotification(aa, bb, nullGroup,
                MessageContent.NOTIFICATION, SUBJECT, BODY, true);
            Utils.endSession(true);

            // check sent/received
            assertEquals(0, mgr.getUserInbox(aa, 0, -1).size());
            assertEquals(1, mgr.getUserSentBox(aa, 0, -1).size());
            assertEquals(1, mgr.getUserInbox(bb, 0, -1).size());
            assertEquals(0, mgr.getUserSentBox(bb, 0, -1).size());

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }
        log.info("END");
    }

    public void testSystemNotificationCRUD() throws Exception {
        log.info("BEGIN");
        try {
            NotificationManager mgr =
                Factory.getSocialSite().getNotificationManager();
            assertNotNull(mgr);

            clearAaAndBb(mgr);

            mgr.recordSystemNotification(SUBJECT + "a", BODY + "a");
            mgr.recordSystemNotification(SUBJECT + "b", BODY + "b");
            mgr.recordSystemNotification(SUBJECT + "c", BODY + "c");
            mgr.recordSystemNotification(SUBJECT + "d", BODY + "d");
            Utils.endSession(true);

            // check number received
            List<MessageContent> nots = mgr.getSystemNotifications(0, -1);
            assertEquals(4, nots.size());

            // make sure index/offest work. get 2nd and 3rd notifications
            nots = mgr.getSystemNotifications(1, 2);
            assertEquals(2, nots.size());
            assertEquals(SUBJECT + "c", nots.get(0).getSummary());

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");
    }

    public void testSystemNotificationInboxCRUD() throws Exception {
        log.info("BEGIN");
        try {
            NotificationManager mgr =
                Factory.getSocialSite().getNotificationManager();
            assertNotNull(mgr);

            clearAaAndBb(mgr);

            // clear system notifications
            List<MessageContent> all =
                mgr.getSystemNotifications(0, -1);
            for (MessageContent mc : all) {
                mgr.removeNotification(mc.getId(), aa);
            }
            Utils.endSession(true);
            all = mgr.getSystemNotifications(0, -1);
            assertTrue(all.isEmpty());

            mgr.recordSystemNotification(SUBJECT, BODY);
            Utils.endSession(true);

            // both users should now have one message
            assertEquals(1, mgr.getUserInbox(aa, 0, -1).size());
            assertEquals(1, mgr.getUserInbox(bb, 0, -1).size());


        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }

        log.info("END");
    }

    public void testGroupNotificationInboxCRUD() throws Exception {
        log.info("BEGIN");
        final String GROUP_HANDLE = "group0";

        try {

            // create group
            Group group = clearGroupsAndCreate(GROUP_HANDLE);

            // add profiles to group
            List<Profile> profiles = new ArrayList<Profile>();
            profiles.add(aa);
            profiles.add(bb);
            addProfilesToGroup(profiles, group);

            NotificationManager mgr =
                Factory.getSocialSite().getNotificationManager();
            assertNotNull(mgr);

            clearAaAndBb(mgr);
            
            // clear system notifications
            List<MessageContent> all =
                mgr.getSystemNotifications(0, -1);
            for (MessageContent mc : all) {
                mgr.removeNotification(mc.getId(), aa);
            }
            Utils.endSession(true);
            all = mgr.getSystemNotifications(0, -1);
            assertTrue(all.isEmpty());

            mgr.recordGroupNotification(aa, group, BODY, SUBJECT);

            // both users should now have one message
            assertEquals(1, mgr.getUserInbox(aa, 0, -1).size());
            assertEquals(1, mgr.getUserInbox(bb, 0, -1).size());


        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        } finally {
            try {
                Utils.teardownGroup(GROUP_HANDLE);
            } catch (Throwable t) {
                // don't really care, just don't want this to mask anything
            }
        }

        log.info("END");
    }

    private void clearAaAndBb(NotificationManager mgr) throws Exception {
        // clear notifications for profiles aa and bb
        List<MessageContent> all = mgr.getUserInbox(aa, 0, -1);
        all.addAll(mgr.getUserSentBox(aa, 0, -1));
        for (MessageContent mc : all) {
            if(MessageContent.SYS_NOTIFICATIONS.equals(mc.getCatLabel()))
                continue;
            mgr.removeNotification(mc.getId(), aa);
        }
        all = mgr.getUserInbox(aa, 0, -1);
        all.addAll(mgr.getUserInbox(bb, 0, -1));
        all.addAll(mgr.getUserSentBox(bb, 0, -1));
        for (MessageContent mc : all) {
            if(MessageContent.SYS_NOTIFICATIONS.equals(mc.getCatLabel()))
                continue;
            mgr.removeNotification(mc.getId(), bb);
        }
        Utils.endSession(true);
    }

    private Group clearGroupsAndCreate(String handle) throws Exception {
        Group g = null;
        GroupManager mgr = Factory.getSocialSite().getGroupManager();
        assertNotNull(mgr);

        List<Group> all = mgr.getGroups(0, -1);
        for (Group e : all) {
            mgr.removeGroup(e);
        }
        Utils.endSession(true);
        g = Utils.setupGroup(handle);
        Utils.endSession(true);
        assertNotNull(g);
        return g;
    }

    // assuming profile was added successfully -- this is covered in GroupTest
    private void addProfilesToGroup(List<Profile> ps, Group g)
            throws Exception {
        GroupManager mgr = Factory.getSocialSite().getGroupManager();

        for (Profile p : ps) {
            mgr.requestMembership(g, p);
        }
        Utils.endSession(true);

        List<GroupRequest> reqs = mgr.getMembershipRequestsByGroup(g, 0, -1);
        assertEquals(ps.size(), reqs.size());
        for (GroupRequest req : reqs) {
            mgr.acceptMembership(req);
        }
        Utils.endSession(true);
    }

}
