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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.socialsite.Utils;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.business.GroupManager;
import com.sun.socialsite.business.ProfileManager;
import com.sun.socialsite.pojos.Group;
import com.sun.socialsite.pojos.GroupRequest;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.web.rest.config.SocialSiteGuiceModule;
import com.sun.socialsite.web.rest.model.ViewerRelationship;
import java.util.List;
import junit.framework.TestCase;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.json.JSONObject;


public class ViewerRelationshipTest extends TestCase {

    public void testBeanConversion() throws Exception {

        ProfileManager pmgr = Factory.getSocialSite().getProfileManager();
        GroupManager gmgr = Factory.getSocialSite().getGroupManager();

        // create jimmy
        Profile profile = new Profile();
        profile.setUserId("jimmy");
        profile.setFirstName("James");
        profile.setMiddleName("Samuel");
        profile.setLastName("Flappenpopper");
        profile.setPrimaryEmail("jimmy@papercrafts.com");
        pmgr.saveProfile(profile);
        Utils.endSession(true);

        // create jimmy's group
        Group group = new Group();
        group.setHandle("paperpopper");
        group.setName("Paper Poppers");
        gmgr.saveGroup(group);
        Utils.endSession(true);

        try {
            // get jimmy into the group
            profile = pmgr.getProfile(profile.getId());
            gmgr.requestMembership(group, profile);
            Utils.endSession(true);

            group = gmgr.getGroupById(group.getId());
            List<GroupRequest> reqs = gmgr.getMembershipRequestsByGroup(group, 0, -1);
            gmgr.acceptMembership(reqs.get(0));
            Utils.endSession(true);

            group = gmgr.getGroupById(group.getId());
            profile = pmgr.getProfile(profile.getId());
            ViewerRelationship vrel = new ViewerRelationship(profile, profile);

            // assert that jimmy is in a group
            assertEquals(1, vrel.getGroups().size());

            Injector injector = Guice.createInjector(new SocialSiteGuiceModule());
            BeanJsonConverter converter = injector.getInstance(BeanJsonConverter.class);

            JSONObject vrelJson = new JSONObject(converter.convertToString(vrel));
            assertNotNull(vrelJson);

            ViewerRelationship vrelObject = converter.convertToObject(
                vrelJson.toString(), ViewerRelationship.class);

            assertNotNull(vrelObject);
            assertEquals(1, vrelObject.getGroups().size());

        } finally {
            Utils.teardownGroup(group.getHandle());
            profile = pmgr.getProfile(profile.getId());
            pmgr.removeProfile(profile);
            Utils.endSession(true);
        }

    }

}
