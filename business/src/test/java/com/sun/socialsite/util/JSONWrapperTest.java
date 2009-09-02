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
import org.json.JSONObject;
import org.junit.Test;

/**
 * Test JSONWrapper POJO implementation
 */
public class JSONWrapperTest extends TestCase {

    private static Log log = LogFactory.getLog(JSONWrapperTest.class);


    /**
     * Verify that two json values end up with the same contents when
     * populated by equivalent <code>JSONObject</code> and
     * <code>JSONWrapper</code> methods.
     */
    @Test
    public void testEquivalency() throws Exception {

        log.info("BEGIN");

        // Populate the first json directly via JSONObject methods
        JSONObject json1 = new JSONObject();
        JSONWrapper wrapper1 = new JSONWrapper(json1);
        json1.put("a", new JSONObject());
        json1.getJSONObject("a").put("aa", "aaa");
        assertEquals("aaa", wrapper1.getString("a.aa"));

        // Populate the second json with the equivalent JSONWrapper methods
        JSONObject json2 = new JSONObject();
        JSONWrapper wrapper2 = new JSONWrapper(json2);
        wrapper2.put("a.aa", "aaa");
        assertEquals("aaa", wrapper2.getString("a.aa"));

        // Verify that the two end up with the same contents
        assertEquals(json1.toString(), json2.toString());

        log.info("END");

    }


    /**
     * Verify that calling <code>JSONObject.put</code> with a key containing
     * a period creates an entry which is effectively invisible to a corresponding
     * <code>JSONWrapper</code> object.  This is expected, since the latter should
     * treat the period as a delmitor for descension into a nested item.
     */
    @Test
    public void testRawWriteWithAPeriod() throws Exception {

        log.info("BEGIN");

        JSONObject json = new JSONObject();
        JSONWrapper wrapper = new JSONWrapper(json);
        json.put("a.a", "aa");

        // The value should be visible via the raw JSONObject
        assertEquals("aa", json.getString("a.a"));

        // ...but it should be effectively invisible via the JSONWrapper
        assertNull(wrapper.optString("a.a", null));

        log.info("END");

    }


    /**
     * Verify that calling <code>JSONWrapper.put</code> with a key containing
     * a period creates an entry which is automatically nested at an appropriate
     * level within the underlying <code>JSONObject</code> object.  This is
     * expected, since <code>JSONWrapper</code> should treat the period as a
     * nesting delmitor.
     */
    @Test
    public void testWrappedWriteWithAPeriod() throws Exception {

        log.info("BEGIN");

        JSONObject json = new JSONObject();
        JSONWrapper wrapper = new JSONWrapper(json);
        wrapper.put("a.a", "aa");

        // The value should not be visible under the "a.a." key in the raw JSONObject
        assertNull(json.optString("a.a", null));

        // ...but it should be visible if we manually handle the nesting in JSONObject
        assertEquals("aa", json.getJSONObject("a").getString("a"));

        // ...and it should be available via the JSONWrapper's automatic nesting logic
        assertEquals("aa", wrapper.getString("a.a"));

        log.info("END");

    }

}
