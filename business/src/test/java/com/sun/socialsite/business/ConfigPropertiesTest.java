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

import com.sun.socialsite.TestUtils;
import com.sun.socialsite.pojos.RuntimeConfigProperty;
import java.util.Map;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test Properties-related business operations.
 */
public class ConfigPropertiesTest extends TestCase {

    public static Log log = LogFactory.getLog(ConfigPropertiesTest.class);


    public void setUp() throws Exception {
        TestUtils.setupSocialSite();

    }


    public static Test suite() {
        return new TestSuite(ConfigPropertiesTest.class);
    }


    public void tearDown() throws Exception {
    }


    public void testProperiesCRUD() throws Exception {

        log.info("BEGIN");

        // remember, the properties table is initialized during SocialSite startup
        ConfigPropertiesManager mgr = Factory.getSocialSite().getConfigPropertiesManager();
        TestUtils.endSession(true);

        RuntimeConfigProperty prop = null;

        // get a property by name
        prop = mgr.getProperty("site.name");
        assertNotNull(prop);

        // update a property
        prop.setValue("testtest");
        mgr.saveProperty(prop);
        TestUtils.endSession(true);

        // make sure property was updated
        prop = null;
        prop = mgr.getProperty("site.name");
        assertNotNull(prop);
        assertEquals("testtest", prop.getValue());

        // get all properties
        Map<String, RuntimeConfigProperty> props = mgr.getProperties();
        assertNotNull(props);
        assertTrue(props.containsKey("site.name"));

        // update multiple properties
        prop = props.get("site.name");
        prop.setValue("foofoo");
        prop = props.get("site.description");
        prop.setValue("blahblah");
        mgr.saveProperties(props);
        TestUtils.endSession(true);

        // make sure all properties were updated
        props = mgr.getProperties();
        assertNotNull(props);
        assertEquals("foofoo", props.get("site.name").getValue());
        assertEquals("blahblah", props.get("site.description").getValue());

        log.info("END");

    }

}
