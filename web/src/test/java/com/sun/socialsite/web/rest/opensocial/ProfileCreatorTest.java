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
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.business.ProfileManager;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.util.JSONWrapper;
import com.sun.socialsite.web.rest.config.SocialSiteGuiceModule;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;


/**
 * Tests the <code>ProfileCreator</code> class.
 */
public class ProfileCreatorTest extends TestCase {

    public static Log log = LogFactory.getLog(ProfileCreatorTest.class);

    public ProfileCreator profileCreator;


    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new SocialSiteGuiceModule());
        profileCreator = injector.getInstance(ProfileCreator.class);
        TestUtils.setupSocialSite();
    }


    public static Test suite() {
        return new TestSuite(ProfileCreatorTest.class);
    }


    public void tearDown() throws Exception {
    }


    public void testProfileCreator() throws Exception {

        log.info("BEGIN");

        ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
        JSONObject json = new JSONObject();
        JSONWrapper wrapper = new JSONWrapper(json);

        /*
         * Setup Bugs Bunny as the owner in the context JSON.
         */
        wrapper.put("assertions.owner.id", "bugs_bunny");
        wrapper.put("assertions.owner.displayName", "Bugs Bunny");
        wrapper.put("assertions.owner.name.formatted", "Bugs Bunny");
        wrapper.put("assertions.owner.name.givenName", "Bugs");
        wrapper.put("assertions.owner.name.familyName", "Bunny");
        wrapper.append("assertions.owner.emails", new JSONObject().put("value", "bugs.bunny@cartoons.com").put("primary", true));

        /*
         * Setup Daffy Duck as the viewer in the context JSON.
         */
        wrapper.put("assertions.viewer.id", "daffy_duck");
        wrapper.put("assertions.viewer.displayName", "Daffy Duck");
        wrapper.put("assertions.viewer.name.formatted", "Daffy Duck");
        wrapper.put("assertions.viewer.name.givenName", "Daffy");
        wrapper.put("assertions.viewer.name.familyName", "Duck");
        wrapper.append("assertions.viewer.emails", new JSONObject().put("value", "daffy.duck@cartoons.com").put("primary", true));

        /*
         * Construct a ConsumerContext object from the JSON, and pass it through our ProfileCreator.
         */
        try {
            ConsumerContext consumerContext = new ConsumerContext(null, json);
            profileCreator.handleContext(consumerContext);
            TestUtils.endSession(true);

            /*
             * Verify that a profile for the context owner (Bugs) was created.
             */
            Profile bugs = profileManager.getProfileByUserId("bugs_bunny");
            assertNotNull("A profile with userId='bugs_bunny' should exist", bugs);
            Assert.assertEquals("Bugs Bunny", bugs.getDisplayName());
            Assert.assertEquals("Bugs", bugs.getFirstName());
            Assert.assertEquals("Bunny", bugs.getLastName());
            Assert.assertEquals("bugs.bunny@cartoons.com", bugs.getPrimaryEmail());
            TestUtils.endSession(true);

            /*
             * Verify that a profile for the context viewer (Daffy) was created.
             */
            Profile daffy = profileManager.getProfileByUserId("daffy_duck");
            assertNotNull("A profile with userId='daffy_duck' should exist", daffy);
            Assert.assertEquals("Daffy Duck", daffy.getDisplayName());
            Assert.assertEquals("Daffy", daffy.getFirstName());
            Assert.assertEquals("Duck", daffy.getLastName());
            Assert.assertEquals("daffy.duck@cartoons.com", daffy.getPrimaryEmail());
            TestUtils.endSession(true);

        } finally {
            profileManager.removeProfile(profileManager.getProfileByUserId("bugs_bunny"));
            profileManager.removeProfile(profileManager.getProfileByUserId("daffy_duck"));
        }

        log.info("END");

    }

}
