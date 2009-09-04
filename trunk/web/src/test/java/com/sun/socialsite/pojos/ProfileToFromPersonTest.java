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

package com.sun.socialsite.pojos;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.socialsite.Utils;
import com.sun.socialsite.business.*;
import com.sun.socialsite.web.rest.config.SocialSiteGuiceModule;
import com.sun.socialsite.web.rest.model.PersonEx;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.opensocial.model.Person;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Profile POJO implementation
 */
public class ProfileToFromPersonTest extends TestCase {
    private BeanJsonConverter converter;
    private static Log log = LogFactory.getLog(ProfileToFromPersonTest.class);

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new SocialSiteGuiceModule());
        converter = injector.getInstance(BeanJsonConverter.class);
        Utils.setupSocialSite();
    }

    /**
     * Test profile to/from JSON to/from Shindig person conversion
     */
    @Test
    public void testProfileToPerson() throws Exception {
        log.info("BEGIN");
        ProfileManager mgr = Factory.getSocialSite().getProfileManager();
        try {

            // create a profile
            Profile profile1 = new Profile();
            profile1.setUserId("steves");
            profile1.setFirstName("Steve");
            profile1.setMiddleName("Bertram");
            profile1.setLastName("Smith");
            profile1.setPrimaryEmail("steves@example.com");
            mgr.saveProfile(profile1);
            Utils.endSession(true);
            assertNotNull(mgr.getProfileByUserId("steves"));

            // set some properties
            profile1 = mgr.getProfileByUserId("steves");
            profile1.setProfileProp("personal_gender", "male");

            profile1.setProfileProp("contact_primaryemail", "steves@example.com");
            profile1.setProfileProp("education_schools_1_name", "Old School");
            profile1.setProfileProp("identification_name_familyName", "Smith");
            profile1.setProfileProp("identification_name_givenName", "Steven");
            profile1.setProfileProp("morepersonal_drinker", "NO");
            profile1.setProfileProp("morepersonal_smoker", "NO");
            profile1.setProfileProp("status", "good times!");

            profile1.setProfileProp("identification_tags","[\"java, opensource, php\"] ");
            //profile1.setProfileProp("","");


            mgr.saveProfile(profile1);
            Factory.getSocialSite().flush();
            Utils.endSession(true);

            // convert it to JSON format
            profile1 = mgr.getProfileByUserId("steves");
            JSONObject jsonPerson = profile1.toJSON(Profile.Format.OPENSOCIAL);

            // convert JSON to Shindig Person object
            Person person = converter.convertToObject(jsonPerson.toString(), PersonEx.class);
            assertNotNull(person);

        } catch (Exception e) {
            log.error("ERROR", e);
        } finally {
            if (mgr.getProfileByUserId("steves") != null) {
                mgr.removeProfile(mgr.getProfileByUserId("steves"));
                Utils.endSession(true);
            }
        }
        log.info("END");

    }
}
