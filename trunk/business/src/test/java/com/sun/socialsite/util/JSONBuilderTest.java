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

package com.sun.socialsite.util;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Test JSONBuilder POJO implementation
 */
public class JSONBuilderTest extends TestCase {

    private static Log log = LogFactory.getLog(JSONBuilderTest.class);


    /**
     * Verify that two compatible json values can be combined.
     */
    @Test
    public void testCombiningCompatibleObjects() throws Exception {

        log.info("BEGIN");

        JSONObject json1 = new JSONObject();
        JSONWrapper wrapper1 = new JSONWrapper(json1);
        wrapper1.put("a.b.c1", "1");

        JSONObject json2 = new JSONObject();
        JSONWrapper wrapper2 = new JSONWrapper(json2);
        wrapper2.put("a.b.c2", "2");

        JSONBuilder jb = new JSONBuilder();
        jb.addAll(json1);
        jb.addAll(json2);

        JSONWrapper combinedWrapper = new JSONWrapper(jb.toJSONObject());

        // Verify that the two end up with the same contents
        assertEquals("1", combinedWrapper.getString("a.b.c1"));
        assertEquals("2", combinedWrapper.getString("a.b.c2"));

        log.info("END");

    }


    /**
     * Verify that two incompatible json values cannot be combined.
     */
    @Test
    public void testCombiningIncompatibleObjects() throws Exception {

        log.info("BEGIN");

        JSONObject json1 = new JSONObject();
        JSONWrapper wrapper1 = new JSONWrapper(json1);
        wrapper1.put("a.b", "1");

        JSONObject json2 = new JSONObject();
        JSONWrapper wrapper2 = new JSONWrapper(json2);
        wrapper2.put("a.b.c2", "2");

        JSONBuilder jb = new JSONBuilder();
        jb.addAll(json1);

        JSONException expectedException = null;
        try {
            jb.addAll(json2);
        } catch (JSONException e) {
            expectedException = e;
        }

        assertNotNull(expectedException);

        log.info("END");

    }

}
