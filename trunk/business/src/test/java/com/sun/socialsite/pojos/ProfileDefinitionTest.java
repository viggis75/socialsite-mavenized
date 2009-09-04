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

import com.sun.socialsite.Utils;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.util.TextUtil;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


/**
 * Test parsing of profiledefs.xml file.
 */
public class ProfileDefinitionTest extends TestCase {
    private static Log log = LogFactory.getLog(ProfileDefinitionTest.class);

    public ProfileDefinitionTest() {
    }

    public static void setUpClass() throws Exception {
    }

    public static void tearDownClass() throws Exception {
    }

    public void setUp() throws Exception {
        // setup socialsite
        Utils.setupSocialSite();
    }

    public void tearDown() {
    }

    private void logPropertyHolderContents(ProfileDefinition.PropertyDefinitionHolder holder, String indent) {
        for (ProfileDefinition.PropertyDefinition prop: holder.getPropertyDefinitions()) {
            log.debug(indent + "Property: " + prop.getName());
        }
        for (ProfileDefinition.PropertyObjectDefinition obj : holder.getPropertyObjectDefinitions()) {
            log.debug(indent + "PropertyObject: " + obj.getName());
            logPropertyHolderContents(obj, indent + "    ");
        }
        for (ProfileDefinition.PropertyObjectCollectionDefinition col : holder.getPropertyObjectCollectionDefinitions()) {
            log.debug(indent + "PropertyObjectCollection: " + col.getName());
            logPropertyHolderContents(col, indent + "    ");
        }
    }

    /**
     * Test of build method, of class ProfileDef.
     */
    @Test
    public void testProfileDefParser() {
        try {

            // Test basic parsing of profiledefs.xml
            ProfileDefinition pdef = Factory.getSocialSite().getProfileManager().getProfileDefinition();

            for (ProfileDefinition.DisplaySectionDefinition ds : pdef.getDisplaySectionDefinitions()) {
                log.debug("Display Section: " + ds.getName());
                logPropertyHolderContents(ds, "");
            }

            // Standard property set has 7 display sections
            assertEquals(7, pdef.getDisplaySectionDefinitions().size());

        } catch (Exception ex) {
            log.error("ERROR testing property def parser", ex);
            fail();
        }
    }

    @Test
    public void testI18NStrings() {
        try {
            // Test basic parsing of profiledefs.xml
            ProfileDefinition pdef = Factory.getSocialSite().getProfileManager().getProfileDefinition();

            boolean failed = false;
            for (ProfileDefinition.DisplaySectionDefinition ds : pdef.getDisplaySectionDefinitions()) {
                if (!verifyI18NStrings(ds)) failed = true;
            }
            if (failed) fail("Missing I18N string!");

        } catch (Exception ex) {
            log.error("ERROR testing property def parser", ex);
            fail();
        }
    }

    private boolean verifyI18NStrings(ProfileDefinition.PropertyDefinitionHolder holder) {
        boolean success = true;
        try {
            String dsname = null;
            try {
                dsname = TextUtil.getResourceString(holder.getNamekey());
            } catch (Exception intentionallyIgnored) {}
            if (dsname == null) {
                success = false;
                log.error("I18N key not found: " + holder.getNamekey());
            }

            for (ProfileDefinition.PropertyDefinition prop: holder.getPropertyDefinitions()) {
                String name = null;
                try {
                    name = TextUtil.getResourceString(prop.getNamekey());
                } catch (Exception intentionallyIgnored) {}
                if (name == null) {
                    success = false;
                    log.error("I18N key not found: " + prop.getNamekey());
                }
            }
            for (ProfileDefinition.PropertyObjectDefinition obj : holder.getPropertyObjectDefinitions()) {
                String name = null;
                try {
                    name = TextUtil.getResourceString(obj.getNamekey());
                } catch (Exception intentionallyIgnored) {}
                if (name == null) {
                    success = false;
                    log.error("I18N key not found: " + obj.getNamekey());
                }
                if (!verifyI18NStrings(obj)) {
                    success = false;
                }
            }
            for (ProfileDefinition.PropertyObjectCollectionDefinition col : holder.getPropertyObjectCollectionDefinitions()) {
                String name = null;
                try {
                    name = TextUtil.getResourceString(col.getNamekey());
                } catch (Exception intentionallyIgnored) {}
                if (name == null) {
                    success = false;
                    log.error("I18N key not found: " + col.getNamekey());
                }
                if (!verifyI18NStrings(col)) {
                    success = false;
                }
            }

        } catch (Exception ex) {
            log.error("ERROR verifying I18N strings", ex);
            fail();
            return false;
        }
        return success;
    }

}


